package de.gdiservice.bplan;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import de.gdiservice.util.DBUtil;

public class ConfigReader {


    public static ImportConfigEntry readConfig(Connection con, String table, int id) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        ImportConfigEntry importConfigEntry = null;

        String sql = DBUtil.getSelectSQLString(table, ImportConfigEntry.ATTNAMES, new String[] {"id=?"});

        try {    
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            if (rs.next()) {
                importConfigEntry = new ImportConfigEntry();
                for (int i=0, count=ImportConfigEntry.ATTNAMES.length; i<count; i++) {
                    set(importConfigEntry, ImportConfigEntry.ATTNAMES[i], rs.getObject(i+1));
                }
            }
        }
        finally {
            if (rs!=null) {
                rs.close();
            }
            if (stmt!=null) {
                stmt.close();
            }
        }      
        return importConfigEntry;      
    }

    public static List<ImportConfigEntry> readConfig(Connection con, String table) throws SQLException {

        Statement stmt = null;
        ResultSet rs = null;

        List<ImportConfigEntry> list = new ArrayList<>();

        String sql = DBUtil.getSelectSQLString(table, ImportConfigEntry.ATTNAMES);

        try {    
            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                ImportConfigEntry entry = new ImportConfigEntry();
                for (int i=0, count=ImportConfigEntry.ATTNAMES.length; i<count; i++) {
                    set(entry, ImportConfigEntry.ATTNAMES[i], rs.getObject(i+1));
                }
                list.add(entry);
            }


        }
        finally {
            if (rs!=null) {
                rs.close();
            }
            if (stmt!=null) {
                stmt.close();
            }
        }

        return list;

    }

    
    public static void setJobStarted(Connection con, JobEntry job) throws SQLException {
        String sql = "update xplankonverter.import_jobs set started_at=now() where id = ?";

        PreparedStatement stmt = null;
        
        try {    
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, job.jobId);
            stmt.executeUpdate();
        }
        finally {
            if (stmt!=null) {
                stmt.close();
            }
        }
    }
    

    public static void setJobFinished(Connection con, JobEntry job) throws SQLException {
        String sql = "update xplankonverter.import_jobs set finished_at=now() where id = ?";

        PreparedStatement stmt = null;
        
        try {    
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, job.jobId);
            stmt.executeUpdate();
        }
        finally {
            if (stmt!=null) {
                stmt.close();
            }
        }
    }

    public static List<JobEntry> readJobs(Connection con, String table) throws SQLException {

        Statement stmt = null;
        ResultSet rs = null;

        final String[] ATTNAMES = new String[] {
                "jobId", "importType", "id", "bezeichnung", "onlineresource", "featuretype", "stelle_id", "created_at", "created_from", "updated_at", "updated_from", "version"
        };

        List<JobEntry> list = new ArrayList<>();

        String sql = "select j.id, import_type ,s.id,s.bezeichnung,s.onlineresource,s.featuretype,s.stelle_id,s.created_at,s.created_from,s.updated_at,s.updated_from,s.version "
                + "from xplankonverter.import_jobs j\r\n"
                + "left join xplankonverter.import_services s on j.import_service_id=s.id "
                + "where j.started_at is null";

        try {    
            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                JobEntry entry = new JobEntry();
                for (int i=0, count=ATTNAMES.length; i<count; i++) {
                    set(entry, ATTNAMES[i], rs.getObject(i+1));
                }
                list.add(entry);
            }


        }
        finally {
            if (rs!=null) {
                rs.close();
            }
            if (stmt!=null) {
                stmt.close();
            }
        }

        return list;

    }  


    public static boolean set(Object object, String fieldName, Object fieldValue) {
        Class<?> clazz = object.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(object, fieldValue);
                return true;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return false;
    }  


}
