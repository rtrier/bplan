package de.gdiservice.bplan;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gdiservice.util.DBConnectionParameter;
import de.gdiservice.util.EMailSender;

public class ImportJobRunner {

    final static Logger logger = LoggerFactory.getLogger(ImportJobRunner.class);
    
    public static void start(DBConnectionParameter dbParam, EMailSender emailSender, String kvwmapUrl, String kvwmapLoginName, String kvwmapPassword) {
        Connection conWrite = null;
        Connection conRead = null;
        try {
            conWrite = BPlanImportStarter.getConnection(dbParam);
            conRead = BPlanImportStarter.getConnection(dbParam);
            
            List<JobEntry> l = ConfigReader.readJobs(conWrite, kvwmapUrl);
            if (l!=null && l.size()>0) {                
                XPPlanImporter.runImport(l, conWrite, conRead, emailSender, kvwmapUrl, kvwmapLoginName, kvwmapPassword);
            }
        }
        catch (Exception ex) {
            logger.error("error running Observer", ex);
        }
        finally {
            if (conWrite != null) {
                try {
                    conWrite.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conRead != null) {
                try {
                    conRead.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
    }




}
