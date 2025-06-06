package de.gdiservice.bplan.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gdiservice.bplan.poi.BPBereich;
import de.gdiservice.bplan.poi.BPlan;
import de.gdiservice.bplan.poi.XPBereich;
import de.gdiservice.dao.AbstractDAO;

public class BPBereichDAO extends XPBereichDAO<BPBereich> {
    
    final static Logger logger = LoggerFactory.getLogger(BPBereichDAO.class);

    final static String[] COLUMN_NAMES = new String[] {            
            "versionbaunvodatum","versionbaugbtext","versionsonstrechtsgrundlagetext",
            "versionbaunvotext","versionsonstrechtsgrundlagedatum","versionbaugbdatum"      
    };
        
    
    final static String KEY_COLUMN = "gml_id";

//    private final Connection conWrite;
//    private final Connection conRead;
//
//    private final String tableName;

    public BPBereichDAO(Connection conWrite, Connection conRead, String tableName) {
        super(conWrite, conRead, tableName);
    }
    
    
  

//    @Override
//    public void insert(BPBereich bereich) throws SQLException {
//        PreparedStatement stmt = null;
//
//        try {           
//            String[] columns = concatenate(XPBereichDAO.COLLUMN_NAMES, BPBereichDAO.COLUMN_NAMES); 
//            String sql = DBUtil.getInsertSQLString(tableName, columns);
//            stmt = conWrite.prepareStatement(sql);
//
//            int i = 1;
//            stmt.setObject(i++, bereich.gml_id);            
//            setSQLParameter(conWrite, stmt, bereich, i);
//            
//            try {
//                stmt.execute();
//            }
//            catch (SQLException ex) {
//                logger.error("Fehler in Statement: "+stmt.toString());
//                throw ex;
//            }
//
//        }
//        finally {
//            if (stmt!=null) {
//                try { stmt.close(); } catch (SQLException e) {}
//            }
//        }
//    }  

    
    protected int setSQLParameter(Connection con, PreparedStatement stmt, BPBereich bereich, int i) throws SQLException {
        i = super.setSQLParameter(con, stmt, (XPBereich<BPlan>)bereich, i);
//      LocalDate versionbaunvodatum;
        stmt.setObject(i++, bereich.versionbaunvodatum);
//      String versionbaugbtext;
        stmt.setString(i++, bereich.versionbaugbtext);
//      String versionsonstrechtsgrundlagetext;
        stmt.setString(i++, bereich.versionsonstrechtsgrundlagetext);
//      String versionbaunvotext;
        stmt.setString(i++, bereich.versionbaunvotext);
//      LocalDate versionsonstrechtsgrundlagedatum;
        stmt.setObject(i++, bereich.versionsonstrechtsgrundlagedatum);
//      LocalDate versionbaugbdatum;
        stmt.setObject(i++, bereich.versionbaugbdatum);
        return i;
    }
    

//    public BPBereich findById(UUID gmlId) throws SQLException {
//       logger.debug("findById(\""+gmlId+"\") in table=" +tableName);
//       BPBereich bereich = null;
//        
//        PreparedStatement stmt = null;
//        ResultSet rs = null;
//        try {    
//            String[] columns = concatenate(XPBereichDAO.COLLUMN_NAMES, BPBereichDAO.COLUMN_NAMES); 
//            String sql = DBUtil.getSelectSQLString(tableName, columns, new String[]  {"gml_id=?"});      
//            stmt = conRead.prepareStatement(sql);
//            stmt.setObject(1, gmlId);
//            rs = stmt.executeQuery();
//            if (rs.next()) {
//                bereich = createBereich(rs);
//            }
//        } finally {
//            if (rs!=null) {
//                try { rs.close(); } catch (SQLException e) {}
//            }
//            if (stmt!=null) {
//                try { stmt.close(); } catch (SQLException e) {}
//            }
//        }
//        return bereich;
//    }  
    

    
    
 
    
//    public List<BPBereich> findAll() throws SQLException {
//        return findAll(null, null);
//    }
//
//    public List<BPBereich> findAll(String[] whereClauses, Integer maxCount) throws SQLException {
//        logger.debug("findAll {}", tableName);
//        List<BPBereich> bereiche= new ArrayList<>();
//
//        Statement stmt = null;
//        ResultSet rs = null;
//        try {      
//            stmt = conRead.createStatement();
//            String[] columns = concatenate(XPBereichDAO.COLLUMN_NAMES, BPBereichDAO.COLUMN_NAMES); 
//            String sql = DBUtil.getSelectSQLString(tableName, columns, whereClauses, maxCount);
//            logger.debug(sql);
//            rs = stmt.executeQuery(sql);
//            while (rs.next()) {
//                bereiche.add(createBereich(rs));
//            }
//            return bereiche;
//        }
//        finally {
//            if (rs!=null) {
//                try { rs.close(); } catch (SQLException e) {}
//            }
//            if (stmt!=null) {
//                try { stmt.close(); } catch (SQLException e) {}
//            }
//        }
//    }    
    protected BPBereich createObject(ResultSet rs) throws SQLException {        
        BPBereich bereich = super.createBereich(rs);
        try {
            int i = super.getColumnCount() + 1; 
    //      LocalDate versionbaunvodatum;
            bereich.versionbaunvodatum = rs.getObject(i++, LocalDate.class);
    //      String versionbaugbtext;
            bereich.versionbaugbtext = rs.getString(i++);
    //      String versionsonstrechtsgrundlagetext;
            bereich.versionsonstrechtsgrundlagetext = rs.getString(i++);
    //      String versionbaunvotext;
            bereich.versionbaunvotext = rs.getString(i++);
    //      LocalDate versionsonstrechtsgrundlagedatum;
            bereich.versionsonstrechtsgrundlagedatum = rs.getObject(i++, LocalDate.class);
    //      LocalDate versionbaugbdatum;
            bereich.versionbaugbdatum = rs.getObject(i++, LocalDate.class);
    //      UUID gehoertzuplan;
//            bereich.setGehoertzuplan( rs.getObject(i++, UUID.class));
            return bereich;
        } catch (Exception e) {                   
            throw new SQLException("Error interpreting Data gml_id=\""+bereich.gml_id+"\"", e);
        }
    }


//    public int update(BPBereich bereich) throws SQLException {
//        
//        PreparedStatement stmt = null;
//
//        try {           
//            String[] columns = concatenate(XPBereichDAO.COLLUMN_NAMES, BPBereichDAO.COLUMN_NAMES); 
//            String sql = DBUtil.getUpdateSQLString(tableName, columns, KEY_COLUMN);
////            logger.debug(sql);
//            stmt = conWrite.prepareStatement(sql);
//
//            int i = setSQLParameter(conWrite, stmt, bereich, 1);
//            // where clause
//            stmt.setObject(i++, bereich.gml_id);
//            try {
//                return stmt.executeUpdate();
//            }
//            catch (SQLException ex) {
//                logger.info("stmt={}", stmt);
//                logger.error("Fehler writing: \""+bereich+"\"", ex);
//                throw ex;
//            }
//        }
//        finally {
//            if (stmt!=null) {
//                try { stmt.close(); } catch (SQLException e) {}
//            }
//        }    
//    }




    @Override
    BPBereich createBereich() {        
        return new BPBereich();
    }

    


    @Override
    public String[] getColumns() {
        return AbstractDAO.concatenate(XPBereichDAO.COLLUMN_NAMES, BPBereichDAO.COLUMN_NAMES); 
    }


    @Override
    public String getKeyColumn() {
        return BPBereichDAO.KEY_COLUMN;
    }




}
