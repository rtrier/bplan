package de.gdiservice.bplan;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import de.gdiservice.bplan.konvertierung.Konvertierung;
import de.gdiservice.bplan.poi.BPBereich;
import de.gdiservice.bplan.poi.BPlan;
import de.gdiservice.bplan.poi.FPBereich;
import de.gdiservice.bplan.poi.FPlan;
import de.gdiservice.bplan.poi.SOBereich;
import de.gdiservice.bplan.poi.SOPlan;
import de.gdiservice.bplan.poi.XPPlan;
import de.gdiservice.util.DBConnectionParameter;
import de.gdiservice.util.EMailSender;
import de.gdiservice.wfs.BFitzBPlanFactoryV5_1;
import de.gdiservice.wfs.BFitzFPlanFactoryV5_1;
import de.gdiservice.wfs.BFitzSOPlanFactoryV5_1;
import de.gdiservice.wfs.GeolexBPlanFactory;
import de.gdiservice.wfs.WFSClient;
import de.gdiservice.wfs.WFSFactory;

interface XPPlanImporterI {
    public void importWFS(Connection conWrite, Connection conRead, ImportConfigEntry entry, ImportLogger importLogger) throws Exception;
}

public abstract class XPPlanImporter<T extends XPPlan> implements XPPlanImporterI {

    final static Logger logger = LoggerFactory.getLogger(XPPlanImporter.class);
    

    protected String bplanTable;
    
    protected String konvertierungTable;
    
    protected String kvwmapUrl;
    
    private String kvwmapLoginName;
    private String kvwmapPassword;

    WFSFactory<T> wfsFactory;
    
    boolean test = false;

    protected Class<T> planClasz;

    
    public enum Version {
        v5_1,
        v5_1n,
        v5_3,
        v_Geoplex
    }


    public XPPlanImporter(Class<T> clasz, String konvertierungTable, String bplanTable, Version version, String kvwmapUrl, String kvwmapLoginName, String kvwmapPassword) {
        
        this.planClasz = clasz;
        this.bplanTable = bplanTable;
        
        this.konvertierungTable = konvertierungTable;
        this.kvwmapUrl = kvwmapUrl;
        this.kvwmapLoginName = kvwmapLoginName;
        this.kvwmapPassword = kvwmapPassword;
        this.wfsFactory = getWFSFactory(version);
    }
    
    @SuppressWarnings("unchecked")
    protected WFSFactory<T> getWFSFactory(Version version) {
        WFSFactory<T> wfsFactory = null;
        if (planClasz.equals(BPlan.class)) {
            if (version==Version.v5_1) {
                wfsFactory = (WFSFactory<T>) new BFitzBPlanFactoryV5_1(false);
            } else if (version==Version.v5_1n) {
                wfsFactory = (WFSFactory<T>) new BFitzBPlanFactoryV5_1(true);
            } else {
                wfsFactory = (WFSFactory<T>) new GeolexBPlanFactory(true);
            }
        } if (planClasz.equals(FPlan.class)) {
            wfsFactory = (WFSFactory<T>) new BFitzFPlanFactoryV5_1(true);            
        } if (planClasz.equals(SOPlan.class)) {
            wfsFactory = (WFSFactory<T>) new BFitzSOPlanFactoryV5_1(true);
        }
        return wfsFactory;
    }
//    
//    public void setTest(boolean isTest) {
//        test = isTest;
//    }
    

    public abstract void updatePlaene(Connection conWrite, Connection conRead, ImportLogger importLogger, ImportConfigEntry entry, List<T> bPlans) throws SQLException; 
    

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
    
    public boolean validate(Konvertierung konvertierung, XPPlan plan, String kvwmapUrl, ImportLogger importLogger) throws ValidationException {
        
        try {
            boolean succedded = true; 
            ObjectReader objectReader = new ObjectMapper().reader();
            
            if (konvertierung==null) {
                throw new IllegalArgumentException("Validation: konvertierung was null");
            }
            if (plan==null) {
                throw new IllegalArgumentException("Validation: bplan was null");
            }
            if (konvertierung.id == null) {
                throw new IllegalArgumentException("Id der Konvertierung null");
            }
            if (plan.getKonvertierungId() == null) {
                throw new IllegalArgumentException("KonvertierungId des Plans is null");
            }
            if (konvertierung.id.intValue() != plan.getKonvertierungId().intValue()) {
                throw new IllegalArgumentException("KonvertierungId des Plans und die Id der Konvertierung stimmen nicht überein");
            }
            
            StringBuilder sb = new StringBuilder(kvwmapUrl);                
            sb.append("?go=xplankonverter_konvertierung");
            sb.append("&konvertierung_id=").append(plan.getKonvertierungId());
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
            sb.append("&value_konvertierung_id=").append(plan.getKonvertierungId());
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
                importLogger.addLine(String.format("validated %s", plan.getGml_id())); 
            } else {
                importLogger.addLine(String.format("Validierung war nicht erfolgreich %s", plan.getGml_id())); 
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

        List<T> plans = null;        
        try {
            String sVersion = (entry.onlineresource.indexOf("nwm")>=0) ? "2.0.0" : "1.1.0";
            
            final String wfsUrl = entry.onlineresource + "?service=WFS&VERSION="+sVersion+"&REQUEST=GetFeature&TYPENAME=" + entry.featuretype + "&SRSNAME=epsg:25833";
//            final String wfsUrl = entry.onlineresource + "?service=WFS&VERSION="+sVersion+"&REQUEST=GetFeature&TYPENAME=" + entry.featuretype + "&SRSNAME=epsg:25833&COUNT=50";
            
            importLogger.addLine("Reading WFS: \""+ entry.onlineresource + "\"");
            plans = WFSClient.read(wfsUrl, wfsFactory, importLogger);
        } 
        catch (Exception ex) {
            logger.error("error reading from service " + entry.bezeichnung + " with url=\"" + entry.onlineresource +"\"", ex);
            importLogger.addError("ERROR - Reading WFS: \""+ entry.onlineresource + "\" error:["+ex.getMessage()+"]");                
        }

        updatePlaene(conWrite, conRead, importLogger, entry, plans);

    }

    
    public static XPPlanImporterI getImporter(ImportConfigEntry entry, String kvwmapUrl, String kvwmapLoginName, String kvwmapPassword) {
        XPPlanImporterI planImporter = null;
        
        String tableKonvertierung = entry.testtable_prefix == null ? "xplankonverter.konvertierungen" : "xplankonverter."+entry.testtable_prefix+"konvertierungen";
        String tableBPlan = entry.testtable_prefix == null ? "xplan_gml.bp_plan" : "xplan_gml."+entry.testtable_prefix+"bp_plan";
        String tableBPBereich = entry.testtable_prefix == null ? "xplan_gml.bp_bereich" : "xplan_gml."+entry.testtable_prefix+"bp_bereich";
        String tableFPlan = entry.testtable_prefix == null ? "xplan_gml.fp_plan" : "xplan_gml."+entry.testtable_prefix+"fp_plan";
        String tableFPBereich  = entry.testtable_prefix == null ? "xplan_gml.fp_bereich" : "xplan_gml."+entry.testtable_prefix+"fp_bereich";
        String tableSOPlan = entry.testtable_prefix == null ? "xplan_gml.so_plan" : "xplan_gml."+entry.testtable_prefix+"so_plan";
        String tableSOBereich = entry.testtable_prefix == null ? "xplan_gml.so_bereich" : "xplan_gml."+entry.testtable_prefix+"so_bereich";
        
        if (entry.onlineresource.indexOf("nwm")>=0) {
            if ("B_PLAN".equalsIgnoreCase(entry.featuretype)) {       
                planImporter = new XPlanNwmImporter<BPlan, BPBereich>(BPlan.class, tableKonvertierung, tableBPlan, tableBPBereich, kvwmapUrl, kvwmapLoginName, kvwmapPassword );                                  
            } else if ("F_PLAN".equalsIgnoreCase(entry.featuretype)) {
                planImporter = new XPlanNwmImporter<FPlan, FPBereich>(FPlan.class, tableKonvertierung, tableFPlan, tableFPBereich, kvwmapUrl, kvwmapLoginName, kvwmapPassword );                
            } else if ("SO_PLAN".equalsIgnoreCase(entry.featuretype)) {
                planImporter = new XPlanNwmImporter<SOPlan, SOBereich>(SOPlan.class, tableKonvertierung, tableSOPlan, tableSOBereich, kvwmapUrl, kvwmapLoginName, kvwmapPassword );
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

                logDAO.insert(importLogger.getTime(), entry.stelle_id, importLogger.getText());
                logger.info("importLogger:\n"+ importLogger.getText());
                
                
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
                XPPlanImporter.runImport(importConfigEntries, conWrite, conRead, eMailSender, kvwmapUrl, kvwmapLoginName, kvwmapPassword);

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
