package de.gdiservice.bplan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gdiservice.util.DBUtil;

public class LogDAO {

    final static Logger logger = LoggerFactory.getLogger(LogDAO.class);

    final static String[] COLLUMN_NAMES = new String[] {
            "time", "stelle_id", "protocol"
    };


    private final Connection con;
    private final String tableName;

    public LogDAO(Connection con, String tableName) {
        this.con = con;        
        this.tableName = tableName;
    }



    public void insert(OffsetDateTime time, int stellenId, String text) throws SQLException {
        
        PreparedStatement stmt = null;

        try {           
            String sql = DBUtil.getInsertSQLString(tableName, COLLUMN_NAMES);
            stmt = con.prepareStatement(sql);

            int i = 1;

            stmt.setObject(i++, time);
            stmt.setInt(i++, stellenId);
            stmt.setString(i++, text);
            logger.info("insertLog {}", text);
            logger.info("insertLog {}", stmt);
            try {
                stmt.execute();
            }
            catch (SQLException ex) {
                ex.printStackTrace();
                throw ex;
            }
        }
        finally {
            if (stmt!=null) {
                try { stmt.close(); } catch (SQLException e) {}
            }
        }
    }
}
