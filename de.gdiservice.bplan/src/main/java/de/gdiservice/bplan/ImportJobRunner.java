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
    
    public static void start(DBConnectionParameter dbParam, EMailSender emailSender, String kvwmapUrl) {
        Connection con = null;
        try {
            con = BPlanImportStarter.getConnection(dbParam);
            
            List<JobEntry> l = ConfigReader.readJobs(con, kvwmapUrl);
            if (l!=null && l.size()>0) {                
                BPlanImporter.runImport(l, con, emailSender, kvwmapUrl);
            }
        }
        catch (Exception ex) {
            logger.error("error running Observer", ex);
        }
        finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
    }




}
