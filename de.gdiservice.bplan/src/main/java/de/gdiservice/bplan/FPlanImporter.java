package de.gdiservice.bplan;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import de.gdiservice.bplan.poi.*;
import de.gdiservice.bplan.poi.VerbundenerPlan.RechtscharakterPlanaenderung;
import de.gdiservice.bplan.dao.FPlanDAO;
import de.gdiservice.bplan.konvertierung.Gemeinde;
import de.gdiservice.bplan.konvertierung.GemeindeDAO;
import de.gdiservice.bplan.konvertierung.Konvertierung;
import de.gdiservice.bplan.konvertierung.Konvertierung.KonvertierungStatus;
import de.gdiservice.bplan.konvertierung.KonvertierungDAO;
import de.gdiservice.util.DBConnectionParameter;
import de.gdiservice.util.EMailSender;
import de.gdiservice.wfs.BFitzFPlanFactoryV5_1;
import de.gdiservice.wfs.WFSClient;
import de.gdiservice.wfs.WFSFactory;


public class FPlanImporter implements XPPlanImporterI {

    final static Logger logger = LoggerFactory.getLogger(FPlanImporter.class);
    

    protected String fplanTable;
    protected String konvertierungTable;
    
    protected String kvwmapUrl;
    
    private String kvwmapLoginName;
    private String kvwmapPassword;

    WFSFactory<FPlan> wfsFactory;
    
    boolean test = false;

    public enum Version {
        v5_1,
        v5_1n,
        v5_3
    }

    public FPlanImporter(String konvertierungTable, String fplanTable, Version version, String kvwmapUrl, String kvwmapLoginName, String kvwmapPassword) {
        this.fplanTable = fplanTable;
        this.konvertierungTable = konvertierungTable;
        this.kvwmapUrl = kvwmapUrl;
        this.kvwmapLoginName = kvwmapLoginName;
        this.kvwmapPassword = kvwmapPassword;
        if (version==Version.v5_1) {
            this.wfsFactory = new BFitzFPlanFactoryV5_1(false);
        } else if (version==Version.v5_1n) {
            this.wfsFactory = new BFitzFPlanFactoryV5_1(true);
        }        
        if (kvwmapLoginName==null || kvwmapPassword==null) {
            logger.error("kvwmapLoginName="+this.kvwmapLoginName+" or kvwmapPassword"+" is null", new Exception());
        }
    }
    
    public void setTest(boolean isTest) {
        test = isTest;
    }
    


    public void updateFPlaene(Connection conWrite, Connection conRead, ImportLogger importLogger, ImportConfigEntry entry, List<FPlan> fPlans) throws SQLException  {
        boolean autoCommit = conWrite.getAutoCommit();
        if (autoCommit) {
            conWrite.setAutoCommit(false);
        }

        if (fPlans != null) {
            FPlanDAO fplanDao = new FPlanDAO(conWrite, conRead, fplanTable);
            KonvertierungDAO konvertierungDAO = new KonvertierungDAO(conWrite, konvertierungTable);

            for (FPlan plan : fPlans) {        
                logger.debug("Verarbeite: {} {}", plan.getGml_id(), plan.getName());
                try {
                    if (!isStelleResponsible(entry.stelle_id, plan)) {
                        throw new IllegalAccessException("Stelle mit Id \""+entry.stelle_id+"\" ist nicht für die Gemeinde " + Arrays.toString(plan.getGemeinde()) + " zuständig");
                    }
                    
                    // List<FPlan> teilPlaene = splitPlan(plan) ? FPlanGroup.split(plan) : Collections.singletonList(plan);
                    List<FPlan> teilPlaene = FPlanGroup.split(plan);

                    String geomValidierungsResult = fplanDao.validateGeom(plan.getGeom());
                    if ("Valid Geometry".equals(geomValidierungsResult)) {

                        List<FPlan> listDBPlaene = fplanDao.findByInternalIdLikeGmlId(plan.getGml_id());
                        
                        /**
                         * Setzen der Felder aendert und wurdegeaendertvon
                         */
                        FPlan previousPlan = null;
                        for (int teilPlanNr=0; teilPlanNr<teilPlaene.size(); teilPlanNr++) {
                            FPlan teilPlan = teilPlaene.get(teilPlanNr);
                            teilPlan.setInternalId(plan.getGml_id()+"-"+teilPlanNr);
                            
                            if (teilPlanNr>0) {
                                UUID teilPlanUUID = (teilPlanNr<listDBPlaene.size()) ? listDBPlaene.get(teilPlanNr).getGml_id() : UUID.randomUUID();
                                teilPlan.setGml_id(teilPlanUUID);
                                teilPlan.setName( teilPlan.getName() + " " + String.valueOf(teilPlanNr) + ". Änderung");
                                VerbundenerPlan aendert = new VerbundenerPlan(plan.getName(), RechtscharakterPlanaenderung.Aenderung, plan.getNummer(), previousPlan.getGml_id().toString());
                                teilPlan.setAendert(aendert);
                                VerbundenerPlan wurdegeaendertvon =  new VerbundenerPlan(plan.getName(), RechtscharakterPlanaenderung.Aenderung, plan.getNummer(), teilPlan.getGml_id().toString()); 
                                previousPlan.setWurdeGeaendertVon(wurdegeaendertvon);
                            }
                            previousPlan = teilPlan; 
                            
                        }
                        
                        
                        
                        for (int teilPlanNr=0; teilPlanNr<teilPlaene.size(); teilPlanNr++) {
                            FPlan teilPlan = teilPlaene.get(teilPlanNr);
                            FPlan dbPlan = (listDBPlaene.size()>teilPlanNr) ? listDBPlaene.get(teilPlanNr) : null;
                            Konvertierung konvertierung;
                            if (dbPlan == null) {
                                // neuer FPlan
                                GemeindeDAO gemeindeDAO = new GemeindeDAO(conWrite);
                                de.gdiservice.bplan.poi.Gemeinde gemeinde = teilPlan.getGemeinde()[0];
                                List<Gemeinde> kvGemeinden = gemeindeDAO.find(gemeinde.getRs(), Integer.parseInt(gemeinde.getAgs()), gemeinde.getGemeindename(),gemeinde.getOrtsteilname());
                                if (kvGemeinden==null || kvGemeinden.size()==0) {
                                    throw new IllegalArgumentException("Gemeinde "+gemeinde+" vom WFS ist nicht in der DB hinterlegt.");
                                }       
    
                                konvertierung = new Konvertierung();
                                konvertierung.stelle_id = kvGemeinden.get(0).stelle_id;
                                konvertierung.status = KonvertierungStatus.erstellt;
                                konvertierung.user_id = 1;
                                konvertierung.gebietseinheiten = gemeinde.getRs();
                                konvertierung.bezeichnung = teilPlan.getName()+" "+gemeinde.getGemeindename()+" "+teilPlan.getNummer();
                                konvertierung.veroeffentlicht = false;
                                konvertierung.beschreibung = "automatically created from wfs-fietz";
                                Integer iPlanArt = null;
    
                                try {
                                    if (teilPlan.getPlanart()!=null) {
                                        iPlanArt = Integer.parseInt(teilPlan.getPlanart());
                                    } else {
                                        throw new IllegalArgumentException("WFS enthält keine Planart.");
                                    }
                                } catch (NumberFormatException e) {
                                    throw new IllegalArgumentException("Planart für gmlId=\""+teilPlan.getGml_id()+"\" \""+teilPlan.getPlanart()+"\" ist nicht gültig.");
                                }                    
                                FPlan.PlanArt planArt = FPlan.PlanArt.get(iPlanArt);
                                if (planArt==null) {
                                    throw new IllegalArgumentException("Planart für gmlId=\""+teilPlan.getGml_id()+"\" \""+planArt+"\" ist im System nicht bekannt.");
                                }
                                konvertierung.planart = "FP-Plan";
                                konvertierung.epsg = Konvertierung.EPSGCodes.EPSG_25833;
                                konvertierung.input_epsg = Konvertierung.EPSGCodes.EPSG_25833;
                                konvertierung.output_epsg = Konvertierung.EPSGCodes.EPSG_25833;
                                konvertierung.geom_precision =3;
    
                                Konvertierung dbKonvertierung = konvertierungDAO.insert(konvertierung);                    
                                teilPlan.setKonvertierungId(dbKonvertierung.id);
                                fplanDao.insert(teilPlan);
                                conWrite.commit();
                                
                                boolean succeded = validate(konvertierung, teilPlan, kvwmapUrl, importLogger);   
                                boolean isLastPlan = (teilPlanNr == teilPlaene.size()-1);
                                if (succeded) {
                                    if (isLastPlan && teilPlan.auslegungsstartdatum!=null && teilPlan.auslegungsstartdatum.length>0) {
                                        konvertierungDAO.updatePublishDate(konvertierung.id, teilPlan.auslegungsstartdatum[teilPlan.auslegungsstartdatum.length-1]);                                        
                                    } else {
                                        if (teilPlan.wirksamkeitsdatum!=null) {
                                            konvertierungDAO.updatePublishDate(konvertierung.id, teilPlan.wirksamkeitsdatum);
                                        }
                                    }
                                }
                                logger.info("BPLanImpoter: Plan gmlId=\""+teilPlan.getGml_id()+"\" inserted.");
                                importLogger.addLine(String.format("inserted %s", teilPlan.getGml_id()));
    
                            } else {
                                // update FPlan                                
                                if (HasChangedFunctions.hasChanged(teilPlan, dbPlan)) {
                                    logger.debug("update plan");
                                    teilPlan.setKonvertierungId(dbPlan.getKonvertierungId());
                                    fplanDao.update(teilPlan);
                                    int updateCount = konvertierungDAO.update(teilPlan.konvertierung_id);
                                    if (updateCount == 0) {
                                        throw new IllegalArgumentException("In der DB existiert ein FPlan mit der gmlId. Der zugehörige Eintrag in der Konvertierungs-Tabelle existiert aber nicht.");
                                    }
                                    konvertierungDAO.updatePublishFlag(teilPlan.konvertierung_id, false);
                                    konvertierung = konvertierungDAO.find(teilPlan.konvertierung_id); 
                                    
                                    logger.info("FPLanImpoter: Plan gmlId=\""+teilPlan.getGml_id()+"\" updated.");
                                    importLogger.addLine(String.format("updated %s", teilPlan.getGml_id()));
                                    conWrite.commit();
                                    
                                    boolean succeded = validate(konvertierung, teilPlan, kvwmapUrl, importLogger);
//                                    konvertierungDAO.updatePublishFlag(konvertierung.id, succeded);
                                    if (teilPlan.wirksamkeitsdatum!=null && succeded) {
                                        konvertierungDAO.updatePublishDate(konvertierung.id, teilPlan.wirksamkeitsdatum);
                                    }
                                    
                                } else {
                                    logger.info("FPLanImpoter: Plan gmlId=\""+teilPlan.getGml_id()+"\" unchanged.");
                                    importLogger.addLine(String.format("unchanged %s", teilPlan.getGml_id()));
                                }
    
                            }
                        }
                    } else {
                        logger.info("BPLanImpoter: Plan gmlId=\""+plan.getGml_id()+"\" Geometry is not valid: "+ geomValidierungsResult +".");
                        importLogger.addError("BPLanImpoter: Plan gmlId=\""+plan.getGml_id()+"\" Geometry is not valid: "+ geomValidierungsResult +".");
                    }
                } catch (Exception ex) {                    
                    try {
                        conWrite.rollback();
                    } 
                    catch (SQLException e) {
                        logger.error("rollback Error", e);
                    }
                    importLogger.addError("error updating FPlan [gmlId="+ plan.getGml_id() +" name=\""+ plan.getName() +"\"] from service \"" + entry.bezeichnung + "\" with url=\"" + entry.onlineresource +"\" error:["+ex.getMessage()+"]");
                    logger.error("error updating FPlan [gmlId="+ plan.getGml_id() +" name=\""+ plan.getName() +"\"] from service \"" + entry.bezeichnung + "\" with url=\"" + entry.onlineresource +"\"", ex);
                } 
            }
        }
        if (autoCommit) {
            conWrite.setAutoCommit(autoCommit);
        }
    }
    
    /**
     * Prüft, ob die Stelle mit der stellenId für die Gemeinde zuständig ist.
     *  
     * @param stelle_id
     * @param plan
     * @return
     */
    private boolean isStelleResponsible(Integer stelle_id, FPlan plan) {
        de.gdiservice.bplan.poi.Gemeinde[] gemeinden = plan.getGemeinde();
        if (gemeinden!=null) {
            for (de.gdiservice.bplan.poi.Gemeinde gemeinde : gemeinden) {
                if (gemeinde.rs.startsWith(String.valueOf(stelle_id))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String  parseUrl(String s) throws MalformedURLException, URISyntaxException  {
        URL u = new URL(s);
        return new URI(
               u.getProtocol(), 
               u.getAuthority(), 
               u.getPath(),
               u.getQuery(), 
               u.getRef()).
               toURL().toExternalForm();
   }
    
    public boolean validate(Konvertierung konvertierung, FPlan bplan, String kvwmapUrl, ImportLogger importLogger) throws ValidationException {
        
        try {
            boolean succedded = true; 
            ObjectReader objectReader = new ObjectMapper().reader();
            
            StringBuilder sb = new StringBuilder(kvwmapUrl);                
            sb.append("?go=xplankonverter_konvertierung");
            sb.append("&konvertierung_id=").append(bplan.getKonvertierungId());
            sb.append("&login_name=").append(this.kvwmapLoginName);
            sb.append("&Stelle_ID=").append(konvertierung.stelle_id);
            sb.append("&passwort=").append(this.kvwmapPassword);
            sb.append("&mime_type=formatter&format=json_result");
            
            String s;
            try {
                s = parseUrl(sb.toString());
            } catch (MalformedURLException | URISyntaxException ex) {
                throw new ValidationException("Validierung konnte nicht durchgeführt werden. URL nicht interpretierbar: \""+sb+"\"", ex);
            }  
            logger.info("ValidierungsRequest: \""+s+"\"");
            HttpClient client = new HttpClient();
            GetMethod get01 = new GetMethod(s);
            int httpCode01 = client.executeMethod(get01);
            final String result01 = get01.getResponseBodyAsString();
            if (httpCode01!=200 || result01==null || result01.trim().length()==0) {
                logger.error("Validierung konnte nicht durchgeführt werden. Der Server antwortete mit HTTP-Code "+ httpCode01 + " URL: \""+kvwmapUrl+"\". Antwort des Servers:\""+
                        result01 + "\"");
                throw new ValidationException("Validierung konnte nicht durchgeführt werden. Der Server antwortete mit HTTP-Code "+ httpCode01 + " URL: \""+kvwmapUrl+"\". Antwort des Servers:\""+
                        result01 + "\"", null);
            } else {
                JsonNode node = objectReader.readValue(result01, JsonNode.class);
                JsonNode n1 = node.get("success");
                if (n1 == null || !n1.asBoolean()) {
                    throw new ValidationException("Validierung war nicht erfolgreich. Antwort des Servers:\""+ result01 + "\"", null);
                } else {
                    logger.info("ErgebnisValidierungsRequest:\""+node+"\"");
                }
            }
            get01.releaseConnection();        
            sb = new StringBuilder(kvwmapUrl);
            sb.append("?go=Layer-Suche_Suchen");
            sb.append("&selected_layer_id=18");
            sb.append("&operator_konvertierung_id==");
            sb.append("&value_konvertierung_id=").append(bplan.getKonvertierungId());
            sb.append("&mime_type=formatter");
            sb.append("&login_name=").append(this.kvwmapLoginName);
            // sb.append("&Stelle_ID=").append(konvertierung.stelle_id);
            sb.append("&passwort=").append(this.kvwmapPassword);            
            sb.append("&format=json");
            try {
                s = parseUrl(sb.toString());
            } catch (MalformedURLException | URISyntaxException ex) {
                throw new ValidationException("Validierung konnte nicht durchgeführt werden. URL nicht interpretierbar: \""+sb+"\"", ex);
            } 
            logger.info("RequestValidierungsErgebnisse: \""+s+"\"");
            GetMethod get02 = new GetMethod(s);
            int httpCode02 = client.executeMethod(get02);
            if (httpCode02!=200) {
                logger.error(get02.getResponseBodyAsString());
                throw new ValidationException("Die Validierungsergebnisse konnten nicht abgerufen werden. Der Server antwortete mit HTTP-Code "+ httpCode02 + " URL: \""+kvwmapUrl+"\". Antwort des Servers:\""+
                        get01.getResponseBodyAsString() + "\"", null);
            }
            
            String json = get02.getResponseBodyAsString();
            logger.info(json);
            try {
                JsonNode node = objectReader.readValue(json, JsonNode.class);
                
                if (node.isArray()) {
//            System.out.println(node.size());
                    for (int i=0; i<node.size() && succedded; i++) {
                        JsonNode n = node.get(i);
                        JsonNode n1 = n.get("ergebnis_status");
                        if (n1 !=null) {
                            succedded = "Erfolg".equals(n1.asText());
                        } else {
                            throw new ValidationException("Das Valisierungsergebnis konnte nicht interpretiert werden. Response:\""+json+"\" request:"+sb.toString(), null);
                        }
                    }
                } else {
                    throw new ValidationException("Das Valisierungsergebnis konnte nicht interpretiert werden. Response:\""+json+"\"", null);
                }
            } catch (IOException e) {
               throw new ValidationException("Das Valisierungsergebnis konnte nicht interpretiert werden. Response:\""+json+"\"", e);
            }
            get02.releaseConnection();

            if (succedded) {
                importLogger.addLine(String.format("validated %s", bplan.getGml_id())); 
            } else {
                importLogger.addLine(String.format("Validierung war nicht erfolgreich %s", bplan.getGml_id())); 
            }
            return succedded;
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new ValidationException("Fehler bei Validierung durch den Backend.", ex);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            throw new ValidationException("unspezifischer Fehler bei Validierung.", ex);
        }
    }

    public void importWFS(Connection conWrite, Connection conRead, ImportConfigEntry entry, ImportLogger importLogger) throws Exception  {

        List<FPlan> fPlans = null;

        try {
            final String wfsUrl = entry.onlineresource + "?service=WFS&VERSION=1.1.0&REQUEST=GetFeature&TYPENAME=" + entry.featuretype + "&SRSNAME=epsg:25833";                 
            importLogger.addLine("Reading WFS (F-Plan): \""+ entry.onlineresource + "\"");
            fPlans = WFSClient.read(wfsUrl, wfsFactory, importLogger);
        } 
        catch (Exception ex) {
            logger.error("error reading from service " + entry.bezeichnung + " with url=\"" + entry.onlineresource +"\"", ex);
            importLogger.addError("ERROR - Reading WFS (F-Plan): \""+ entry.onlineresource + "\" error:["+ex.getMessage()+"]");                
        }

        updateFPlaene(conWrite, conRead, importLogger, entry, fPlans);

    }




    

   
    
    




    public static void print(String text, FPlan bPlan) {
        StringBuilder sb = new StringBuilder();
        sb.append(text).append('\n');
        sb.append('\t').append("[name=" + bPlan.getName() + ", gml_id=" + bPlan.getGml_id() + ", nummer=" + bPlan.getNummer() + ", planart="
                + bPlan.planart + ", rechtsstand=" + bPlan.rechtsstand + ", wirksamkeitsdatum=" + bPlan.wirksamkeitsdatum);
        sb.append("\n\tgemeinde=[");
        if (bPlan.gemeinde!=null && bPlan.gemeinde.length>0) {
            for (int i=0; i<bPlan.gemeinde.length; i++) {
                if (i>0) {
                    sb.append(", ");
                }
                sb.append(bPlan.gemeinde[i]);
            }
        }
        sb.append("]");
        sb.append("\n\t").append(bPlan.gemeinde[0].getValue());

        sb.append("\n\texternereferenz=[");
        PGSpezExterneReferenz[] pgExterneReferenzs = bPlan.getExternereferenzes();
        if (pgExterneReferenzs!=null) {
            for (int i = 0; i < pgExterneReferenzs.length; i++) {
                SpezExterneRef exRef = pgExterneReferenzs[i].object;
                sb.append("\n\t\tExterneRef [georefurl=" + exRef.georefurl + ", georefmimetype=" + exRef.georefmimetype + ", art=" + exRef.art
                        + ", informationssystemurl=" + exRef.informationssystemurl + ", referenzname=" + exRef.referenzname + ", referenzurl="
                        + exRef.referenzurl);
                sb.append("\n\t\treferenzmimetype=" + exRef.referenzmimetype);
                sb.append("\n\t\tbeschreibung=" + exRef.beschreibung + ", datum=" + exRef.datum + ", typ=" + exRef.typ + "]");

            }
        }
        sb.append("\n\tgeom=" + bPlan.getGeom() + "]");
        System.out.println(sb);
    }

    public static void runImport(List<? extends ImportConfigEntry> importConfigEntries, Connection conWrite, Connection conRead, String kvwmapUrl, String kvwmapLoginName, String kvwmapPassword) {

        try {         
            FPlanImporter bplImport;
            FPlanImporter bplNwmImport;
            if (BPlanImportStarter.isSqlTypeSupported(conWrite, "xp_spezexternereferenzauslegung")) {
                bplImport = new FPlanImporter("xplankonverter.konvertierungen", "xplan_gml.fp_plan", Version.v5_1n, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
                bplNwmImport = new FPlanNwmImporter("xplankonverter.konvertierungen", "xplan_gml.fp_plan", kvwmapUrl, kvwmapLoginName, kvwmapPassword );
            } else {
                bplImport = new FPlanImporter("xplankonverter.konvertierungen", "xplan_gml.fp_plan", Version.v5_1, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
                bplNwmImport = new FPlanNwmImporter("xplankonverter.konvertierungen", "xplan_gml.fp_plan", kvwmapUrl, kvwmapLoginName, kvwmapPassword );
            }

            LogDAO logDAO = new LogDAO(conWrite, "xplankonverter.import_protocol");

            for (int i = 0; i < importConfigEntries.size(); i++) {                
                ImportConfigEntry entry = importConfigEntries.get(i);
                System.err.println("StellenId: \""+entry.stelle_id+"\"");
                if (entry instanceof JobEntry) {
                    ConfigReader.setJobStarted(conWrite, (JobEntry)entry);
                }
                ImportLogger logger = new ImportLogger();
                if (entry.onlineresource.indexOf("nwm")>=0) {                    
                    bplNwmImport.importWFS(conWrite, conRead, entry, logger);
                } else {
                    bplImport.importWFS(conWrite, conRead, entry, logger);
                }                
                logDAO.insert(logger.getTime(), entry.stelle_id, logger.getText());
                
                List<String> errors = logger.getErrors();
                if (errors!=null && errors.size()>0) {
                    System.err.println(errors.get(i));
                }
                if (entry instanceof JobEntry) {
                    ConfigReader.setJobFinished(conWrite, (JobEntry)entry);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    
    public static void runImport(List<? extends ImportConfigEntry> importConfigEntries, Connection conWrite, Connection conRead, EMailSender eMailSender, String kvwmapUrl, String kvwmapLoginName, String kvwmapPassword) {

        try {                
            FPlanImporter bplImport;
            if (BPlanImportStarter.isSqlTypeSupported(conWrite, "xp_spezexternereferenzauslegung")) {
                bplImport = new FPlanImporter("xplankonverter.konvertierungen", "xplan_gml.fp_plan", Version.v5_1n, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
            } else {
                bplImport = new FPlanImporter("xplankonverter.konvertierungen", "xplan_gml.fp_plan", Version.v5_1, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
            }

            LogDAO logDAO = new LogDAO(conWrite, "xplankonverter.import_protocol");

            for (int i = 0; i < importConfigEntries.size(); i++) {                
                ImportConfigEntry entry = importConfigEntries.get(i);
                System.err.println("StellenId: \""+entry.stelle_id+"\"");
                if (entry instanceof JobEntry) {
                    ConfigReader.setJobStarted(conWrite, (JobEntry)entry);
                }
                ImportLogger logger = new ImportLogger();
                bplImport.importWFS(conWrite, conRead, entry, logger);
                logDAO.insert(logger.getTime(), entry.stelle_id, logger.getText());
                
                List<String> errors = logger.getErrors();
                if (errors!=null && errors.size()>0) {
                    sendErrors(errors, eMailSender, entry);
                }
                if (entry instanceof JobEntry) {
                    ConfigReader.setJobFinished(conWrite, (JobEntry)entry);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static void runImport(DBConnectionParameter dbParam, EMailSender eMailSender, String kvwmapUrl, String kvwmapLoginName, String kvwmapPassword) {
        Connection conWrite = null;
        Connection conRead = null;
        try {
            conWrite = BPlanImportStarter.getConnection(dbParam);
            conRead = BPlanImportStarter.getConnection(dbParam);

            try {
                List<ImportConfigEntry> importConfigEntries = ConfigReader.readConfig(conWrite, "xplankonverter.import_services");
                
                runImport(importConfigEntries, conWrite, conRead, eMailSender, kvwmapUrl, kvwmapLoginName, kvwmapPassword);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } 
        catch (Exception ex) {
            throw new IllegalArgumentException("DB-Verbinung konnte nicht hergestellt werden.", ex);
        } finally {
            if (conWrite!=null) {
                try {
                    conWrite.close();
                } catch (SQLException e) {}
            }
            if (conRead!=null) {
                try {
                    conRead.close();
                } catch (SQLException e) {}
            }
        }
    }

    private static void sendErrors(List<String> errors, EMailSender eMailSender, ImportConfigEntry entry) {
        StringBuilder sb = new StringBuilder();
        sb.append("Sehr geehrte Damen und Herren,<br>");
        sb.append("beim Importieren von BPlänen sind Fehler aufgetreten\n");
        for (int i=0; i<errors.size(); i++) {
            sb.append("<br><br>").append(errors.get(i));
        }
        sb.append("<br><br>");
        sb.append("Bei Fragen wenden Sie sich bitte an Herrn Trier<br>");
        sb.append("ralf.trier@gdi-service.de<br>");
        sb.append("Tel: 0381 87397363");
        
        try {
            String bezeichng = entry.bezeichnung==null ? entry.onlineresource : String.valueOf(entry.bezeichnung);
            eMailSender.sendEmail(sb.toString(), "Fehler beim Import von BPlänen - " + bezeichng, "ralf.trier@gdi-service.de");
            if ( entry.email != null && entry.email.contains("@") ) {
                eMailSender.sendEmail(sb.toString(), "Fehler beim Import von BPlänen", entry.email);
            }
        } catch (Throwable ex) {
           logger.error("Fehler beim Versenden der eMail", ex);
        }
        
    }    

    static class ValidationException extends Exception {

        private static final long serialVersionUID = 1L;

        public ValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }





}
