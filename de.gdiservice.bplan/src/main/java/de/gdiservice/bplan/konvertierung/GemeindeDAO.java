package de.gdiservice.bplan.konvertierung;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gdiservice.util.DBUtil;

public class GemeindeDAO {

    final static Logger logger = LoggerFactory.getLogger(GemeindeDAO.class);

    final String[] COLUMN_NAMES = new String[] {
            "id_amt", "amt_name", "id_gmd", "rs", "ags", "gmd_name", "id_ot", "ot_name", "stelle_id"
        };


    private final Connection con;
    private final String tableName;

    public GemeindeDAO(Connection con, String tableName) {
        this.con = con;
        this.tableName = tableName;
    }
    
    public List<Gemeinde> find(String rs, Integer ags, String gmd_name, String ot_name) throws SQLException {        
        List<Gemeinde> gemeinden = new ArrayList<>();
        
        List<String> whereClauses = new ArrayList<>(4);
        if (rs!=null) {
            whereClauses.add("rs=?");
        }
        if (ags!=null) {
            whereClauses.add("ags=?");
        }
        if (gmd_name!=null) {
            whereClauses.add("gmd_name=?");
        }
        if (ot_name!=null) {
            whereClauses.add("ot_name=?");
        }
        
        PreparedStatement stmt = null;
        ResultSet resultset = null;
        try {    
            String sql = DBUtil.getSelectSQLString(tableName, COLUMN_NAMES, whereClauses.toArray(new String[whereClauses.size()]));      

            stmt = con.prepareStatement(sql);
            int i=1;
            if (rs!=null) {
                stmt.setObject(i++, rs);
            }
            if (ags!=null) {
                stmt.setObject(i++, ags);    
            }
            if (gmd_name!=null) {
                stmt.setObject(i++, gmd_name);
            }
            if (ot_name!=null) {
                stmt.setObject(i++, ot_name);
            }
            logger.debug("{}", stmt);
            resultset = stmt.executeQuery();
            while (resultset.next()) {
                gemeinden.add(createGemeinde(resultset));
            }
        } finally {
            if (resultset!=null) {
                try { resultset.close(); } catch (SQLException e) {}
            }
            if (stmt!=null) {
                try { stmt.close(); } catch (SQLException e) {}
            }
        }
        return gemeinden;
    }
    



    private Gemeinde createGemeinde(ResultSet resultset) throws SQLException {
        Gemeinde gemeinde = new Gemeinde();
        int i=1;
        gemeinde.id_amt = (Integer)resultset.getObject(i++);
        gemeinde.amt_name = resultset.getString(i++);;
        gemeinde.id_gmd = (Integer)resultset.getObject(i++);
        gemeinde.rs = resultset.getString(i++);
        gemeinde.ags = (Integer)resultset.getObject(i++);
        gemeinde.gmd_name = resultset.getString(i++);
        gemeinde.id_ot = (Integer)resultset.getObject(i++);
        gemeinde.ot_name = resultset.getString(i++);
        gemeinde.stelle_id = (Integer)resultset.getObject(i++);        
        return gemeinde;
    }     

}
