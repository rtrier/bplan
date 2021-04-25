package de.gdiservice.bplan;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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

    final static Logger logger = LoggerFactory.getLogger(BPlanImporter.class);

    final static String DB_CONNECTION_PARAMS = "DB_CONNECTION_PARAMS";
    final static String EMAIL_SENDER = "emailCredential";
    final static String KVWMAP_URL = "KVWMAP_URL";

    

    final private DBConnectionParameter dbParam;
    final private String cronExpr;
    final private EMailSender emailSender;
    final private String kvwmapUrl;
    Scheduler scheduler;


    BPlanImportStarter(DBConnectionParameter dbParam, String cronExpr, String[] emailCredential, String kvwmapUrl) throws SchedulerException {
        this.dbParam = dbParam;
        this.cronExpr = cronExpr;   
        this.emailSender = new EMailSender(emailCredential[0], emailCredential[1]);
        this.kvwmapUrl = kvwmapUrl;
    }

    
    @DisallowConcurrentExecution
    public static class ImportJob implements Job {

        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.err.println("!!execute "+new Date());

            DBConnectionParameter dbParam = (DBConnectionParameter)context.getJobDetail().getJobDataMap().get(DB_CONNECTION_PARAMS);
            EMailSender emailSender = (EMailSender)context.getJobDetail().getJobDataMap().get(EMAIL_SENDER);
            String kvwmapUrl = (String)context.getJobDetail().getJobDataMap().get(KVWMAP_URL);
            BPlanImporter.runImport(dbParam, emailSender, kvwmapUrl);
        }
    }  
    
    @DisallowConcurrentExecution
    public static class JobReaderJob implements Job {

        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.err.println("!!execute "+new Date());

            DBConnectionParameter dbParam = (DBConnectionParameter)context.getJobDetail().getJobDataMap().get(DB_CONNECTION_PARAMS);
            EMailSender emailSender = (EMailSender)context.getJobDetail().getJobDataMap().get(EMAIL_SENDER);
            String kvwmapUrl = (String)context.getJobDetail().getJobDataMap().get(KVWMAP_URL);
            ImportJobRunner.start(dbParam, emailSender, kvwmapUrl);            
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
                .withSchedule(CronScheduleBuilder.cronSchedule(this.cronExpr))
                .build();        
        scheduler.scheduleJob(job02, trigger02);
        
        scheduler.start();
    }
    
    public void runNow() throws SchedulerException {
        BPlanImporter.runImport(dbParam, emailSender, kvwmapUrl);
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


    public static Connection getConnection(DBConnectionParameter param) throws SQLException {

        Properties props = new Properties();
        props.setProperty("user", param.getUser());
        props.setProperty("password", param.getPassword());

        Connection con =  DriverManager.getConnection(param.getUrl(), props);

        PGConnection pgconn = con.unwrap(PGConnection.class);
        pgconn.addDataType("\"xplan_gml\".\"xp_gemeinde\"", Gemeinde.class);
        pgconn.addDataType("\"xplan_gml\".\"xp_spezexternereferenz\"", PGExterneReferenz.class);
        pgconn.addDataType("\"xplan_gml\".\"xp_verbundenerplan\"", PGVerbundenerPlan.class);  
        pgconn.addDataType("geometry", JtsGeometry.class);
        return con;
    }



    public static void main(String[] args) {
        try {
            System.out.println(Arrays.toString(args));
            ArgList argList = new ArgList(args);
            
            String dburl = argList.get("dburl"); // "username@host:port/dbname";

            String pgpass = argList.get("pgpass");
            String cronExpr = argList.get("cronExpr");
            String sRunNow = argList.get("runNow");
            
            String emailUser = argList.get("emailUser");
            String emailPwd = argList.get("emailPwd");
            String[] emailCredential = null;
            if (emailUser!=null && emailPwd!=null) {
                emailCredential = new String[] {emailUser, emailPwd};
            }
            
            String kvwmapUrl = argList.get("kvwmap_url");   

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
            boolean runNow = sRunNow!=null && "true".equalsIgnoreCase(sRunNow);
            
            if (!runNow && cronExpr==null ) {
                missingParams.add("cronExpr");
            }
            if (missingParams.size()>0) {
                printVerwendung(missingParams.toString());
                System.exit(1);
            }


            File f = new File(pgpass);

            DBConnectionParameter dbParam = DBUtil.getConnectionParameter(f, dburl);
            BPlanImportStarter starter = new BPlanImportStarter(dbParam, cronExpr, emailCredential, kvwmapUrl);
            if (emailCredential == null) {
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
        System.out.println("\t\thttp://bauleitplaene-mv.de:8085/kvwmap_dev/konverter/index.php");
        System.out.println("\t\thttps://bauleitplaene-mv.de/konverter/index.php");
        
        System.out.println("Sollen E-Mails versendet werden sind anzugeben:");
        System.out.println("\temailUser"); 
        System.out.println("\temailPwd"); 
    }  
}
