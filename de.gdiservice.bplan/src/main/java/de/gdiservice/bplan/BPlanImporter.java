package de.gdiservice.bplan;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
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

import de.gdiservice.bplan.VerbundenerPlan.RechtscharakterPlanaenderung;
import de.gdiservice.bplan.konvertierung.Gemeinde;
import de.gdiservice.bplan.konvertierung.GemeindeDAO;
import de.gdiservice.bplan.konvertierung.Konvertierung;
import de.gdiservice.bplan.konvertierung.Konvertierung.KonvertierungStatus;
import de.gdiservice.bplan.konvertierung.KonvertierungDAO;
import de.gdiservice.util.DBConnectionParameter;
import de.gdiservice.util.EMailSender;
import de.gdiservice.wfs.BFitzBPlanFactory;
import de.gdiservice.wfs.BFitzBPlanFactoryV5_1;
import de.gdiservice.wfs.WFSClient;
import de.gdiservice.wfs.WFSFactory;


public class BPlanImporter {

    final static Logger logger = LoggerFactory.getLogger(BPlanImporter.class);
    

    private String bplanTable;
    private String konvertierungTable;
    
    private String kvwmapUrl;
    
    private String kvwmapLoginName;
    private String kvwmapPassword;

    WFSFactory<BPlan> wfsFactory;
    
    boolean test = false;

    public enum Version {
        v5_1,
        v5_1n,
        v5_3
    }

    public BPlanImporter(String konvertierungTable, String bplanTable, Version version, String kvwmapUrl, String kvwmapLoginName, String kvwmapPassword) {
        this.bplanTable = bplanTable;
        this.konvertierungTable = konvertierungTable;
        this.kvwmapUrl = kvwmapUrl;
        this.kvwmapLoginName = kvwmapLoginName;
        this.kvwmapPassword = kvwmapPassword;
        if (version==Version.v5_1) {
            this.wfsFactory = new BFitzBPlanFactoryV5_1(false);
        } else if (version==Version.v5_1n) {
            this.wfsFactory = new BFitzBPlanFactoryV5_1(true);
        }
        else {
            this.wfsFactory = new BFitzBPlanFactory();
        }
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

    public void updateBPlaene(Connection con, ImportLogger importLogger, ImportConfigEntry entry, List<BPlan> bPlans) throws SQLException  {
        boolean autoCommit = con.getAutoCommit();
        if (autoCommit) {
            con.setAutoCommit(false);
        }

        if (bPlans != null) {
            BPlanDAO bplanDao = new BPlanDAO(con, bplanTable);
            KonvertierungDAO konvertierungDAO = new KonvertierungDAO(con, konvertierungTable);

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
                        
                        /**
                         * Setzen der Felder aendert und wurdegeaendertvon
                         */
                        BPlan previousPlan = null;
                        for (int teilPlanNr=0; teilPlanNr<teilPlaene.size(); teilPlanNr++) {
                            BPlan teilPlan = teilPlaene.get(teilPlanNr);
                            teilPlan.setInternalId(plan.getGml_id()+"-"+teilPlanNr);
                            
                            if (teilPlanNr>0) {
                                UUID teilPlanUUID = (teilPlanNr<listDBPlaene.size()) ? listDBPlaene.get(teilPlanNr).getGml_id() : UUID.randomUUID();
                                teilPlan.setGml_id(teilPlanUUID);
//                                teilPlan.name = teilPlan.name + " " + String.valueOf(teilPlanNr) + ". Änderung";
                                VerbundenerPlan aendert = new VerbundenerPlan(plan.name, RechtscharakterPlanaenderung.Aenderung, plan.nummer, previousPlan.getGml_id().toString());
                                teilPlan.setAendert(aendert);
                                VerbundenerPlan wurdegeaendertvon =  new VerbundenerPlan(plan.name, RechtscharakterPlanaenderung.Aenderung, plan.nummer, teilPlan.getGml_id().toString()); 
                                previousPlan.setWurdeGeaendertVon(wurdegeaendertvon);
                            }
                            previousPlan = teilPlan; 
                            
                        }
                        
                        
                        
                        for (int teilPlanNr=0; teilPlanNr<teilPlaene.size(); teilPlanNr++) {
                            BPlan teilPlan = teilPlaene.get(teilPlanNr);
                            BPlan dbPlan = (listDBPlaene.size()>teilPlanNr) ? listDBPlaene.get(teilPlanNr) : null;
                            logger.info("Teilplaene: {} {}/{} {} {}", plan.getGml_id(), teilPlanNr, teilPlaene.size(), teilPlan.getName(), dbPlan==null ? "null" : dbPlan.getName());
                            Konvertierung konvertierung;
                            if (dbPlan == null) {
                                // neuer BPlan
                                GemeindeDAO gemeindeDAO = new GemeindeDAO(con);
                                de.gdiservice.bplan.Gemeinde gemeinde = teilPlan.getGemeinde()[0];
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
                                con.commit();
                                
                                boolean succeded = validate(konvertierung, teilPlan, kvwmapUrl, importLogger);
                                // konvertierungDAO.updatePublishFlag(konvertierung.id, succeded);
                                if (succeded) {
                                    boolean isLastPlan = (teilPlanNr == teilPlaene.size()-1);
                                    if (isLastPlan && teilPlan.auslegungsstartdatum!=null && teilPlan.auslegungsstartdatum.length>0) {
                                        konvertierungDAO.updatePublishDate(konvertierung.id, new Timestamp(teilPlan.auslegungsenddatum[teilPlan.auslegungsstartdatum.length-1].getTime()));                                        
                                    } else {
                                        if (teilPlan.inkrafttretensdatum!=null) {
                                            konvertierungDAO.updatePublishDate(konvertierung.id, new Timestamp(teilPlan.inkrafttretensdatum.getTime()));
                                        }
                                    }
                                }
                                logger.info("BPLanImpoter: Plan gmlId=\""+teilPlan.getGml_id()+"\" inserted.");
                                importLogger.addLine(String.format("inserted %s", teilPlan.getGml_id()));
    
                            } else {
                                // update BPlan                                
                                if (BPlanImporter.hasChanged(teilPlan, dbPlan)) {
                                    logger.debug("update plan");
                                    teilPlan.setKonvertierungId(dbPlan.getKonvertierungId());
                                    bplanDao.update(teilPlan);
                                    int updateCount = konvertierungDAO.update(teilPlan.konvertierung_id);
                                    if (updateCount == 0) {
                                        throw new IllegalArgumentException("In der DB existiert ein BPlan mit der gmlId. Der zugehörige Eintrag in der Konvertierungs-Tabelle existiert aber nicht.");
                                    }
                                    konvertierungDAO.updatePublishFlag(teilPlan.konvertierung_id, false);
                                    konvertierung = konvertierungDAO.find(teilPlan.konvertierung_id); 
                                    
                                    logger.info("BPLanImpoter: Plan gmlId=\""+teilPlan.getGml_id()+"\" updated.");
                                    importLogger.addLine(String.format("updated %s", teilPlan.getGml_id()));
                                    con.commit();
                                    
                                    boolean succeded = validate(konvertierung, teilPlan, kvwmapUrl, importLogger);
//                                    konvertierungDAO.updatePublishFlag(konvertierung.id, succeded);
                                    if (teilPlan.inkrafttretensdatum!=null && succeded) {
                                        konvertierungDAO.updatePublishDate(konvertierung.id, new Timestamp(teilPlan.inkrafttretensdatum.getTime()));
                                    }
                                    
                                } else {
                                    logger.info("BPLanImpoter: Plan gmlId=\""+teilPlan.getGml_id()+"\" unchanged.");
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
                        con.rollback();
                    } 
                    catch (SQLException e) {
                        logger.error("rollback Error", e);
                    }
                    importLogger.addError("error updating BPlan [gmlId="+ plan.gml_id +" name=\""+ plan.name +"\"] from service \"" + entry.bezeichnung + "\" with url=\"" + entry.onlineresource +"\" error:["+ex.getMessage()+"]");
                    logger.error("error updating BPlan [gmlId="+ plan.gml_id +" name=\""+ plan.name +"\"] from service \"" + entry.bezeichnung + "\" with url=\"" + entry.onlineresource +"\"", ex);
                } 
            }
        }
        if (autoCommit) {
            con.setAutoCommit(autoCommit);
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
        de.gdiservice.bplan.Gemeinde[] gemeinden = plan.getGemeinde();
        if (gemeinden!=null) {
            for (de.gdiservice.bplan.Gemeinde gemeinde : gemeinden) {
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

    public void importWFS(Connection con, ImportConfigEntry entry, ImportLogger importLogger) throws Exception  {

        List<BPlan> bPlans = null;        
        try {
            final String wfsUrl = entry.onlineresource + "?service=WFS&VERSION=1.1.0&REQUEST=GetFeature&TYPENAME=" + entry.featuretype + "&SRSNAME=epsg:25833";
            importLogger.addLine("Reading WFS: \""+ entry.onlineresource + "\"");
            bPlans = WFSClient.read(wfsUrl, wfsFactory, importLogger);
        } 
        catch (Exception ex) {
            logger.error("error reading from service " + entry.bezeichnung + " with url=\"" + entry.onlineresource +"\"", ex);
            importLogger.addError("ERROR - Reading WFS: \""+ entry.onlineresource + "\" error:["+ex.getMessage()+"]");                
        }

        updateBPlaene(con, importLogger, entry, bPlans);

    }



    private static boolean hasChanged(Object o1, Object o2) {
        if (o1==null) {
            if (o2!=null) {
                return true;
            }
            return false;
        } 
        return !o1.equals(o2);
    }
    
    public static boolean hasChanged(PGVerbundenerPlan[] a01, PGVerbundenerPlan[] a02) {
        if (a01 == null) {
            return (a02 == null) ? false : true;
        }
        if (a02 == null) {
            return true;
        }
        if (a01.length != a02.length) {
            return true;
        }
        for (int i=0; i<a01.length; i++) {

            VerbundenerPlan vb01 = a01[i].getVerbundenerPlan();
            VerbundenerPlan vb02 = a02[i].getVerbundenerPlan();
            if (hasChanged(vb01.getPlanname(), vb02.getPlanname())) {
                return true;
            }
            if (hasChanged(vb01.getNummer(), vb02.getNummer())) {
                return true;
            }
            if (hasChanged(vb01.getRechtscharakter(), vb02.getRechtscharakter())) {
                return true;
            }
            if (hasChanged(vb01.getVerbundenerplan(), vb02.getVerbundenerplan())) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean hasChanged(PGExterneReferenz[] a01, PGExterneReferenz[] a02) {
        if (a01 == null) {
            return (a02 == null) ? false : true;
        }
        if (a02 == null) {
            return true;
        }
        if (a01.length != a02.length) {
            return true;
        }
        for (int i=0; i<a01.length; i++) {

            ExterneRef exRef01 = a01[i].object;
            ExterneRef exRef02 = a02[i].object;
            
            if (hasChanged(exRef01.art, exRef02.art)) {                
                return true;
            }
            if (hasChanged(exRef01.beschreibung, exRef02.beschreibung)) {
                return true;
            }
            if (hasChanged(exRef01.datum, exRef02.datum)) {           
                return true;        
            }
            if (hasChanged(exRef01.georefmimetype, exRef02.georefmimetype)) {
                return true;
            } 
            if (hasChanged(exRef02.georefurl, exRef02.georefurl)) {
                return true;
            } 
            if (hasChanged(exRef01.informationssystemurl, exRef02.informationssystemurl)) {
                return true;
            } 
            if (hasChanged(exRef01.referenzname, exRef02.referenzname)) {
                return true;                    
            }
            if (hasChanged(exRef01.referenzurl, exRef02.referenzurl)) {
                return true;
            } 
            if (hasChanged(exRef01.typ, exRef02.typ)) {
                return true;
            }
        }
        return false;
    }
    public static boolean hasChanged(
            de.gdiservice.bplan.Gemeinde[] gemeinden01, 
            de.gdiservice.bplan.Gemeinde[] gemeinden02) {
        if (gemeinden01 == null || gemeinden02 == null) {
            throw new IllegalArgumentException("one Plan doesnt has gemeinden");
        }
        if (gemeinden01.length != gemeinden02.length) {
            return true;
        }
        for (int i=0; i<gemeinden01.length; i++) {
            if (!gemeinden01[i].equals(gemeinden02[i])) {
                return true;
            }
        }
        return false;
    }
    
    private static String toString(de.gdiservice.bplan.Gemeinde[] gemeinden) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<gemeinden.length; i++) {
            sb.append("\n\ttoString[\"").append(gemeinden[i]).append("\"\n\t");
            sb.append("toStrin2[ags=").append(gemeinden[i].ags).append(" rs=").append(gemeinden[i].rs);
            sb.append(" gemeindename=\"").append(gemeinden[i].gemeindename).append("\"");
            sb.append(" ortsteilname=\"").append(gemeinden[i].ortsteilname).append("\"");
        }
        return sb.toString();
    }

    public static boolean hasChanged(BPlan plan, BPlan dbPlan) {

        

        if (hasChanged(plan.gemeinde, dbPlan.gemeinde)) {
            logger.info(String.format("<>gemeinde %s %s", toString(plan.gemeinde), toString(dbPlan.gemeinde)));
            return true;
        }
        if (hasChanged(plan.getExternereferenzes(), dbPlan.getExternereferenzes())) {
            logger.info(String.format("<>Externereferenzes %s %s", plan.getExternereferenzes(), dbPlan.getExternereferenzes()));
            return true;
        }

        if (plan.geom == null || dbPlan.geom == null) {
            throw new IllegalArgumentException("one Plan doesnt has a geometry");
        }
        if (!plan.geom.equals(dbPlan.geom)) {
            logger.info(String.format("<>geom %s %s", plan.geom, dbPlan.geom));
            return true;
        }
        if (hasChanged(plan.inkrafttretensdatum, dbPlan.inkrafttretensdatum)) {
            logger.info(String.format("<>inkrafttretensdatum %s %s", plan.inkrafttretensdatum, dbPlan.inkrafttretensdatum));
            return true;
        }
        if (hasChanged(plan.auslegungsstartdatum, dbPlan.auslegungsstartdatum)) {
            logger.info(String.format("<>auslegungsstartdatum %s %s", plan.auslegungsstartdatum, dbPlan.auslegungsstartdatum));
            return true;
        }
        if (hasChanged(plan.auslegungsenddatum, dbPlan.auslegungsenddatum)) {
            logger.info(String.format("<>auslegungsenddatum %s %s", plan.auslegungsenddatum, dbPlan.auslegungsenddatum));
            return true;
        }  
        if (hasChanged(plan.name, dbPlan.name)) {
            logger.info(String.format("<>name %s %s", plan.name, dbPlan.name));
            return true;
        }
        if (hasChanged(plan.nummer, dbPlan.nummer)) {
            logger.info(String.format("<>nummer %s %s", plan.nummer, dbPlan.nummer));
            return true;
        }
        if (!Arrays.equals(plan.planart, dbPlan.planart)) {
            logger.info(String.format("<>planart %s %s", plan.planart, dbPlan.planart));
            return true;
        }
        if (hasChanged(plan.rechtsstand, dbPlan.rechtsstand)) {
            logger.info(String.format("<>rechtsstand %s %s", plan.rechtsstand, dbPlan.rechtsstand));
            return true;
        }
        
        if (hasChanged(plan.aendert, dbPlan.aendert)) {
            logger.info(String.format("<>aendert %s %s", plan.aendert, dbPlan.aendert));
            return true;
        }
        if (hasChanged(plan.wurdegeaendertvon, dbPlan.wurdegeaendertvon)) {
            logger.info(String.format("<>wurdegeaendertvon %s %s", plan.wurdegeaendertvon, dbPlan.wurdegeaendertvon));
            return true;
        }
        if (hasChanged(plan.internalid, dbPlan.internalid)) {
            logger.info(String.format("<>internalid %s %s", plan.internalid, dbPlan.internalid));
            return true;
        }
        return false;
    }


    public static void print(String text, BPlan bPlan) {
        StringBuilder sb = new StringBuilder();
        sb.append(text).append('\n');
        sb.append('\t').append("[id=" + bPlan.id + ", name=" + bPlan.name + ", gml_id=" + bPlan.gml_id + ", nummer=" + bPlan.nummer + ", planart="
                + bPlan.planart + ", rechtsstand=" + bPlan.rechtsstand + ", inkrafttretensdatum=" + bPlan.inkrafttretensdatum);
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
        PGExterneReferenz[] pgExterneReferenzs = bPlan.externeReferenzes;
        if (pgExterneReferenzs!=null) {
            for (int i = 0; i < pgExterneReferenzs.length; i++) {
                ExterneRef exRef = pgExterneReferenzs[i].object;
                sb.append("\n\t\tExterneRef [georefurl=" + exRef.georefurl + ", georefmimetype=" + exRef.georefmimetype + ", art=" + exRef.art
                        + ", informationssystemurl=" + exRef.informationssystemurl + ", referenzname=" + exRef.referenzname + ", referenzurl="
                        + exRef.referenzurl);
                sb.append("\n\t\treferenzmimetype=" + exRef.referenzmimetype);
                sb.append("\n\t\tbeschreibung=" + exRef.beschreibung + ", datum=" + exRef.datum + ", typ=" + exRef.typ + "]");

            }
        }
        sb.append("\n\tgeom=" + bPlan.geom + "]");
        System.out.println(sb);
    }

    public static void runImport(List<? extends ImportConfigEntry> importConfigEntries, Connection con, String kvwmapUrl, String kvwmapLoginName, String kvwmapPassword) {

        try {                
            
            BPlanImporter bplImport = null;
            FPlanImporter fplImport = null;
            LogDAO logDAO = new LogDAO(con, "xplankonverter.import_protocol");

            for (int i = 0; i < importConfigEntries.size(); i++) {                
                ImportConfigEntry entry = importConfigEntries.get(i);
                System.err.println("StellenId: \""+entry.stelle_id+"\"");
                if (entry instanceof JobEntry) {
                    ConfigReader.setJobStarted(con, (JobEntry)entry);
                }
                ImportLogger logger = new ImportLogger();
                if ("B_PLAN".equalsIgnoreCase(entry.featuretype)) {
                    if (bplImport==null) {
                        if (BPlanImportStarter.isSqlTypeSupported(con, "xp_spezexternereferenzauslegung")) {
                            bplImport = new BPlanImporter("xplankonverter.konvertierungen", "xplan_gml.bp_plan", BPlanImporter.Version.v5_1n, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
                        } else {
                            bplImport = new BPlanImporter("xplankonverter.konvertierungen", "xplan_gml.bp_plan", BPlanImporter.Version.v5_1, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
                        }
                    }
                    bplImport.importWFS(con, entry, logger);
                } else {
                    if ("F_PLAN".equalsIgnoreCase(entry.featuretype)) {
                        if (fplImport==null) {
                            if (BPlanImportStarter.isSqlTypeSupported(con, "xp_spezexternereferenzauslegung")) {
                                fplImport = new FPlanImporter("xplankonverter.konvertierungen", "xplan_gml.fp_plan", FPlanImporter.Version.v5_1n, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
                            } else {
                                fplImport = new FPlanImporter("xplankonverter.konvertierungen", "xplan_gml.fp_plan", FPlanImporter.Version.v5_1, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
                            }
                        }
                        fplImport.importWFS(con, entry, logger);
                    } else {
                        logger.addError("Featuretype \"" + entry.featuretype + "\" wird nicht unterstützt. Unterstützt werden erden nur B_PLAN und F_PLAN");
                    }
                }                
                logDAO.insert(logger.getTime(), entry.stelle_id, logger.getText());
                
                List<String> errors = logger.getErrors();
                if (errors!=null && errors.size()>0) {
                    System.err.println(errors.get(i));
                }
                if (entry instanceof JobEntry) {
                    ConfigReader.setJobFinished(con, (JobEntry)entry);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    
    public static void runImport(List<? extends ImportConfigEntry> importConfigEntries, Connection con, EMailSender eMailSender, String kvwmapUrl, String kvwmapLoginName, String kvwmapPassword) {

        try {                
            BPlanImporter bplImport;
            FPlanImporter fplImport;
            if (BPlanImportStarter.isSqlTypeSupported(con, "xp_spezexternereferenzauslegung")) {
                bplImport = new BPlanImporter("xplankonverter.konvertierungen", "xplan_gml.bp_plan", BPlanImporter.Version.v5_1n, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
                fplImport = new FPlanImporter("xplankonverter.konvertierungen", "xplan_gml.fp_plan", FPlanImporter.Version.v5_1n, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
            } else {
                bplImport = new BPlanImporter("xplankonverter.konvertierungen", "xplan_gml.bp_plan", BPlanImporter.Version.v5_1, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
                fplImport = new FPlanImporter("xplankonverter.konvertierungen", "xplan_gml.fp_plan", FPlanImporter.Version.v5_1, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
            }

            LogDAO logDAO = new LogDAO(con, "xplankonverter.import_protocol");

            for (int i = 0; i < importConfigEntries.size(); i++) {                
                ImportConfigEntry entry = importConfigEntries.get(i);
                System.err.println("StellenId: \""+entry.stelle_id+"\"");
                if (entry instanceof JobEntry) {
                    ConfigReader.setJobStarted(con, (JobEntry)entry);
                }
                ImportLogger logger = new ImportLogger();
                if ("B_PLAN".equalsIgnoreCase(entry.featuretype)) {
                    bplImport.importWFS(con, entry, logger);
                } else {
                    if ("F_PLAN".equalsIgnoreCase(entry.featuretype)) {
                        fplImport.importWFS(con, entry, logger);
                    } else {
                        logger.addError("Featuretype \"" + entry.featuretype + "\" wird nicht unterstützt. Unterstützt werden erden nur B_PLAN und F_PLAN");
                    }
                }
                logDAO.insert(logger.getTime(), entry.stelle_id, logger.getText());
                
                List<String> errors = logger.getErrors();
                if (errors!=null && errors.size()>0) {
                    sendErrors(errors, eMailSender, entry);
                }
                if (entry instanceof JobEntry) {
                    ConfigReader.setJobFinished(con, (JobEntry)entry);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static void runImport(DBConnectionParameter dbParam, EMailSender eMailSender, String kvwmapUrl, String kvwmapLoginName, String kvwmapPassword) {
        Connection con = null;
        try {
            con = BPlanImportStarter.getConnection(dbParam);

            try {
                List<ImportConfigEntry> importConfigEntries = ConfigReader.readConfig(con, "xplankonverter.import_services");
                
                runImport(importConfigEntries, con, eMailSender, kvwmapUrl, kvwmapLoginName, kvwmapPassword);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } 
        catch (Exception ex) {
            throw new IllegalArgumentException("DB-Verbinung konnte nicht hergestellt werden.", ex);
        } finally {
            if (con!=null) {
                try {
                    con.close();
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
