package de.gdiservice.bplan.dao;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.postgis.jts.JtsGeometry;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gdiservice.bplan.poi.PGExterneReferenz;
import de.gdiservice.bplan.poi.XPBereich;

import de.gdiservice.dao.AbstractDAO;
import de.gdiservice.util.DBUtil;

public abstract class XPBereichDAO<T extends XPBereich<?>>  extends AbstractDAO<UUID, T> {

    XPBereichDAO(Connection conWrite, Connection conRead, String tableName) {
        super(conWrite, conRead, tableName);
    }

    final static Logger logger = LoggerFactory.getLogger(XPBereichDAO.class);
    
    static String[] COLLUMN_NAMES = new String[] {
            "gml_id","nummer","name","bedeutung","detailliertebedeutung","erstellungsmassstab","geltungsbereich",
            "user_id","created_at","updated_at","konvertierung_id","planinhalt","praesentationsobjekt",
            "rasterbasis","refscan", "gehoertzuplan",      
    };
    
    
    abstract T createBereich();
    
    protected int setSQLParameter(Connection con, PreparedStatement stmt, XPBereich<?> bereich, int i) throws SQLException {

//      nummer integer NOT NULL,       
        stmt.setObject(i++, bereich.nummer);
//      name character varying COLLATE pg_catalog."default",
        stmt.setString(i++, bereich.name);
//      bedeutung xplan_gml.xp_bedeutungenbereich
        if (bereich.bedeutung != null) {
            PGobject pgObject = new PGobject();
            pgObject.setType("\"xplan_gml\".\"xp_bedeutungenbereich\"");
            pgObject.setValue(String.valueOf(bereich.bedeutung));
            stmt.setObject(i++, pgObject);
        } else {
            stmt.setObject(i++, null);            
        }        
//      detailliertebedeutung character varying COLLATE pg_catalog."default",
        stmt.setString(i++, bereich.detailliertebedeutung);
//      erstellungsmassstab integer,
        stmt.setObject(i++, bereich.erstellungsmassstab);        
//      geltungsbereich geometry(MultiPolygon),
        if (bereich.geltungsbereich!=null) {
            stmt.setObject(i++, new JtsGeometry(bereich.geltungsbereich));
        } else {
            stmt.setNull(i++, Types.OTHER);
        }     
//      user_id integer,
        stmt.setObject(i++, bereich.user_id);
//      created_at timestamp without time zone NOT NULL DEFAULT now(),
        stmt.setObject(i++, bereich.created_at);
//      updated_at timestamp without time zone NOT NULL DEFAULT now(),
        stmt.setObject(i++, bereich.updated_at);
//      konvertierung_id integer,
        stmt.setObject(i++, bereich.konvertierung_id);
//       planinhalt text COLLATE pg_catalog."default",
        stmt.setString(i++, bereich.planinhalt);
//      praesentationsobjekt text COLLATE pg_catalog."default",
        stmt.setString(i++, bereich.praesentationsobjekt);        
//      rasterbasis text COLLATE pg_catalog."default",
        stmt.setString(i++, bereich.rasterbasis);        
//      refscan xplan_gml.xp_externereferenz[],
        if (bereich.refscan instanceof PGExterneReferenz[]) {
                stmt.setArray(i++, con.createArrayOf("\"xplan_gml\".\"xp_externereferenz\"", bereich.refscan));    
        } else {
            stmt.setArray(i++, null);
        }     
//      UUID gehoertzuplan;
        stmt.setObject(i++, bereich.getGehoertzuplan());
        return i;
    }

    protected T createBereich(ResultSet rs) throws SQLException {
        int i = 1;
        T bereich = createBereich();
        bereich.gml_id = rs.getObject(i++, UUID.class);
//      nummer integer NOT NULL,       
        bereich.nummer = rs.getObject(i++, Integer.class);
//      name character varying COLLATE pg_catalog."default",
        bereich.name = rs.getString(i++);
//      bedeutung xplan_gml.xp_bedeutungenbereich
        Object o = rs.getObject(i++);        
        if (o instanceof String){            
            bereich.bedeutung = (String)o;
        } else if (o instanceof PGobject){
            bereich.bedeutung = BPlanDAO.getString((PGobject)o);
        }
//      detailliertebedeutung character varying COLLATE pg_catalog."default",
        bereich.detailliertebedeutung = rs.getString(i++);
//      erstellungsmassstab integer,
        bereich.erstellungsmassstab = rs.getObject(i++, Integer.class);        
//      geltungsbereich geometry(MultiPolygon),
        JtsGeometry pGobject = (JtsGeometry) rs.getObject(i++);          
        bereich.geltungsbereich = pGobject!=null ? pGobject.getGeometry() : null;
//      user_id integer,
        bereich.user_id = rs.getObject(i++, Integer.class);
//      created_at timestamp without time zone NOT NULL DEFAULT now(),
//        bereich.created_at = BPlanDAO.toLocalDate(rs.getDate(i++));
        bereich.created_at = rs.getObject(i++, LocalDate.class);
//      updated_at timestamp without time zone NOT NULL DEFAULT now(),
        bereich.updated_at = rs.getObject(i++, LocalDate.class);    
//      konvertierung_id integer,
        bereich.konvertierung_id = rs.getObject(i++, Integer.class);
//       planinhalt text COLLATE pg_catalog."default",
        bereich.planinhalt = rs.getString(i++);
//      praesentationsobjekt text COLLATE pg_catalog."default",
        bereich.praesentationsobjekt = rs.getString(i++);        
//      rasterbasis text COLLATE pg_catalog."default",
        bereich.rasterbasis = rs.getString(i++);        
//      refscan xplan_gml.xp_externereferenz[],
        Array ar = rs.getArray(i++);
        if (ar !=null) {
            bereich.refscan = BPlanDAO.getArray(ar, PGExterneReferenz[].class);
        }                
        bereich.setGehoertzuplan( rs.getObject(i++, UUID.class));
        return bereich;
    }

    /**
     * provides all Bereiche, belonging to the Plan with the give UUID, ordered by gmlId
     * 
     * 
     * @param gehoertzuplan to which the bereich is belonging 
     * @return ordered by gmlId List with BPBereichen, may be empty
     * @throws SQLException
     */
    public List<T> findByGehoertzuplan(UUID gehoertzuplan) throws SQLException {
        logger.debug("findByGehoertzuplan(\""+gehoertzuplan+"\") in table=" +tableName);
        List<T> bereiche = new ArrayList<>();
         
         PreparedStatement stmt = null;
         ResultSet rs = null;
         try {    
             String[] columns = getColumns(); 
             String sql = DBUtil.getSelectSQLString(tableName, columns, new String[]  {"gehoertzuplan=?"}) + " order by "+getKeyColumn();
             
             stmt = conRead.prepareStatement(sql);
             stmt.setObject(1, gehoertzuplan);
             rs = stmt.executeQuery();
             while (rs.next()) {
                 bereiche.add(createObject(rs));
             }
         } finally {
             if (rs!=null) {
                 try { rs.close(); } catch (SQLException e) {}
             }
             if (stmt!=null) {
                 try { stmt.close(); } catch (SQLException e) {}
             }
         }
         return bereiche;
     }    
    
    /**
     * provides all Bereiche with the specified konvertierungsId
     * 
     * @param konvertierungsId  
     * @return List with Bereichen, may be empty, not null
     * @throws SQLException
     */
    public List<T> findByKonvertierungsId(Integer konvertierungsId) throws SQLException {
        logger.debug("findByKonvertierungsId(\""+konvertierungsId+"\") in table=" +tableName);
        List<T> bereiche = new ArrayList<>();
         
         PreparedStatement stmt = null;
         ResultSet rs = null;
         try {    
             String[] columns = getColumns(); 
             String sql = DBUtil.getSelectSQLString(tableName, columns, new String[]  {"konvertierung_id=?"}) + " order by "+getKeyColumn();
             
             stmt = conRead.prepareStatement(sql);
             stmt.setObject(1, konvertierungsId);
             rs = stmt.executeQuery();
             while (rs.next()) {
                 bereiche.add(createObject(rs));
             }
         } finally {
             if (rs!=null) {
                 try { rs.close(); } catch (SQLException e) {}
             }
             if (stmt!=null) {
                 try { stmt.close(); } catch (SQLException e) {}
             }
         }
         return bereiche;
     }       

    public int getColumnCount() {
        return COLLUMN_NAMES.length;
    }
    
 
    public int delete(T bereich) throws SQLException {
        PreparedStatement stmt = null;

        try {           
            String sql = "delete from "+tableName+" where "+ getKeyColumn() + "= ?";
            logger.debug(sql);
            stmt = conWrite.prepareStatement(sql);
            stmt.setObject(1, bereich.getGml_id());
            try {
                return stmt.executeUpdate();
            }
            catch (SQLException ex) {
                logger.info("stmt={}", stmt);
                logger.error("Fehler writing: \""+bereich+"\"", ex);
                throw ex;
            }
        }
        finally {
            if (stmt!=null) {
                try { stmt.close(); } catch (SQLException e) {}
            }
        }         
    }

    public int delete(List<T> bereiche) throws SQLException {
        int i = 0;
        for (T bereich : bereiche) {
            i = i + this.delete(bereich);
        }        
        return i;
    }        
   
    
}
