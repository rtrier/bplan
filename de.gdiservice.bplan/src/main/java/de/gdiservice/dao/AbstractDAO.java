package de.gdiservice.dao;

import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gdiservice.util.DBUtil;


public abstract class AbstractDAO<K extends Object, T extends KeyHolder<K>> {

    final static Logger logger = LoggerFactory.getLogger(AbstractDAO.class);
    
    protected final Connection conWrite;
    protected final Connection conRead;

    protected final String tableName;
    
    public AbstractDAO(Connection conWrite, Connection conRead, String tableName) {
        this.conWrite = conWrite;
        this.conRead = conRead;
        this.tableName = tableName;
    }
    
    public abstract String[] getColumns();
    
    public abstract String getKeyColumn();
    
    public List<T> findAll() throws SQLException {
        return findAll(null, null);
    }

    public List<T> findAll(String[] whereClauses, Integer maxCount) throws SQLException {
        logger.debug("findAll {}", tableName);
        List<T> bPlans = new ArrayList<>();

        Statement stmt = null;
        ResultSet rs = null;
        try {      
            stmt = conRead.createStatement();
            String sql = DBUtil.getSelectSQLString(tableName, getColumns(), whereClauses, maxCount);
            logger.debug(sql);
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                bPlans.add(createObject(rs));
            }
            return bPlans;
        }
        finally {
            if (rs!=null) {
                try { rs.close(); } catch (SQLException e) {}
            }
            if (stmt!=null) {
                try { stmt.close(); } catch (SQLException e) {}
            }
        }
    }
    
    public T findById(UUID gmlId) throws SQLException {
        logger.debug("findById "+tableName);
        T fplan = null;
         
         PreparedStatement stmt = null;
         ResultSet rs = null;
         try {    
             String sql = DBUtil.getSelectSQLString(tableName, getColumns(), new String[]  { getKeyColumn()+"=?"});      
//             logger.debug(sql);
             stmt = conRead.prepareStatement(sql);
             stmt.setObject(1, gmlId);
             rs = stmt.executeQuery();
             if (rs.next()) {
                 fplan = createObject(rs);
             }
         } finally {
             if (rs!=null) {
                 try { rs.close(); } catch (SQLException e) {}
             }
             if (stmt!=null) {
                 try { stmt.close(); } catch (SQLException e) {}
             }
         }
         return fplan;
     }    
    
    protected abstract T createObject(ResultSet rs) throws SQLException;

    public void insert(T plan) throws SQLException {
        PreparedStatement stmt = null;

        try {           
            String sql = DBUtil.getInsertSQLString(tableName, this.getColumns());
            stmt = conWrite.prepareStatement(sql);

            int i = 1;
            stmt.setObject(i++, plan.getKey());            
            setSQLParameter(conWrite, stmt, plan, i);
            
            try {            
                logger.debug("stmt: "+stmt.toString().substring(0, 150)+"...");
                stmt.execute();
            }
            catch (SQLException ex) {
                throw ex;
            }

        }
        finally {
            if (stmt!=null) {
                try { stmt.close(); } catch (SQLException e) {}
            }
        }
    }
    
    public int update(T fplan) throws SQLException {
        
        PreparedStatement stmt = null;

        try {           
            String sql = DBUtil.getUpdateSQLString(tableName, getColumns(), getKeyColumn());
            stmt = conWrite.prepareStatement(sql);

            int i = setSQLParameter(conWrite, stmt, fplan, 1);
            // where clause
            stmt.setObject(i++, fplan.getKey());
            
            
            try {
                logger.info("stmt={}", stmt.toString().substring(0, 150)+"...");
                return stmt.executeUpdate();
            }
            catch (SQLException ex) {
                logger.error("Fehler writing: \""+fplan+"\"", ex);
                throw ex;
            }
        }
        finally {
            if (stmt!=null) {
                try { stmt.close(); } catch (SQLException e) {}
            }
        }    
    }
    

    
    
    public final String validateGeom(Geometry geom) throws SQLException {
        PreparedStatement stmt = null;
        String result = null;
        try {
            stmt = conWrite.prepareStatement("select public.st_isvalidreason(?)");
            WKTWriter writer = new WKTWriter(2);
            stmt.setObject(1, writer.write(geom), Types.OTHER);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                result = rs.getString(1);
            }
        }
        finally {
            if (stmt!=null) {
                try { stmt.close(); } catch (SQLException e) {}
            }
        } 
        return result;
    }    
    

    protected abstract int setSQLParameter(Connection con, PreparedStatement stmt, T plan, int i) throws SQLException;
    
    
    public static <T> T[] concatenate(T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }  
    
}
