package de.gdiservice.bplan.konvertierung;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gdiservice.bplan.konvertierung.Konvertierung.EPSGCodes;
import de.gdiservice.bplan.konvertierung.Konvertierung.KonvertierungStatus;
import de.gdiservice.util.DBUtil;

public class KonvertierungDAO {

    final static Logger logger = LoggerFactory.getLogger(KonvertierungDAO.class);

    final String[] COLUMN_NAMES = new String[] {
            "id", "bezeichnung", "status", "stelle_id", "beschreibung", "shape_layer_group_id", "created_at", "updated_at", "user_id", 
            "geom_precision", "gml_layer_group_id", "epsg", "output_epsg", "input_epsg", "gebietseinheiten", "planart", "veroeffentlicht"
        };
    
    final String[] INSERT_COLUMN_NAMES = new String[] {
            "bezeichnung", "status", "stelle_id", "beschreibung", "shape_layer_group_id", "user_id", 
            "geom_precision", "gml_layer_group_id", "epsg", "output_epsg", "input_epsg", "gebietseinheiten", "planart", "veroeffentlicht"
        };


    private final Connection con;
    private final String tableName;

    public KonvertierungDAO(Connection con, String tableName) {
        this.con = con;
        this.tableName = tableName;
    }
    
    public Konvertierung find(Integer id) throws SQLException {        
        Konvertierung konvertierung = null;
        PreparedStatement stmt = null;
        ResultSet resultset = null;
        try {    
            String sql = DBUtil.getSelectSQLString(tableName, COLUMN_NAMES, new String[] {"id=?"});      
            stmt = con.prepareStatement(sql);
            stmt.setObject(1, id);;
            logger.debug("{}", stmt);
            resultset = stmt.executeQuery();
            if (resultset.next()) {
                konvertierung = createKonvertierung(resultset);
            }
        } finally {
            if (resultset!=null) {
                try { resultset.close(); } catch (SQLException e) {}
            }
            if (stmt!=null) {
                try { stmt.close(); } catch (SQLException e) {}
            }
        }
        return konvertierung;

    }    

    public List<Konvertierung> find(String rs, Integer ags) throws SQLException {        
        List<Konvertierung> konvertierungen = new ArrayList<>();
        
        PreparedStatement stmt = null;
        ResultSet resultset = null;
        try {    
            String sql = DBUtil.getSelectSQLString(tableName, COLUMN_NAMES, new String[]  {"rs=?", "ags=?"});      
            stmt = con.prepareStatement(sql);
            stmt.setObject(1, rs);
            stmt.setObject(2, ags);
            logger.debug("{}", stmt);
            resultset = stmt.executeQuery();
            while (resultset.next()) {
                konvertierungen.add(createKonvertierung(resultset));
            }
        } finally {
            if (resultset!=null) {
                try { resultset.close(); } catch (SQLException e) {}
            }
            if (stmt!=null) {
                try { stmt.close(); } catch (SQLException e) {}
            }
        }
        return konvertierungen;

    }
    
    
    public Konvertierung insert(Konvertierung konvertierung) throws SQLException {
        PreparedStatement stmt = null;

        try {    
            String sql = DBUtil.getInsertSQLString(tableName, INSERT_COLUMN_NAMES);      
            
            stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int i=1;
            // stmt.setNull(i++, Types.INTEGER);
            stmt.setString(i++, konvertierung.bezeichnung);
            
            if (konvertierung.status==null) {
                stmt.setNull(i++, java.sql.Types.OTHER);
            } else {
                stmt.setObject(i++, konvertierung.status.getText(), Types.OTHER);
            }
                        
            stmt.setObject(i++, konvertierung.stelle_id);
            stmt.setString(i++, konvertierung.beschreibung);
            stmt.setObject(i++, konvertierung.shape_layer_group_id);
//            if (konvertierung.created_at==null) {
//                stmt.setNull(i++, Types.TIMESTAMP);
//            } else {
//                stmt.setTimestamp(i++, new Timestamp(konvertierung.created_at.getTime()));
//            }
//            if (konvertierung.updated_at==null) {
//                stmt.setNull(i++, Types.TIMESTAMP);
//            } else {
//                stmt.setTimestamp(i++, new Timestamp(konvertierung.updated_at.getTime()));
//            }
            stmt.setObject(i++, konvertierung.user_id);
            stmt.setObject(i++, konvertierung.geom_precision);
            stmt.setObject(i++, konvertierung.gml_layer_group_id);    
            if (konvertierung.epsg==null) {
                stmt.setNull(i++, Types.OTHER); 
            } else {
                stmt.setObject(i++, String.valueOf(konvertierung.epsg.getCode()), Types.OTHER); 
            }
            if (konvertierung.output_epsg==null) {
                stmt.setNull(i++, Types.OTHER); 
            } else {
                stmt.setObject(i++, String.valueOf(konvertierung.output_epsg.getCode()), Types.OTHER);
            }
            if (konvertierung.input_epsg==null) {
                stmt.setNull(i++, Types.OTHER); 
            } else {
                stmt.setObject(i++, String.valueOf(konvertierung.input_epsg.getCode()), Types.OTHER);
            }

            stmt.setString(i++, konvertierung.gebietseinheiten);
            stmt.setString(i++, konvertierung.planart);
            stmt.setObject(i++, konvertierung.veroeffentlicht);            
            
            logger.debug("{}", stmt);
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()){
                konvertierung.id=(Integer)rs.getObject(1);
            }

        } finally {

            if (stmt!=null) {
                try { stmt.close(); } catch (SQLException e) {}
            }
        }
        return konvertierung;
    }

    private Konvertierung createKonvertierung(ResultSet resultset) throws SQLException {
        Konvertierung konvertierung = new Konvertierung();
        int i=1;
        konvertierung.id = (Integer)resultset.getObject(i++);
        konvertierung.bezeichnung = resultset.getString(i++);
        String status = resultset.getString(i++);
        konvertierung.status = (status==null) ? null : KonvertierungStatus.get(status);
        konvertierung.stelle_id = (Integer)resultset.getObject(i++);
        konvertierung.beschreibung = resultset.getString(i++);
        konvertierung.shape_layer_group_id = (Integer)resultset.getObject(i++);
        konvertierung.created_at = resultset.getTimestamp(i++);
        konvertierung.updated_at = resultset.getTimestamp(i++);
        konvertierung.user_id = (Integer)resultset.getObject(i++);
        konvertierung.geom_precision = (Integer)resultset.getObject(i++);
        konvertierung.gml_layer_group_id = (Integer)resultset.getObject(i++);
        
        String epsg = resultset.getString(i++); 
        konvertierung.epsg = (epsg==null) ? null : EPSGCodes.get(Integer.parseInt(epsg));
        String epsg_out= resultset.getString(i++);
        konvertierung.output_epsg = (epsg_out==null) ? null : EPSGCodes.get(Integer.parseInt(epsg_out));
        String epsg_in = resultset.getString(i++);
        konvertierung.input_epsg = (epsg_in==null) ? null : EPSGCodes.get(Integer.parseInt(epsg_in));
        konvertierung.gebietseinheiten = resultset.getString(i++);
        konvertierung.planart = resultset.getString(i++);
        konvertierung.veroeffentlicht = (Boolean)resultset.getObject(i++);
        return konvertierung;
    }

    public int update(Integer konvertierung_id) throws SQLException {
        
        PreparedStatement stmt = null;

        try {    
            String sql = "update " + tableName +" set updated_at = now() where id=?";
            
            stmt = con.prepareStatement(sql);
            stmt.setObject(1, konvertierung_id);
            logger.debug("{}", stmt);
            return stmt.executeUpdate();

        }
        finally {

            if (stmt!=null) {
                try { stmt.close(); } catch (SQLException e) {}
            }
        }
    }

    public void updatePublishFlag(Integer konvertierung_id, boolean succeded) throws SQLException {
        PreparedStatement stmt = null;
        try {    
            String sql = "update xplankonverter.konvertierungen set veroeffentlicht = ? where id=?";
            
            stmt = con.prepareStatement(sql);
            stmt.setBoolean(1, succeded);
            stmt.setObject(2, konvertierung_id);
            logger.debug("{}", stmt);
            stmt.executeUpdate();
        }
        finally {

            if (stmt!=null) {
                try { stmt.close(); } catch (SQLException e) {}
            }
        }
        
    }     
    
    
    public void updatePublishDate(Integer konvertierung_id, LocalDate time) throws SQLException {
        PreparedStatement stmt = null;
        try {    
            String sql = "update xplankonverter.konvertierungen set veroeffentlichungsdatum = ? where id=?";
            
            stmt = con.prepareStatement(sql);
            stmt.setObject(1, time);
            stmt.setObject(2, konvertierung_id);
            logger.debug("{}", stmt);
            stmt.executeUpdate();
        }
        finally {

            if (stmt!=null) {
                try { stmt.close(); } catch (SQLException e) {}
            }
        }
        
    }
    
    public void updatePublishDate(Integer konvertierung_id, java.util.Date time) throws SQLException {
        PreparedStatement stmt = null;
        try {    
            String sql = "update xplankonverter.konvertierungen set veroeffentlichungsdatum = ? where id=?";
            
            stmt = con.prepareStatement(sql);
            stmt.setDate(1, new Date(time.getTime()));
            stmt.setObject(2, konvertierung_id);
            logger.debug("{}", stmt);
            stmt.executeUpdate();
        }
        finally {

            if (stmt!=null) {
                try { stmt.close(); } catch (SQLException e) {}
            }
        }
        
    }

}
