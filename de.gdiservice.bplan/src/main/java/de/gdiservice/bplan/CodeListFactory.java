package de.gdiservice.bplan;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CodeListFactory {
    
    final static Logger logger = LoggerFactory.getLogger(CodeListFactory.class);

    private static CodeListFactory INSTANCE;
    
    Connection con;
    //          Table       Codespace   Id 
    private Map<String, Map<String, Map<String, CodeList>>> cache = new HashMap<>();
    
    
    public CodeListFactory(Connection con) {
        this.con = con;
        CodeListFactory.INSTANCE = this;
    }
    
    public static CodeList getCodeList(String table, String codespace, String id) throws SQLException {
        if (INSTANCE == null) {
          throw new IllegalStateException("CodeListFactory not initialised");  
        }
        Map<String, Map<String, CodeList>> tableMap = INSTANCE.cache.get(table);
        if (tableMap == null) {
            tableMap = INSTANCE.getCodeList(table);
            INSTANCE.cache.put(table, tableMap);
        }
        Map<String, CodeList> codespaceMap = tableMap.get(codespace);
        return (codespaceMap != null) ? codespaceMap.get(id) : null;
        
    }
    
    
    private Map<String, Map<String, CodeList>> getCodeList(String table) throws SQLException {
        ArrayList<CodeList> l = new ArrayList<CodeList>();
        
        Statement stmt = null;
        ResultSet rs = null;
        try {      
            stmt = con.createStatement();
            String sql = "select codespace, id, value from " + table;
            logger.debug(sql);
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                l.add(new CodeList(rs.getString(1), rs.getString(2), rs.getString(3)));
            }
            Map<String, Map<String, CodeList>> m = new HashMap<>();
            for (CodeList cl : l) {
                Map<String, CodeList> codespaceMap = m.get(cl.codespace);
                if (codespaceMap == null) {
                    codespaceMap = new HashMap<>();
                    m.put(cl.codespace, codespaceMap);
                }
                codespaceMap.put(cl.id, cl);
            }
            return m;
            
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
}
