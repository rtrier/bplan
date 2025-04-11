package de.gdiservice.bplan.konvertierung;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GemeindeDAO {

    final static Logger logger = LoggerFactory.getLogger(GemeindeDAO.class);

    final String[] COLUMN_NAMES = new String[] {
            "id_amt", "amt_name", "id_gmd", "rs", "ags", "gmd_name", "id_ot", "ot_name", "stelle_id"
        };


    private final Connection con;
   

    public GemeindeDAO(Connection con) {
        this.con = con;
    }
    
    
    public Gemeinde find(String rs, String ags, String gmd_name, String ot_name) throws SQLException {
        if (rs == null || rs.length()==0) {
            throw new IllegalArgumentException("In der Gemeinde fehlt das Attribut rs.");
        }
        if (ags == null || ags.length()==0) {
            throw new IllegalArgumentException("In der Gemeinde fehlt das Attribut ags.");
        }
        if (gmd_name == null || gmd_name.length()==0) {
            throw new IllegalArgumentException("In der Gemeinde fehlt das Attribut gmd_name.");
        }
        try {
            int iAgs = Integer.parseInt(ags);
            List<Gemeinde> gemeinden =  find(rs, iAgs, gmd_name, ot_name);
            return (gemeinden != null && gemeinden.size()>0) ? gemeinden.get(0) : null;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Das Attribut ags in der Gemeinde ist keine Zahl.");
        }
        
    }
    
//    public List<Gemeinde> find(String rs, String ags, String gmd_name, String ot_name) throws SQLException {
//        if (rs == null || rs.length()==0) {
//            throw new IllegalArgumentException("In der Gemeinde fehlt das Attribut rs.");
//        }
//        if (ags == null || ags.length()==0) {
//            throw new IllegalArgumentException("In der Gemeinde fehlt das Attribut ags.");
//        }
//        if (gmd_name == null || gmd_name.length()==0) {
//            throw new IllegalArgumentException("In der Gemeinde fehlt das Attribut gmd_name.");
//        }
//        try {
//            int iAgs = Integer.parseInt(ags);
//            return find(rs, iAgs, gmd_name, ot_name);
//        } catch (NumberFormatException ex) {
//            throw new IllegalArgumentException("Das Attribut ags in der Gemeinde ist keine Zahl.");
//        }
//        
//    }
    
    public List<Gemeinde> find(String rs, Integer ags, String gmd_name, String ot_name) throws SQLException {        
        List<Gemeinde> gemeinden = new ArrayList<>();
        
        List<String> whereClauses = new ArrayList<>(4);
        if (rs!=null) {
            whereClauses.add("rs=?");
        }
        if (ags!=null) {
            whereClauses.add("ags=?");
        }
//        if (gmd_name!=null) {
//            whereClauses.add("gmd_name=?");
//        }
//        if (ot_name!=null) {
//            whereClauses.add("ot_name=?");
//        }
        
        PreparedStatement stmt = null;
        ResultSet resultset = null;
        try {    
            StringBuilder sb = new StringBuilder("select rs,ags,gem_name, gtl_name, stelle_id  "
                    + "from gebietseinheiten.gemeindeteile t "
                    + "left join gebietseinheiten.gemeinden g on g.gem_schl=t.gem_schl");
            
            
            if (whereClauses!=null && whereClauses.size()>0) {
                sb.append(" where ").append(whereClauses.get(0));
                for (int i=1, count=whereClauses.size(); i<count; i++) {
                    sb.append(" and ").append(whereClauses.get(i));
                }      
            }
            
                    
            // String sql = DBUtil.getSelectSQLString(tableName, COLUMN_NAMES, whereClauses.toArray(new String[whereClauses.size()]));      

            stmt = con.prepareStatement(sb.toString());
            int i=1;
            if (rs!=null) {
                stmt.setObject(i++, rs);
            }
            if (ags!=null) {
                stmt.setObject(i++, String.valueOf(ags));    
            }
//            if (gmd_name!=null) {
//                stmt.setObject(i++, gmd_name);
//            }
//            if (ot_name!=null) {
//                stmt.setObject(i++, ot_name);
//            }
//            logger.debug("{}", stmt);
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
        
        if (gemeinden.size()==0) {
            return gemeinden;
        }
        
        Set<String> gemeindeNamen = new TreeSet<>();
        Set<String> otNamen = new TreeSet<>();
        for (Gemeinde gemeinde: gemeinden) {
            gemeindeNamen.add(gemeinde.gmd_name);
            otNamen.add(gemeinde.ot_name);
        }
        
        if (!gemeindeNamen.contains(gmd_name)) {
            throw new IllegalArgumentException("Gemeindename \""+gmd_name+"\" nicht gültig. Mögliche Werte: "+toString(gemeindeNamen));
        }
        if (ot_name !=null && ot_name.length()>0 && !otNamen.contains(ot_name)) {
            throw new IllegalArgumentException("Ortsteilname \""+ot_name+"\" nicht gültig. Mögliche Werte: "+toString(otNamen));
        }
        
        return gemeinden;
    }
    
    
    private String toString(Collection<String> c) {
        StringBuilder sb = new StringBuilder();
        for (String s: c) {
            if (sb.length()>0) {
                sb.append(", ");
            }
            sb.append('"').append(s).append('"');
        }
        return sb.toString();
        
    }



    private Gemeinde createGemeinde(ResultSet resultset) throws SQLException {
        Gemeinde gemeinde = new Gemeinde();
        int i=1;
        
        gemeinde.rs = resultset.getString(i++);
        gemeinde.ags = resultset.getString(i++);
        gemeinde.gmd_name = resultset.getString(i++);
        gemeinde.ot_name = resultset.getString(i++);
        gemeinde.stelle_id = (Integer)resultset.getObject(i++);
        
//        gemeinde.id_amt = (Integer)resultset.getObject(i++);
//        gemeinde.amt_name = resultset.getString(i++);;
//        gemeinde.id_gmd = (Integer)resultset.getObject(i++);
//        gemeinde.rs = resultset.getString(i++);
//        gemeinde.ags = (Integer)resultset.getObject(i++);
//        gemeinde.gmd_name = resultset.getString(i++);
//        gemeinde.id_ot = (Integer)resultset.getObject(i++);
//        gemeinde.ot_name = resultset.getString(i++);
//        gemeinde.stelle_id = (Integer)resultset.getObject(i++);        
        return gemeinde;
    }     

}
