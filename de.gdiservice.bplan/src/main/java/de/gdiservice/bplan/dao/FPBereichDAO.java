package de.gdiservice.bplan.dao;

import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gdiservice.bplan.poi.FPBereich;
import de.gdiservice.bplan.poi.FPlan;
import de.gdiservice.bplan.poi.XPBereich;


public class FPBereichDAO extends XPBereichDAO<FPBereich> {
    
    final static Logger logger = LoggerFactory.getLogger(FPBereichDAO.class);

    final static String[] COLUMN_NAMES = new String[] {            
            "versionbaunvodatum","versionbaugbtext","versionsonstrechtsgrundlagetext",
            "versionbaunvotext","versionsonstrechtsgrundlagedatum","versionbaugbdatum"      
    };
        
    
    final static String KEY_COLUMN = "gml_id";

//    private final Connection conWrite;
//    private final Connection conRead;
//
//    private final String tableName;

    public FPBereichDAO(Connection conWrite, Connection conRead, String tableName) {
        super(conWrite, conRead, tableName);
    }
    
    
    static public <T> T[] concatenate(T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
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

    
    protected int setSQLParameter(Connection con, PreparedStatement stmt, FPBereich bereich, int i) throws SQLException {
        i = super.setSQLParameter(con, stmt, (XPBereich<FPlan>)bereich, i);
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
//      UUID gehoertzuplan;
//        stmt.setObject(i++, bereich.getGehoertzuplan());
        return i;
    }
    

    
    protected FPBereich createObject(ResultSet rs) throws SQLException {      
        FPBereich bereich = super.createBereich(rs);
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
    //      bereich.setGehoertzuplan( rs.getObject(i++, UUID.class));
            return bereich;
        } catch (Exception e) {                   
            throw new SQLException("Error interpreting Data gml_id=\""+bereich.gml_id+"\"", e);
        }
    }







    @Override
    FPBereich createBereich() {        
        return new FPBereich();
    }

    


    @Override
    public String[] getColumns() {
        return concatenate(XPBereichDAO.COLLUMN_NAMES, FPBereichDAO.COLUMN_NAMES); 
    }


    @Override
    public String getKeyColumn() {
        return FPBereichDAO.KEY_COLUMN;
    }




}
