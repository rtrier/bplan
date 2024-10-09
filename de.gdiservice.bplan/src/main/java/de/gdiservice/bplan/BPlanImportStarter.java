package de.gdiservice.bplan;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.postgis.jts.JtsGeometry;
import org.postgresql.PGConnection;
import org.quartz.CronScheduleBuilder;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gdiservice.util.ArgList;
import de.gdiservice.util.DBConnectionParameter;
import de.gdiservice.util.DBUtil;
import de.gdiservice.util.EMailSender;

public class BPlanImportStarter {

    final static Logger logger = LoggerFactory.getLogger(BPlanImportStarter.class);

    final static String DB_CONNECTION_PARAMS = "DB_CONNECTION_PARAMS";
    final static String EMAIL_SENDER = "emailCredential";
    final static String KVWMAP_URL = "KVWMAP_URL";
    final static String KVWMAP_LOGIN_NAME = "KVWMAP_LOGIN_NAME";
    final static String KVWMAP_PASWORD = "KVWMAP_PASWORD";
    

    final private DBConnectionParameter dbParam;
    final private String cronExpr;
    final private EMailSender emailSender;
    final private String kvwmapUrl;
    final private String kvwmapLoginName;
    final private String kvwmapPassword;
    
    Scheduler scheduler;


    BPlanImportStarter(DBConnectionParameter dbParam, String cronExpr, Map<String, String> emailParams, String kvwmapUrl, String kvwmapLoginName, String kvwmapPassword) throws SchedulerException {
        this.dbParam = dbParam;
        this.cronExpr = cronExpr;   
        this.emailSender = new EMailSender(emailParams);
        this.kvwmapUrl = kvwmapUrl;
        this.kvwmapLoginName = kvwmapLoginName;
        this.kvwmapPassword = kvwmapPassword;
    }

    
    @DisallowConcurrentExecution
    public static class ImportJob implements Job {

        public void execute(JobExecutionContext context) throws JobExecutionException {
            logger.info("execute ImportJob"+new Date());

            try {
                DBConnectionParameter dbParam = (DBConnectionParameter)context.getJobDetail().getJobDataMap().get(DB_CONNECTION_PARAMS);
                EMailSender emailSender = (EMailSender)context.getJobDetail().getJobDataMap().get(EMAIL_SENDER);
                String kvwmapUrl = (String)context.getJobDetail().getJobDataMap().get(KVWMAP_URL);
                String kvwmapLoginName = (String)context.getJobDetail().getJobDataMap().get(KVWMAP_LOGIN_NAME);
                String kvwmapPassword = (String)context.getJobDetail().getJobDataMap().get(KVWMAP_PASWORD);
                BPlanImporter.runImport(dbParam, emailSender, kvwmapUrl, kvwmapLoginName, kvwmapPassword);
            }
            catch (Throwable ex) {
                ex.printStackTrace();
                logger.error("Error ImportJob", ex);
            }
        }
    }  
    
    @DisallowConcurrentExecution
    public static class JobReaderJob implements Job {

        public void execute(JobExecutionContext context) throws JobExecutionException {
            logger.info("execute JobReaderJob "+new Date());

            try {
                DBConnectionParameter dbParam = (DBConnectionParameter)context.getJobDetail().getJobDataMap().get(DB_CONNECTION_PARAMS);
                EMailSender emailSender = (EMailSender)context.getJobDetail().getJobDataMap().get(EMAIL_SENDER);
                String kvwmapUrl = (String)context.getJobDetail().getJobDataMap().get(KVWMAP_URL);
                String kvwmapLoginName = (String)context.getJobDetail().getJobDataMap().get(KVWMAP_LOGIN_NAME);
                String kvwmapPassword = (String)context.getJobDetail().getJobDataMap().get(KVWMAP_PASWORD);
                ImportJobRunner.start(dbParam, emailSender, kvwmapUrl, kvwmapLoginName, kvwmapPassword);     
            }
            catch (Throwable ex) {
                ex.printStackTrace();
                logger.error("Error JobReaderJob", ex);
            }
        }
    }    


    public void start() throws SchedulerException {
        System.out.println("start cronExpression=\"" + this.cronExpr +"\"");
        
        this.scheduler = StdSchedulerFactory.getDefaultScheduler();
        
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());

        JobDetail job01 = JobBuilder.newJob(ImportJob.class)
                .withIdentity("importJob")
                .build();
        job01.getJobDataMap().put(DB_CONNECTION_PARAMS, this.dbParam);
        job01.getJobDataMap().put(EMAIL_SENDER, this.emailSender);
        job01.getJobDataMap().put(KVWMAP_URL, this.kvwmapUrl);
        job01.getJobDataMap().put(KVWMAP_LOGIN_NAME, this.kvwmapLoginName);
        job01.getJobDataMap().put(KVWMAP_PASWORD, this.kvwmapPassword);

        Trigger trigger01 = TriggerBuilder.newTrigger()
                .withSchedule(CronScheduleBuilder.cronSchedule(this.cronExpr))
                .build();
        scheduler.scheduleJob(job01, trigger01);
        
        JobDetail job02 = JobBuilder.newJob(JobReaderJob.class)
                .withIdentity("jobReader")
                .build();
        job02.getJobDataMap().put(DB_CONNECTION_PARAMS, this.dbParam);
        job02.getJobDataMap().put(EMAIL_SENDER, this.emailSender);
        job02.getJobDataMap().put(KVWMAP_URL, this.kvwmapUrl);

        Trigger trigger02 = TriggerBuilder.newTrigger()                
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0/5 * * * ?"))
                .build();        
        scheduler.scheduleJob(job02, trigger02);
        
        scheduler.start();
    }
    
    public void runNow() throws SchedulerException {
        BPlanImporter.runImport(dbParam, emailSender, kvwmapUrl, kvwmapLoginName, kvwmapPassword);
        if (cronExpr!=null) {
            this.start();
        }
    }

    class ShutdownThread extends Thread {
        public void run() {
            try {
                BPlanImportStarter.this.scheduler.shutdown(true);
                System.out.println("Done");
            } catch (SchedulerException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    
    public static boolean isSqlTypeSupported(Connection con, String type) throws SQLException {
        ResultSet rs = null;
        try {
            rs = con.getMetaData().getTypeInfo();
            while (rs.next()) {
                if (rs.getString(1).equals(type)) {
                    return true;
                }
            }  
        } finally {
            if (rs !=null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
        }
        return false;
    }
    


    public static Connection getConnection(DBConnectionParameter param) throws SQLException {

        Properties props = new Properties();
        props.setProperty("user", param.getUser());
        props.setProperty("password", param.getPassword());

        Connection con =  DriverManager.getConnection(param.getUrl(), props);

        PGConnection pgconn = con.unwrap(PGConnection.class);
        pgconn.addDataType("\"xplan_gml\".\"xp_gemeinde\"", Gemeinde.class);
        pgconn.addDataType("\"xplan_gml\".\"xp_spezexternereferenz\"", PGExterneReferenz.class);        
        pgconn.addDataType("\"xplan_gml\".\"so_planart\"", PG_SO_Planart.class);        
        if (BPlanImportStarter.isSqlTypeSupported(con, "xp_spezexternereferenzauslegung")) {
            pgconn.addDataType("\"xplankonverter\".\"xp_spezexternereferenzauslegung\"", PGExterneReferenzAuslegung.class);
        }
        pgconn.addDataType("\"xplan_gml\".\"xp_verbundenerplan\"", PGVerbundenerPlan.class);  
        pgconn.addDataType("geometry", JtsGeometry.class);
        return con;
    }

    static boolean validateEmailParam(Map<String, String> emailParams) {
        return emailParams.get(EMailSender.PARAM_EMAIL_FROM) != null &&
                emailParams.get(EMailSender.PARAM_EMAIL_SMTP_HOST) != null &&
                emailParams.get(EMailSender.PARAM_EMAIL_SMTP_PORT) != null &&
                emailParams.get(EMailSender.PARAM_EMAIL_USER) != null &&
                emailParams.get(EMailSender.PARAM_EMAIL_PWD) != null;
    }


    public static void main(String[] args) {
        System.out.println("BPlanImporter wird gestartet..");
        try {            
            ArgList argList = new ArgList(args);
            
            String dburl = argList.get("dburl"); // "username@host:port/dbname";

            String pgpass = argList.get("pgpass");
            String cronExpr = argList.get("cronExpr");
            String sRunNow = argList.get("runNow");
            
            Map<String, String> emailParams = new HashMap<>();
            emailParams.put(EMailSender.PARAM_EMAIL_FROM, argList.get("emailFrom"));
            emailParams.put(EMailSender.PARAM_EMAIL_SMTP_HOST, argList.get("emailSmtpHost"));
            emailParams.put(EMailSender.PARAM_EMAIL_SMTP_PORT, argList.get("emailPort"));
            emailParams.put(EMailSender.PARAM_EMAIL_USER, argList.get("emailUser"));
            emailParams.put(EMailSender.PARAM_EMAIL_PWD, argList.get("emailPwd"));            
            
            String kvwmapUrl = argList.get("kvwmap_url");
            String kvwmapUserName = argList.get("kvwmap_username");  
            String kvwmapPassword = argList.get("kvwmap_password");  
            
            List<String> missingParams = new ArrayList<>();
            if (dburl==null) {
                missingParams.add("dburl");
            }
            if (pgpass==null) {
                missingParams.add("pgpass");
            }
            if (kvwmapUrl==null) {
                missingParams.add("kvwmap_url");
            }
            if (kvwmapUserName==null) {
                missingParams.add("kvwmap_username");
            }
            if (kvwmapPassword==null) {
                missingParams.add("kvwmap_password");
            }
            boolean runNow = sRunNow!=null && "true".equalsIgnoreCase(sRunNow);
            
            if (!runNow && cronExpr==null ) {
                missingParams.add("cronExpr");
            }
            if (missingParams.size()>0) {
                printVerwendung(missingParams.toString());
                System.exit(1);
            }


            File f = new File(pgpass);
            if (!f.canRead()) {
                System.out.println("Can not run: file .pgpass not found.");
                System.exit(1);
            }

            DBConnectionParameter dbParam = DBUtil.getConnectionParameter(f, dburl);
            emailParams = validateEmailParam(emailParams) ? emailParams : null;
            
            BPlanImportStarter starter = new BPlanImportStarter(dbParam, cronExpr, emailParams, kvwmapUrl, kvwmapUserName, kvwmapPassword);
            if (emailParams == null) {
                System.out.println("E-Mail-Versand ist deaktiviert, Parameter emailUser oder emailPwd wurde nicht angegeben.");
            }
            if (runNow) { 
                starter.runNow();
            } else {
                starter.start();
            }

        } 
        catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    static void printVerwendung(String missingParam) {
        System.out.println("Es fehlen Parameter:"+ missingParam);
        System.out.println("\tdburl:  [username@host:port/dbname]"); 
        System.out.println("\tpgpass: [path zur pgpass-Datei]");
        System.out.println("\tcronExpr:");
        System.out.println("\tkvwmap_url:");
        System.out.println("\tkvwmap_username:");
        System.out.println("\tkvwmap_password:");
        System.out.println("\t\thttp://bauleitplaene-mv.de:8085/kvwmap_dev/konverter/index.php");
        System.out.println("\t\thttps://bauleitplaene-mv.de/konverter/index.php");
        
        System.out.println("Sollen E-Mails versendet werden sind anzugeben:");
        System.out.println("\temailUser"); 
        System.out.println("\temailPwd"); 
    }  
}
