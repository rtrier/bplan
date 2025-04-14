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
import de.gdiservice.bplan.dao.*;
import de.gdiservice.bplan.konvertierung.Gemeinde;
import de.gdiservice.bplan.konvertierung.GemeindeDAO;
import de.gdiservice.bplan.konvertierung.Konvertierung;
import de.gdiservice.bplan.konvertierung.Konvertierung.KonvertierungStatus;
import de.gdiservice.bplan.konvertierung.KonvertierungDAO;
import de.gdiservice.bplan.poi.BPlan;
import de.gdiservice.bplan.poi.VerbundenerPlan.RechtscharakterPlanaenderung;
import de.gdiservice.util.DBConnectionParameter;
import de.gdiservice.util.EMailSender;
import de.gdiservice.wfs.BFitzBPlanFactory;
import de.gdiservice.wfs.BFitzBPlanFactoryV5_1;
import de.gdiservice.wfs.WFSClient;
import de.gdiservice.wfs.WFSFactory;



public class BPlanImporter implements XPPlanImporterI {

    final static Logger logger = LoggerFactory.getLogger(BPlanImporter.class);
    

    protected String bplanTable;
    
    protected String konvertierungTable;
    
    protected String kvwmapUrl;
    
    private String kvwmapLoginName;
    private String kvwmapPassword;

    WFSFactory<BPlan> wfsFactory;
    
    boolean test = false;

    public enum Version {
        v5_1,
        v5_1n,
        v5_3,
        v_Geoplex
    }

    public BPlanImporter(String konvertierungTable, String bplanTable, Version version, String kvwmapUrl, String kvwmapLoginName, String kvwmapPassword) {
        this.bplanTable = bplanTable;
        
        this.konvertierungTable = konvertierungTable;
        this.kvwmapUrl = kvwmapUrl;
        this.kvwmapLoginName = kvwmapLoginName;
        this.kvwmapPassword = kvwmapPassword;
        this.wfsFactory = getWFSFactory(version);
    }
    
    protected WFSFactory<BPlan> getWFSFactory(Version version) {
        WFSFactory<BPlan> wfsFactory = null;
        if (version==Version.v5_1) {
            wfsFactory = new BFitzBPlanFactoryV5_1(false);
        } else if (version==Version.v5_1n) {
            wfsFactory = new BFitzBPlanFactoryV5_1(true);
        } else {
            wfsFactory = new BFitzBPlanFactory();
        }
        return wfsFactory;
    }
    
    public void setTest(boolean isTest) {
        test = isTest;
    }
    
//    private boolean splitPlan(BPlan plan) {
//        String[] planArten = plan.getPlanart();
//        for (String s : planArten) {
//            if (planArten[0].charAt(0)=='1' ||  planArten[0].charAt(0)=='3') {
//                return true;
//            }
//        }
//        return false;
//    }

    public void updateBPlaene(Connection conWrite, Connection conRead, ImportLogger importLogger, ImportConfigEntry entry, List<BPlan> bPlans) throws SQLException  {
        conWrite.setAutoCommit(false);
        

        if (bPlans != null) {
            BPlanDAO bplanDao = new BPlanDAO(conWrite, conRead, bplanTable);
            KonvertierungDAO konvertierungDAO = new KonvertierungDAO(conWrite, konvertierungTable);

            for (BPlan plan : bPlans) {        
                logger.info("Verarbeite: {} {}", plan.getGml_id(), plan.getName());
                try {
                    if (!isStelleResponsible(entry.stelle_id, plan)) {
                        throw new IllegalAccessException("Stelle mit Id \""+entry.stelle_id+"\" ist nicht für die Gemeinde " + Arrays.toString(plan.getGemeinde()) + " zuständig");
                    }
                    
                    // List<BPlan> teilPlaene = splitPlan(plan) ? BPlanGroup.split(plan) : Collections.singletonList(plan);
                    List<BPlan> teilPlaene = BPlanGroup.split(plan);

                    String geomValidierungsResult = bplanDao.validateGeom(plan.getGeom());
                    if ("Valid Geometry".equals(geomValidierungsResult)) {

                        List<BPlan> listDBPlaene = bplanDao.findByInternalIdLikeGmlId(plan.getGml_id());
                        logger.info("findByInternalIdLikeGmlId Found "+listDBPlaene.size()+" Pläne");                        
                        /**
                         * Setzen der Felder aendert und wurdegeaendertvon
                         */
                        BPlan previousPlan = null;
                        for (int teilPlanNr=0; teilPlanNr<teilPlaene.size(); teilPlanNr++) {
                            BPlan teilPlan = teilPlaene.get(teilPlanNr);
                            String sTeilPlanNr = (teilPlanNr<10 ? "0"+teilPlanNr : String.valueOf(teilPlanNr));
                            teilPlan.setInternalId(plan.getGml_id()+"-"+sTeilPlanNr);
                            
                            BPlan dbPlan = (listDBPlaene.size()>teilPlanNr) ? listDBPlaene.get(teilPlanNr) : null;
                            if (teilPlanNr == 0 && listDBPlaene.size()>0) {
                                teilPlan.setGml_id(listDBPlaene.get(0).getGml_id());
                            }
                            if (teilPlanNr>0) {
                                UUID teilPlanUUID = (teilPlanNr<listDBPlaene.size()) ? listDBPlaene.get(teilPlanNr).getGml_id() : UUID.randomUUID();
                                teilPlan.setGml_id(teilPlanUUID);
//                                teilPlan.name = teilPlan.name + " " + String.valueOf(teilPlanNr) + ". Änderung";
                                VerbundenerPlan aendert = new VerbundenerPlan(plan.getName(), RechtscharakterPlanaenderung.Aenderung, plan.getNummer(), previousPlan.getGml_id().toString());
                                teilPlan.setAendert(aendert);
                                VerbundenerPlan wurdegeaendertvon =  new VerbundenerPlan(plan.getName(), RechtscharakterPlanaenderung.Aenderung, plan.getNummer(), teilPlan.getGml_id().toString());                                
                                previousPlan.setWurdeGeaendertVon(wurdegeaendertvon);
                            }
                            if (dbPlan != null) {
                                logger.info(teilPlanNr+"------ "+teilPlan.getGml_id()+"  "+ dbPlan.getGml_id()+" "+teilPlan.getName() +" " +dbPlan.getName());
                            }
                            previousPlan = teilPlan; 
                            
                        }
                        
                        
//                        for (int teilPlanNr=0; teilPlanNr<listDBPlaene.size(); teilPlanNr++) {
//                            BPlan dbPlan = (listDBPlaene.size()>teilPlanNr) ? listDBPlaene.get(teilPlanNr) : null;
//                            String aendert = (dbPlan.getAendert()==null ? "null" : dbPlan.getAendert()[0].getVerbundenerPlan().getVerbundenerplan());                
//                            String wurdegeaendert = (dbPlan.getWurdeGeaendertVon()==null ? "null" : dbPlan.getWurdeGeaendertVon()[0].getVerbundenerPlan().getVerbundenerplan());                
//                            logger.info(teilPlanNr+"------ "+dbPlan.getGml_id()+"\t"+ aendert+"\t"+wurdegeaendert);    
//                        }
                        
                        
                        GemeindeDAO gemeindeDAO = new GemeindeDAO(conRead);
                        
                        for (int teilPlanNr=0; teilPlanNr<teilPlaene.size(); teilPlanNr++) {
//                        for (int teilPlanNr=1000; teilPlanNr<teilPlaene.size(); teilPlanNr++) {
                            BPlan teilPlan = teilPlaene.get(teilPlanNr);
                            BPlan dbPlan = (listDBPlaene.size()>teilPlanNr) ? listDBPlaene.get(teilPlanNr) : null;
                            logger.info("Teilplaene: {} {}/{} {} {}", plan.getGml_id(), (teilPlanNr+1), teilPlaene.size(), teilPlan.getName(), dbPlan==null ? "null" : dbPlan.getName());
                            Konvertierung konvertierung;
                            if (dbPlan == null) {
                                // neuer BPlan                                
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
                                    if (teilPlan.getPlanart()!=null && teilPlan.getPlanart().length>0) {
                                        iPlanArt = Integer.parseInt(teilPlan.getPlanart()[0]);
                                    } else {
                                        throw new IllegalArgumentException("WFS enthält keine Planart.");
                                    }
                                } catch (NumberFormatException e) {
                                    throw new IllegalArgumentException("Planart für gmlId=\""+teilPlan.getGml_id()+"\" \""+teilPlan.getPlanart()[0]+"\" ist nicht gültig.");
                                }                    
                                BPlan.PlanArt planArt = BPlan.PlanArt.get(iPlanArt);
                                if (planArt==null) {
                                    throw new IllegalArgumentException("Planart für gmlId=\""+teilPlan.getGml_id()+"\" \""+planArt+"\" ist im System nicht bekannt.");
                                }
                                konvertierung.planart = "BP-Plan";
                                konvertierung.epsg = Konvertierung.EPSGCodes.EPSG_25833;
                                konvertierung.input_epsg = Konvertierung.EPSGCodes.EPSG_25833;
                                konvertierung.output_epsg = Konvertierung.EPSGCodes.EPSG_25833;
                                konvertierung.geom_precision =3;
    
                                Konvertierung dbKonvertierung = konvertierungDAO.insert(konvertierung);                    
                                teilPlan.setKonvertierungId(dbKonvertierung.id);
                                bplanDao.insert(teilPlan);
                                logger.debug("inserted "+teilPlan.getGml_id()+" konvertierungsId="+plan.getKonvertierungId());
                                conWrite.commit();
                                
                                boolean succeded = validate(konvertierung, teilPlan, kvwmapUrl, importLogger);
                                // konvertierungDAO.updatePublishFlag(konvertierung.id, succeded);
                                if (succeded) {
                                    boolean isLastPlan = (teilPlanNr == teilPlaene.size()-1);                                    
                                    if (isLastPlan && teilPlan.auslegungsstartdatum!=null && teilPlan.auslegungsstartdatum.length>0) {
                                        konvertierungDAO.updatePublishDate(konvertierung.id, teilPlan.auslegungsstartdatum[teilPlan.auslegungsstartdatum.length-1]);      
                                        conWrite.commit();
                                    } else {
                                        if (teilPlan.inkrafttretensdatum!=null) {
                                            konvertierungDAO.updatePublishDate(konvertierung.id, teilPlan.inkrafttretensdatum);
                                            conWrite.commit();
                                        }
                                    }
                                }
                                logger.info("BPLanImporter: Plan gmlId=\""+teilPlan.getGml_id()+"\" inserted.");
                                importLogger.addLine(String.format("inserted %s", teilPlan.getGml_id()));
    
                            } else {
                                // update BPlan   
                                logger.debug("Plan "+plan.getGml_id()+" dbPlan "+dbPlan.getGml_id()+ " mit KonvertierungId="+dbPlan.getKonvertierungId());
                                if (HasChangedFunctions.hasChanged(teilPlan, dbPlan)) {
                                    logger.debug("update plan");
                                    teilPlan.setKonvertierungId(dbPlan.getKonvertierungId());
                                    // setze die Orginal
                                    teilPlan.setKonvertierungId(dbPlan.getKonvertierungId());
                                    bplanDao.update(teilPlan);
                                    int updateCount = konvertierungDAO.update(teilPlan.getKonvertierungId());
                                    if (updateCount == 0) {
                                        throw new IllegalArgumentException("In der DB existiert ein BPlan mit der gmlId. Der zugehörige Eintrag in der Konvertierungs-Tabelle existiert aber nicht.");
                                    }
                                    konvertierungDAO.updatePublishFlag(teilPlan.getKonvertierungId(), false);
                                    konvertierung = konvertierungDAO.find(teilPlan.getKonvertierungId()); 
                                    
                                    logger.info("BPLanImporter: Plan gmlId=\""+teilPlan.getGml_id()+"\" updated.");
                                    importLogger.addLine(String.format("updated %s", teilPlan.getGml_id()));
                                    conWrite.commit();
                                    
                                    boolean succeded = validate(konvertierung, teilPlan, kvwmapUrl, importLogger);
//                                    konvertierungDAO.updatePublishFlag(konvertierung.id, succeded);
                                    if (teilPlan.inkrafttretensdatum!=null && succeded) {
                                        konvertierungDAO.updatePublishDate(konvertierung.id, teilPlan.inkrafttretensdatum);
                                        conWrite.commit();
                                    }
                     
                                } else {
                                    logger.info("BPLanImporter: Plan gmlId=\""+teilPlan.getGml_id()+"\" unchanged.");
                                    importLogger.addLine(String.format("unchanged %s", teilPlan.getGml_id()));
                                }
    
                            }
                        }
                    } else {
                        logger.info("BPLanImporter: Plan gmlId=\""+plan.getGml_id()+"\" Geometry is not valid: "+ geomValidierungsResult +".");
                        importLogger.addError("BPLanImporter: Plan gmlId=\""+plan.getGml_id()+"\" Geometry is not valid: "+ geomValidierungsResult +".");
                    }
                } catch (Exception ex) {                    
                    try {
                        conWrite.rollback();
                    } 
                    catch (SQLException e) {
                        logger.error("rollback Error", e);
                    }
                    importLogger.addError("error updating BPlan [gmlId="+ plan.getGml_id() +" name=\""+ plan.getName() +"\"] from service \"" + entry.bezeichnung + "\" with url=\"" + entry.onlineresource +"\" error:["+ex.getMessage()+"]");
                    logger.error("error updating BPlan [gmlId="+ plan.getGml_id() +" name=\""+ plan.getName() +"\"] from service \"" + entry.bezeichnung + "\" with url=\"" + entry.onlineresource +"\"", ex);
                } 
            }
        }
        
    }
    
    /**
     * Prüft, ob die Stelle mit der stellenId für die Gemeinde zuständig ist.
     *  
     * @param stelle_id
     * @param plan
     * @return
     */
    private boolean isStelleResponsible(Integer stelle_id, BPlan plan) {
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
    
    public boolean validate(Konvertierung konvertierung, BPlan bplan, String kvwmapUrl, ImportLogger importLogger) throws ValidationException {
        
        try {
            boolean succedded = true; 
            ObjectReader objectReader = new ObjectMapper().reader();
            
            if (konvertierung==null) {
                throw new RuntimeException("Validation: konvertierung was null");
            }
            if (bplan==null) {
                throw new RuntimeException("Validation: bplan was null");
            }
            
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
                throw new ValidationException("Validierung konnte nicht durchgeführt werden. Der Server antwortete mit HTTP-Code "+ httpCode01 + " Antwort des Servers:\""+
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
            if (json == null) {
                throw new ValidationException("Die Validierungsergebnisse konnten nicht abgerufen werden. Die Antwort vom Server enthielt keine Daten URL: \""+kvwmapUrl+"\"", null);                
            }
            try {
                JsonNode node = objectReader.readValue(json, JsonNode.class);
                logger.info("Validierungsergebnisse:\""+node+"\"");
                
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

//    public void importGeoplexWFS(Connection con, ImportConfigEntry entry, ImportLogger importLogger) throws Exception  {
//        
//        List<BPlan> bPlans = null;        
//        try {
//            final String wfsUrl = entry.onlineresource + "?service=WFS&VERSION=2.0.0&REQUEST=GetFeature&TYPENAME=" + entry.featuretype + "&SRSNAME=epsg:25833";
////            &COUNT=1200";
//            importLogger.addLine("Reading WFS: \""+ entry.onlineresource + "\"");
//            bPlans = WFSClient.read(wfsUrl, wfsFactory, importLogger);
//        } 
//        catch (Exception ex) {
//            logger.error("error reading from service " + entry.bezeichnung + " with url=\"" + entry.onlineresource +"\"", ex);
//            importLogger.addError("ERROR - Reading WFS: \""+ entry.onlineresource + "\" error:["+ex.getMessage()+"]");                
//        }        
//        for (int i=0; i<bPlans.size(); i++) {
//            BPlan plan = bPlans.get(i);
//            System.err.println("-----------------plan-------" + i + "-----------------------");
//            BPlan.print(plan);
//        }
//        
//        
//        
//
////        updateBPlaene(con, importLogger, entry, bPlans);
//        
//    }
    public void importWFS(Connection conWrite, Connection conRead, ImportConfigEntry entry, ImportLogger importLogger) throws Exception  {

        List<BPlan> bPlans = null;        
        try {
            String sVersion = (entry.onlineresource.indexOf("nwm")>=0) ? "2.0.0" : "1.1.0";
            
//            final String wfsUrl = entry.onlineresource + "?service=WFS&VERSION="+sVersion+"&REQUEST=GetFeature&TYPENAME=" + entry.featuretype + "&SRSNAME=epsg:25833";
            final String wfsUrl = entry.onlineresource + "?service=WFS&VERSION="+sVersion+"&REQUEST=GetFeature&TYPENAME=" + entry.featuretype + "&SRSNAME=epsg:25833&COUNT=50";
            
            importLogger.addLine("Reading WFS: \""+ entry.onlineresource + "\"");
            bPlans = WFSClient.read(wfsUrl, wfsFactory, importLogger);
            updateBPlaene(conWrite, conRead, importLogger, entry, bPlans);
        } 
        catch (Exception ex) {
            logger.error("error reading from service " + entry.bezeichnung + " with url=\"" + entry.onlineresource +"\"", ex);
            importLogger.addError("ERROR - Reading WFS: \""+ entry.onlineresource + "\" error:["+ex.getMessage()+"]");                
        }


    }


//    private static boolean hasChanged(Object[] o1, Object[] o2) {
//        if (o1==null) {
//            if (o2!=null) {
//                return true;
//            }
//            return false;
//        }
//        return !Arrays.equals(o1, o2);
//    }
    
    
    
//    private static boolean hasChanged(CodeList o1, CodeList o2) {
//        if (o1==null) {
//            if (o2!=null) {
//                return true;
//            }
//            return false;
//        }
//        CodeList cl01 = (CodeList)o1;
//        CodeList cl02 = (CodeList)o2;
//        if (hasChanged(cl01.getCodespace(), o2.getCodespace())) {
//            return true;
//        }
//        return (hasChanged(cl01.getCodelistValue(), cl02.getCodelistValue()));
//    }
    
//    private static boolean hasChanged(Object o1, Object o2) {
//        if (o1==null) {
//            if (o2!=null) {
//                return true;
//            }
//            return false;
//        }
//        return !o1.equals(o2);
//    }
    
//    public static boolean hasChanged(PGVerbundenerPlan[] a01, PGVerbundenerPlan[] a02) {
//        if (a01 == null) {
//            return (a02 == null) ? false : true;
//        }
//        if (a02 == null) {
//            return true;
//        }
//        if (a01.length != a02.length) {
//            return true;
//        }
//        for (int i=0; i<a01.length; i++) {
//
//            VerbundenerPlan vb01 = a01[i].getVerbundenerPlan();
//            VerbundenerPlan vb02 = a02[i].getVerbundenerPlan();
//            if (hasChanged(vb01.getPlanname(), vb02.getPlanname())) {
//                return true;
//            }
//            if (hasChanged(vb01.getNummer(), vb02.getNummer())) {
//                return true;
//            }
//            if (hasChanged(vb01.getRechtscharakter(), vb02.getRechtscharakter())) {
//                return true;
//            }
//            if (hasChanged(vb01.getVerbundenerplan(), vb02.getVerbundenerplan())) {
//                return true;
//            }
//        }
//        return false;
//    }
    
    
//    public static boolean hasChanged(
//            de.gdiservice.bplan.poi.Gemeinde[] gemeinden01, 
//            de.gdiservice.bplan.poi.Gemeinde[] gemeinden02) {
//        if (gemeinden01 == null && gemeinden02 == null) {
//            return false;
//        }
//        if (gemeinden01 == null || gemeinden02 == null) {
//            return true;                                                                                                                                                 
//        }
//        if (gemeinden01.length != gemeinden02.length) {
//            return true;
//        }
//        for (int i=0; i<gemeinden01.length; i++) {
//            if (!gemeinden01[i].equals(gemeinden02[i])) {
//                return true;
//            }
//        }
//        return false;
//    }
    
//    private static String toString(de.gdiservice.bplan.poi.Gemeinde[] gemeinden) {
//        StringBuilder sb = new StringBuilder();
//        if (gemeinden != null) {
//            for (int i=0; i<gemeinden.length; i++) {
//                sb.append("\n\ttoString[\"").append(gemeinden[i]).append("\"\n\t");
//                sb.append("toStrin2[ags=").append(gemeinden[i].ags).append(" rs=").append(gemeinden[i].rs);
//                sb.append(" gemeindename=\"").append(gemeinden[i].gemeindename).append("\"");
//                sb.append(" ortsteilname=\"").append(gemeinden[i].ortsteilname).append("\"");
//            }
//        } else {
//            sb.append("null");
//        }
//        return sb.toString();
//    }

//    public static boolean hasChanged(BPlan plan, BPlan dbPlan) {
//
//        
//
//        if (hasChanged(plan.gemeinde, dbPlan.gemeinde)) {
//            logger.info(String.format("<>gemeinde %s %s", toString(plan.gemeinde), toString(dbPlan.gemeinde)));
//            return true;
//        }
//        if (hasChanged(plan.getExternereferenzes(), dbPlan.getExternereferenzes())) {
//            logger.info(String.format("<>Externereferenzes %s %s", plan.getExternereferenzes(), dbPlan.getExternereferenzes()));
//            return true;
//        }
//
//        if (plan.geom == null || dbPlan.geom == null) {
//            throw new IllegalArgumentException("one Plan doesnt has a geometry");
//        }
//        if (!plan.geom.equals(dbPlan.geom)) {
//            logger.info(String.format("<>geom %s %s", plan.geom, dbPlan.geom));
//            return true;
//        }
//        if (hasChanged(plan.inkrafttretensdatum, dbPlan.inkrafttretensdatum)) {
//            logger.info(String.format("<>inkrafttretensdatum %s %s", plan.inkrafttretensdatum, dbPlan.inkrafttretensdatum));
//            return true;
//        }
//        if (hasChanged(plan.auslegungsstartdatum, dbPlan.auslegungsstartdatum)) {
//            logger.info(String.format("<>auslegungsstartdatum %s %s", plan.auslegungsstartdatum, dbPlan.auslegungsstartdatum));
//            return true;
//        }
//        if (hasChanged(plan.auslegungsenddatum, dbPlan.auslegungsenddatum)) {
//            logger.info(String.format("<>auslegungsenddatum %s %s", plan.auslegungsenddatum, dbPlan.auslegungsenddatum));
//            return true;
//        }  
//        if (hasChanged(plan.name, dbPlan.name)) {
//            logger.info(String.format("<>name %s %s", plan.name, dbPlan.name));
//            return true;
//        }
//        if (hasChanged(plan.nummer, dbPlan.nummer)) {
//            logger.info(String.format("<>nummer %s %s", plan.nummer, dbPlan.nummer));
//            return true;
//        }
//        if (!Arrays.equals(plan.planart, dbPlan.planart)) {
//            logger.info(String.format("<>planart %s %s", plan.planart, dbPlan.planart));
//            return true;
//        }
//        if (hasChanged(plan.rechtsstand, dbPlan.rechtsstand)) {
//            logger.info(String.format("<>rechtsstand %s %s", plan.rechtsstand, dbPlan.rechtsstand));
//            return true;
//        }
//        
//        if (hasChanged(plan.aendert, dbPlan.aendert)) {
//            logger.info(String.format("<>aendert %s %s", plan.aendert, dbPlan.aendert));
//            return true;
//        }
//        if (hasChanged(plan.wurdegeaendertvon, dbPlan.wurdegeaendertvon)) {
//            logger.info(String.format("<>wurdegeaendertvon %s %s", plan.wurdegeaendertvon, dbPlan.wurdegeaendertvon));
//            return true;
//        }
//        if (hasChanged(plan.internalid, dbPlan.internalid)) {
//            logger.info(String.format("<>internalid %s %s", plan.internalid, dbPlan.internalid));
//            return true;
//        }
////        "status",
//        if (hasChanged(plan.status, dbPlan.status)) {
//            logger.info(String.format("<>status %s %s", plan.status, dbPlan.status));
//            return true;
//        }
////        "verfahren",
//        if (hasChanged(plan.verfahren, dbPlan.verfahren)) {
//            logger.info(String.format("<>verfahren %s %s", plan.verfahren, dbPlan.verfahren));
//            return true;
//        }
////        "untergangsdatum",
//        if (hasChanged(plan.untergangsdatum, dbPlan.untergangsdatum)) {
//            logger.info(String.format("<>untergangsdatum %s %s", plan.untergangsdatum, dbPlan.untergangsdatum));
//            return true;
//        }
////        "genehmigungsdatum",
//        if (hasChanged(plan.genehmigungsdatum, dbPlan.genehmigungsdatum)) {
//            logger.info(String.format("<>genehmigungsdatum %s %s", plan.genehmigungsdatum, dbPlan.genehmigungsdatum));
//            return true;
//        }
////        "gruenordnungsplan",
//        if (hasChanged(plan.gruenordnungsplan, dbPlan.gruenordnungsplan)) {
//            logger.info(String.format("<>gruenordnungsplan %s %s", plan.gruenordnungsplan, dbPlan.gruenordnungsplan));
//            return true;
//        }
////        "ausfertigungsdatum",
//        if (hasChanged(plan.ausfertigungsdatum, dbPlan.ausfertigungsdatum)) {
//            logger.info(String.format("<>ausfertigungsdatum %s %s", plan.ausfertigungsdatum, dbPlan.ausfertigungsdatum));
//            return true;
//        }        
////        "durchfuehrungsvertrag",
//        if (hasChanged(plan.durchfuehrungsvertrag, dbPlan.durchfuehrungsvertrag)) {
//            logger.info(String.format("<>durchfuehrungsvertrag %s %s", plan.durchfuehrungsvertrag, dbPlan.durchfuehrungsvertrag));
//            return true;
//        }         
////        "erschliessungsvertrag",
//        if (hasChanged(plan.erschliessungsvertrag, dbPlan.erschliessungsvertrag)) {
//            logger.info(String.format("<>erschliessungsvertrag %s %s", plan.erschliessungsvertrag, dbPlan.erschliessungsvertrag));
//            return true;
//        }        
////        "rechtsverordnungsdatum",
//        if (hasChanged(plan.rechtsverordnungsdatum, dbPlan.rechtsverordnungsdatum)) {
//            logger.info(String.format("<>rechtsverordnungsdatum %s %s", plan.rechtsverordnungsdatum, dbPlan.rechtsverordnungsdatum));
//            return true;
//        }              
////        "satzungsbeschlussdatum",
//        if (hasChanged(plan.satzungsbeschlussdatum, dbPlan.satzungsbeschlussdatum)) {
//            logger.info(String.format("<>satzungsbeschlussdatum %s %s", plan.satzungsbeschlussdatum, dbPlan.satzungsbeschlussdatum));
//            return true;
//        }         
////        "staedtebaulichervertrag",
//        if (hasChanged(plan.staedtebaulichervertrag, dbPlan.staedtebaulichervertrag)) {
//            logger.info(String.format("<>staedtebaulichervertrag %s %s", plan.staedtebaulichervertrag, dbPlan.staedtebaulichervertrag));
//            return true;
//        }
//////        "veroeffentlichungsdatum",
////        "planaufstellendegemeinde",
//        if (hasChanged(plan.planaufstellendegemeinde, dbPlan.planaufstellendegemeinde)) {
//            logger.info(String.format("<>planaufstellendegemeinde %s %s", plan.planaufstellendegemeinde, dbPlan.planaufstellendegemeinde));
//            return true;
//        }
////        "veraenderungssperredatum",
//        if (hasChanged(plan.veraenderungssperredatum, dbPlan.veraenderungssperredatum)) {
//            logger.info(String.format("<>veraenderungssperredatum %s %s", plan.veraenderungssperredatum, dbPlan.veraenderungssperredatum));
//            return true;
//        }
////        "aufstellungsbeschlussdatum",
//        if (hasChanged(plan.aufstellungsbeschlussdatum, dbPlan.aufstellungsbeschlussdatum)) {
//            logger.info(String.format("<>aufstellungsbeschlussdatum %s %s", plan.aufstellungsbeschlussdatum, dbPlan.aufstellungsbeschlussdatum));
//            return true;
//        }
////        "traegerbeteiligungsenddatum",
//        if (hasChanged(plan.traegerbeteiligungsenddatum, dbPlan.traegerbeteiligungsenddatum)) {
//            logger.info(String.format("<>traegerbeteiligungsenddatum %s %s", plan.traegerbeteiligungsenddatum, dbPlan.traegerbeteiligungsenddatum));
//            return true;
//        }
////        "veraenderungssperreenddatum",
//        if (hasChanged(plan.veraenderungssperreenddatum, dbPlan.veraenderungssperreenddatum)) {
//            logger.info(String.format("<>veraenderungssperreenddatum %s %s", plan.veraenderungssperreenddatum, dbPlan.veraenderungssperreenddatum));
//            return true;
//        }
////        "traegerbeteiligungsstartdatum",
//        if (hasChanged(plan.traegerbeteiligungsstartdatum, dbPlan.traegerbeteiligungsstartdatum)) {
//            logger.info(String.format("<>traegerbeteiligungsstartdatum %s %s", plan.traegerbeteiligungsstartdatum, dbPlan.traegerbeteiligungsstartdatum));
//            return true;
//        }
////        "verlaengerungveraenderungssperre",
//        if (hasChanged(plan.verlaengerungveraenderungssperre, dbPlan.verlaengerungveraenderungssperre)) {
//            logger.info(String.format("<>verlaengerungveraenderungssperre %s %s", plan.verlaengerungveraenderungssperre, dbPlan.verlaengerungveraenderungssperre));
//            return true;
//        }
////        "veraenderungssperrebeschlussdatum",
//        if (hasChanged(plan.veraenderungssperrebeschlussdatum, dbPlan.veraenderungssperrebeschlussdatum)) {
//            logger.info(String.format("<>veraenderungssperrebeschlussdatum %s %s", plan.veraenderungssperrebeschlussdatum, dbPlan.veraenderungssperrebeschlussdatum));
//            return true;
//        }
//        return false;
//    }


//    public static void print(String text, BPlan bPlan) {
//        StringBuilder sb = new StringBuilder();
//        sb.append(text).append('\n');
//        sb.append('\t').append("[id=" + bPlan.id + ", name=" + bPlan.name + ", gml_id=" + bPlan.gml_id + ", nummer=" + bPlan.nummer + ", planart="
//                + Arrays.toString(bPlan.planart) + ", rechtsstand=" + bPlan.rechtsstand + ", inkrafttretensdatum=" + bPlan.inkrafttretensdatum);
//        sb.append("\n\tgemeinde=[");
//        if (bPlan.gemeinde!=null && bPlan.gemeinde.length>0) {
//            for (int i=0; i<bPlan.gemeinde.length; i++) {
//                if (i>0) {
//                    sb.append(", ");
//                }
//                sb.append(bPlan.gemeinde[i]);
//            }
//        }
//        sb.append("]");
//        sb.append("\n\t").append(bPlan.gemeinde[0].getValue());
//
//        sb.append("\n\texternereferenz=[");
//        PGSpezExterneReferenz[] pgExterneReferenzs = bPlan.externeReferenzes;
//        if (pgExterneReferenzs!=null) {
//            for (int i = 0; i < pgExterneReferenzs.length; i++) {
//                SpezExterneRef exRef = pgExterneReferenzs[i].object;
//                sb.append("\n\t\tExterneRef [georefurl=" + exRef.georefurl + ", georefmimetype=" + exRef.georefmimetype + ", art=" + exRef.art
//                        + ", informationssystemurl=" + exRef.informationssystemurl + ", referenzname=" + exRef.referenzname + ", referenzurl="
//                        + exRef.referenzurl);
//                sb.append("\n\t\treferenzmimetype=" + exRef.referenzmimetype);
//                sb.append("\n\t\tbeschreibung=" + exRef.beschreibung + ", datum=" + exRef.datum + ", typ=" + exRef.typ + "]");
//
//            }
//        }
//        sb.append("\n\tgeom=" + bPlan.geom.getGeometryType() + "]");
////        System.out.println(sb);
//        logger.debug(sb.toString());
//    }

//    public static void runImport(List<? extends ImportConfigEntry> importConfigEntries, Connection con, String kvwmapUrl, String kvwmapLoginName, String kvwmapPassword) {
//
//        try {                
//            
//            BPlanImporter bplImport = null;
//            FPlanImporter fplImport = null;
//            LogDAO logDAO = new LogDAO(con, "xplankonverter.import_protocol");
//
//            for (int i = 0; i < importConfigEntries.size(); i++) {                
//                ImportConfigEntry entry = importConfigEntries.get(i);
//                System.err.println("StellenId: \""+entry.stelle_id+"\"");
//                if (entry instanceof JobEntry) {
//                    ConfigReader.setJobStarted(con, (JobEntry)entry);
//                }
//                ImportLogger logger = new ImportLogger();
//                if ("B_PLAN".equalsIgnoreCase(entry.featuretype)) {
//                    if (bplImport==null) {
//                        if (BPlanImportStarter.isSqlTypeSupported(con, "xp_spezexternereferenzauslegung")) {
//                            bplImport = new BPlanImporter("xplankonverter.konvertierungen", "xplan_gml.bp_plan", BPlanImporter.Version.v5_1n, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
//                        } else {
//                            bplImport = new BPlanImporter("xplankonverter.konvertierungen", "xplan_gml.bp_plan", BPlanImporter.Version.v5_1, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
//                        }
//                    }
//                    bplImport.importWFS(con, entry, logger);
//                } else {
//                    if ("F_PLAN".equalsIgnoreCase(entry.featuretype)) {
//                        if (fplImport==null) {
//                            if (BPlanImportStarter.isSqlTypeSupported(con, "xp_spezexternereferenzauslegung")) {
//                                fplImport = new FPlanImporter("xplankonverter.konvertierungen", "xplan_gml.fp_plan", FPlanImporter.Version.v5_1n, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
//                            } else {
//                                fplImport = new FPlanImporter("xplankonverter.konvertierungen", "xplan_gml.fp_plan", FPlanImporter.Version.v5_1, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
//                            }
//                        }
//                        fplImport.importWFS(con, entry, logger);
//                    } else {
//                        logger.addError("Featuretype \"" + entry.featuretype + "\" wird nicht unterstützt. Unterstützt werden erden nur B_PLAN und F_PLAN");
//                    }
//                }                
//                logDAO.insert(logger.getTime(), entry.stelle_id, logger.getText());
//                
//                List<String> errors = logger.getErrors();
//                if (errors!=null && errors.size()>0) {
//                    System.err.println(errors.get(i));
//                }
//                if (entry instanceof JobEntry) {
//                    ConfigReader.setJobFinished(con, (JobEntry)entry);
//                }
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//    }
    
//    public static void runImportGeoplex(List<? extends ImportConfigEntry> importConfigEntries, Connection conRead, Connection conWrite, EMailSender eMailSender, String kvwmapUrl, String kvwmapLoginName, String kvwmapPassword) {
//
//        try {
//            BPlanImporter bplImport;
//            FPlanImporter fplImport;
//            SOPlanImporter soplImport;
//            if (BPlanImportStarter.isSqlTypeSupported(conRead, "xp_spezexternereferenzauslegung")) {
//                bplImport = new BPlanImporter("xplankonverter.konvertierungen", "xplan_gml.bp_plan", BPlanImporter.Version.v_Geoplex, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
//                fplImport = new FPlanImporter("xplankonverter.konvertierungen", "xplan_gml.fp_plan", FPlanImporter.Version.v5_1n, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
//                soplImport = new SOPlanImporter("xplankonverter.konvertierungen", "xplan_gml.so_plan", SOPlanImporter.Version.v5_1n, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
//            } else {
//                bplImport= new BPlanImporter("xplankonverter.konvertierungen", "xplan_gml.bp_plan", BPlanImporter.Version.v5_1, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
//                fplImport = new FPlanImporter("xplankonverter.konvertierungen", "xplan_gml.fp_plan", FPlanImporter.Version.v5_1, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
//                soplImport = new SOPlanImporter("xplankonverter.konvertierungen", "xplan_gml.so_plan", SOPlanImporter.Version.v5_1, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
//            }
//
//            LogDAO logDAO = new LogDAO(conWrite, "xplankonverter.import_protocol");
//
//            for (int i = 0; i < importConfigEntries.size(); i++) {                
//                ImportConfigEntry entry = importConfigEntries.get(i);
//                System.err.println("StellenId: \""+entry.stelle_id+"\"");
//                if (entry instanceof JobEntry) {
//                    ConfigReader.setJobStarted(conRead, (JobEntry)entry);
//                }
//                ImportLogger logger = new ImportLogger();
//                if ("B_PLAN".equalsIgnoreCase(entry.featuretype)) {
//                    bplImport.importGeoplexWFS(conRead, entry, logger);
//                } else if ("F_PLAN".equalsIgnoreCase(entry.featuretype)) {
//                    fplImport.importWFS(conRead, entry, logger);
//                } else if ("SO_PLAN".equalsIgnoreCase(entry.featuretype)) {
//                    soplImport.importWFS(con, entry, logger);                    
//                } else {
//                    logger.addError("Featuretype \"" + entry.featuretype + "\" wird nicht unterstützt. Unterstützt werden nur B_PLAN, F_PLAN und SO_PLAN");
//                }
//                logDAO.insert(logger.getTime(), entry.stelle_id, logger.getText());
//                
//                List<String> errors = logger.getErrors();
//                if (errors!=null && errors.size()>0) {
//                    sendErrors(errors, eMailSender, entry);
//                }
//                if (entry instanceof JobEntry) {
//                    ConfigReader.setJobFinished(con, (JobEntry)entry);
//                }
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//    }    
    
    
    public static XPPlanImporterI getImporter(ImportConfigEntry entry, String kvwmapUrl, String kvwmapLoginName, String kvwmapPassword) {
        XPPlanImporterI planImporter = null;
        
        String tableKonvertierung = entry.testtable_prefix == null ? "xplankonverter.konvertierungen" : "xplankonverter."+entry.testtable_prefix+"konvertierungen";
        String tableBPlan = entry.testtable_prefix == null ? "xplan_gml.bp_plan" : "xplan_gml."+entry.testtable_prefix+"bp_plan";
        String tableBPBereich = entry.testtable_prefix == null ? "xplan_gml.bp_bereich" : "xplan_gml."+entry.testtable_prefix+"bp_bereich";
        String tableFPlan = entry.testtable_prefix == null ? "xplan_gml.fp_plan" : "xplan_gml."+entry.testtable_prefix+"fp_plan";
        String tableSOPlan = entry.testtable_prefix == null ? "xplan_gml.so_plan" : "xplan_gml."+entry.testtable_prefix+"so_plan";
        
        if (entry.onlineresource.indexOf("nwm")>=0) {
            if ("B_PLAN".equalsIgnoreCase(entry.featuretype)) {                
                planImporter = new BPlanNwmImporter(tableKonvertierung, tableBPlan, tableBPBereich, BPlanImporter.Version.v5_1n, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
            } else if ("F_PLAN".equalsIgnoreCase(entry.featuretype)) {
                planImporter = new FPlanNwmImporter(tableKonvertierung, tableFPlan, kvwmapUrl, kvwmapLoginName, kvwmapPassword );                
            } else if ("SO_PLAN".equalsIgnoreCase(entry.featuretype)) {
                planImporter = new SOPlanNwmImporter(tableKonvertierung, tableSOPlan, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
            } else {
                throw new IllegalArgumentException("Featuretype \"" + entry.featuretype + "\" wird nicht unterstützt. Unterstützt werden nur B_PLAN, F_PLAN und SO_PLAN");
            }
        } else {
            if ("B_PLAN".equalsIgnoreCase(entry.featuretype)) {
                planImporter = new BPlanImporter(tableKonvertierung, tableBPlan, BPlanImporter.Version.v5_1n, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
            } else if ("F_PLAN".equalsIgnoreCase(entry.featuretype)) {
                planImporter = new FPlanImporter(tableKonvertierung, tableFPlan, FPlanImporter.Version.v5_1n, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
            } else if ("SO_PLAN".equalsIgnoreCase(entry.featuretype)) {
                planImporter = new SOPlanImporter(tableKonvertierung, tableSOPlan, SOPlanImporter.Version.v5_1n, kvwmapUrl, kvwmapLoginName, kvwmapPassword );                    
            } else {
                throw new IllegalArgumentException("Featuretype \"" + entry.featuretype + "\" wird nicht unterstützt. Unterstützt werden nur B_PLAN, F_PLAN und SO_PLAN");
            }
        }
        return planImporter;
    }
    
    
    public static void runImport(List<? extends ImportConfigEntry> importConfigEntries, Connection conWrite, Connection conRead, EMailSender eMailSender, String kvwmapUrl, String kvwmapLoginName, String kvwmapPassword) {

        try {                
//            BPlanImporter bplImport;
//            BPlanNwmImporter bplNwmImport;
//            FPlanImporter fplImport;
//            FPlanNwmImporter fplNwmImport;
//            SOPlanImporter soplImport;
//            SOPlanNwmImporter soplNwmImport;
//            
//            
//            
//            
//            if (BPlanImportStarter.isSqlTypeSupported(conRead, "xp_spezexternereferenzauslegung")) {
//                bplImport = new BPlanImporter("xplankonverter.konvertierungen", "xplan_gml.bp_plan", BPlanImporter.Version.v5_1n, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
//                bplNwmImport = new BPlanNwmImporter("xplankonverter.konvertierungen", "xplan_gml.bp_plan", BPlanImporter.Version.v5_1n, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
//                fplImport = new FPlanImporter("xplankonverter.konvertierungen", "xplan_gml.fp_plan", FPlanImporter.Version.v5_1n, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
//                fplNwmImport = new FPlanNwmImporter("xplankonverter.konvertierungen", "xplan_gml.fp_plan", kvwmapUrl, kvwmapLoginName, kvwmapPassword );
//                soplImport = new SOPlanImporter("xplankonverter.konvertierungen", "xplan_gml.so_plan", SOPlanImporter.Version.v5_1n, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
//                soplNwmImport = new SOPlanNwmImporter("xplankonverter.konvertierungen", "xplan_gml.so_plan", kvwmapUrl, kvwmapLoginName, kvwmapPassword );
//            } else {
//                bplImport = new BPlanImporter("xplankonverter.konvertierungen", "xplan_gml.bp_plan", BPlanImporter.Version.v5_1, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
//                bplNwmImport = new BPlanNwmImporter("xplankonverter.konvertierungen", "xplan_gml.bp_plan", BPlanImporter.Version.v5_1, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
//                fplImport = new FPlanImporter("xplankonverter.konvertierungen", "xplan_gml.fp_plan", FPlanImporter.Version.v5_1, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
//                fplNwmImport = new FPlanNwmImporter("xplankonverter.konvertierungen", "xplan_gml.fp_plan", kvwmapUrl, kvwmapLoginName, kvwmapPassword );
//                soplImport = new SOPlanImporter("xplankonverter.konvertierungen", "xplan_gml.so_plan", SOPlanImporter.Version.v5_1, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
//                soplNwmImport = new SOPlanNwmImporter("xplankonverter.konvertierungen", "xplan_gml.so_plan", kvwmapUrl, kvwmapLoginName, kvwmapPassword );
//            }

            LogDAO logDAO = new LogDAO(conWrite, "xplankonverter.import_protocol");

            for (int i = 0; i < importConfigEntries.size(); i++) {                
                ImportConfigEntry entry = importConfigEntries.get(i);
                System.err.println("StellenId: \""+entry.stelle_id+"\"");
                if (entry instanceof JobEntry) {
                    ConfigReader.setJobStarted(conWrite, (JobEntry)entry);
                }
                ImportLogger importLogger = new ImportLogger();
                XPPlanImporterI planImporter = getImporter(entry, kvwmapUrl, kvwmapLoginName, kvwmapPassword);
                planImporter.importWFS(conWrite, conRead, entry, importLogger);
//                if (entry.onlineresource.indexOf("nwm")>=0) {
//                    if ("B_PLAN".equalsIgnoreCase(entry.featuretype)) {
//                        bplNwmImport.importWFS(conWrite, conRead, entry, logger);
//                    } else if ("F_PLAN".equalsIgnoreCase(entry.featuretype)) {
//                        fplNwmImport.importWFS(conWrite, conRead, entry, logger);
//                    } else if ("SO_PLAN".equalsIgnoreCase(entry.featuretype)) {
//                        soplNwmImport.importWFS(conWrite, conRead, entry, logger);                    
//                    } else {
//                        logger.addError("Featuretype \"" + entry.featuretype + "\" wird nicht unterstützt. Unterstützt werden nur B_PLAN, F_PLAN und SO_PLAN");
//                    }
//                } else {
//                    if ("B_PLAN".equalsIgnoreCase(entry.featuretype)) {
//                        bplImport.importWFS(conWrite, conRead, entry, logger);
//                    } else if ("F_PLAN".equalsIgnoreCase(entry.featuretype)) {
//                        fplImport.importWFS(conWrite, conRead, entry, logger);
//                    } else if ("SO_PLAN".equalsIgnoreCase(entry.featuretype)) {
//                        soplImport.importWFS(conWrite, conRead, entry, logger);                    
//                    } else {
//                        logger.addError("Featuretype \"" + entry.featuretype + "\" wird nicht unterstützt. Unterstützt werden nur B_PLAN, F_PLAN und SO_PLAN");
//                    }
//                }
                
                
                // TODO temp disabled
//                logDAO.insert(logger.getTime(), entry.stelle_id, logger.getText());
                logger.info(importLogger.getText());
                
                List<String> errors = importLogger.getErrors();
                if (errors!=null && errors.size()>0) {
                    if (eMailSender != null) {
                        sendErrors(errors, eMailSender, entry);
                    }
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
                List<ImportConfigEntry> importConfigEntries = ConfigReader.readConfig(conRead, "xplankonverter.import_services");
                
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
