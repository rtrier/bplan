package de.gdiservice.bplan;

import java.sql.Array;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTWriter;
import org.postgis.jts.JtsGeometry;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gdiservice.util.DBUtil;



public class BPlanDAO {

    final static Logger logger = LoggerFactory.getLogger(BPlanDAO.class);

    final static String[] COLLUMN_NAMES = new String[] {            
            "gml_id","name","nummer","gemeinde","externereferenz","inkrafttretensdatum", "auslegungsstartdatum", "auslegungsenddatum", "rechtsstand","planart","raeumlichergeltungsbereich","konvertierung_id","internalid","aendert","wurdegeaendertvon"
    };
    

    final static String KEY_COLLUMN = "gml_id";

    private final Connection con;

    private final String tableName;

    public BPlanDAO(Connection con, String tableName) {
        this.con = con;
        this.tableName = tableName;
    }




    public void insert(BPlan bplan) throws SQLException {
        PreparedStatement stmt = null;

        try {           
            String sql = DBUtil.getInsertSQLString(tableName, COLLUMN_NAMES);
            logger.debug(sql);
            stmt = con.prepareStatement(sql);

            int i = 1;

            stmt.setObject(i++, bplan.gml_id);
            stmt.setString(i++, bplan.name);
            stmt.setString(i++, bplan.nummer);


            if (bplan.gemeinde != null) {
                stmt.setArray(i++, con.createArrayOf("\"xplan_gml\".\"xp_gemeinde\"", bplan.gemeinde));
            } else {
                stmt.setArray(i++, null);
            }

            if (bplan.externeReferenzes != null) {
                stmt.setArray(i++, con.createArrayOf("\"xplan_gml\".\"xp_spezexternereferenz\"", bplan.externeReferenzes));
            } else {
                stmt.setArray(i++, null);
            }        

            if (bplan.inkrafttretensdatum != null) {
                stmt.setDate(i++, new java.sql.Date(bplan.inkrafttretensdatum.getTime()));
            } else {
                stmt.setObject(i++, null);
            }
            
            if (bplan.auslegungsstartdatum != null && bplan.auslegungsstartdatum.length>0) {
                stmt.setArray(i++, con.createArrayOf("DATE", bplan.auslegungsstartdatum));
            } else {
                stmt.setArray(i++, null);
            }
            if (bplan.auslegungsenddatum != null && bplan.auslegungsenddatum.length>0) {
                stmt.setArray(i++, con.createArrayOf("DATE", bplan.auslegungsenddatum));
            } else {
                stmt.setArray(i++, null);
            }
            

            if (bplan.rechtsstand != null) {
                PGobject pgObject = new PGobject();
                pgObject.setType("\"xplan_gml\".\"bp_rechtsstand\"");
                pgObject.setValue(bplan.rechtsstand);
                stmt.setObject(i++, pgObject);
            } else {
                stmt.setObject(i++, null);
            }

            // bplan.setPlanart((PGobject)rs.getObject(i++));
            //        System.out.println("bplan.planart "+bplan.planart);
            if (bplan.planart !=null ) {
                stmt.setArray(i++,getPGArray(con, "\"xplan_gml\".\"bp_planart\"", bplan.planart));
            } else {
                stmt.setObject(i++, null);
            }

            if (bplan.geom!=null) {
                stmt.setObject(i++, new JtsGeometry(bplan.geom));
            } else {
                stmt.setString(i++, null);
            }
            
            
            stmt.setObject(i++, bplan.konvertierung_id);


            stmt.setObject(i++, bplan.internalid); 
            
            if (bplan.aendert != null) {
                stmt.setArray(i++, con.createArrayOf("\"xplan_gml\".\"xp_verbundenerplan\"", bplan.aendert));
            } else {
                stmt.setArray(i++, null);
            }
            if (bplan.wurdegeaendertvon != null) {
                stmt.setArray(i++, con.createArrayOf("\"xplan_gml\".\"xp_verbundenerplan\"", bplan.wurdegeaendertvon));
            } else {
                stmt.setArray(i++, null);
            }
            
            try {
                logger.debug("stmt: "+stmt.toString());
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


    public BPlan findById(UUID gmlId) throws SQLException {
       logger.debug("findById "+tableName);
        BPlan bplan = null;
        
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {    
            String sql = DBUtil.getSelectSQLString(tableName, COLLUMN_NAMES, new String[]  {"gml_id=?"});      
            logger.debug(sql);
            stmt = con.prepareStatement(sql);
            stmt.setObject(1, gmlId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                bplan = createBPlan(rs);
            }
        } finally {
            if (rs!=null) {
                try { rs.close(); } catch (SQLException e) {}
            }
            if (stmt!=null) {
                try { stmt.close(); } catch (SQLException e) {}
            }
        }
        return bplan;
    }  
    
    public List<BPlan> findByInternalIdLikeGmlId(UUID gmlId) throws SQLException {
        logger.debug("findById "+tableName);

         List<BPlan> plaene = new ArrayList<>();
         
         PreparedStatement stmt = null;
         ResultSet rs = null;
         try {    
             String sql = DBUtil.getSelectSQLString(tableName, COLLUMN_NAMES, new String[]  {"internalid like ? or gml_id=?"});
             sql = sql + " order by internalid";
             logger.debug(sql);
             stmt = con.prepareStatement(sql);
             stmt.setObject(1, gmlId.toString()+"%");
             stmt.setObject(2, gmlId);
             rs = stmt.executeQuery();
             while (rs.next()) {
                 BPlan bplan = createBPlan(rs);
                 plaene.add(bplan);
             }
         } finally {
             if (rs!=null) {
                 try { rs.close(); } catch (SQLException e) {}
             }
             if (stmt!=null) {
                 try { stmt.close(); } catch (SQLException e) {}
             }
         }
         return plaene;

     }  
    
    

    public List<BPlan> findAll() throws SQLException {
        return findAll(null, null);
    }

    public List<BPlan> findAll(String[] whereClauses, Integer maxCount) throws SQLException {
        logger.debug("findAll {}", tableName);
        List<BPlan> bPlans = new ArrayList<>();

        Statement stmt = null;
        ResultSet rs = null;
        try {      
            stmt = con.createStatement();
            String sql = DBUtil.getSelectSQLString(tableName, COLLUMN_NAMES, whereClauses, maxCount);
            logger.debug(sql);
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                bPlans.add(createBPlan(rs));
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
    
    BPlan createBPlan(ResultSet rs) throws SQLException {
        // "gml_id","name","nummer","gemeinde","externereferenz","inkrafttretensdatum", "auslegungsstartdatum[]", "auslegungsenddatum[]",
        //                                                                             "rechtsstand","planart","raeumlichergeltungsbereich","konvertierung_id","internalid","aendert","wurdegeaendertvon"
        // "gml_id","name","nummer","gemeinde","externereferenz","inkrafttretensdatum","rechtsstand","planart","raeumlichergeltungsbereich","konvertierung_id","internalid","aendert","wurdegeaendertvon"
        int i=1;
        BPlan bplan = new BPlan();
        bplan.gml_id = rs.getObject(i++, UUID.class);
        bplan.name = rs.getString(i++);
        bplan.nummer = rs.getString(i++);

        try {

            bplan.gemeinde = getArray(rs.getArray(i++), Gemeinde[].class);
            bplan.externeReferenzes = getArray(rs.getArray(i++), PGExterneReferenz[].class);          

            bplan.inkrafttretensdatum = rs.getDate(i++);
            
            bplan.auslegungsstartdatum = getDates(rs.getArray(i++));
            bplan.auslegungsenddatum = getDates(rs.getArray(i++));            
            
            Object o = rs.getObject(i++);
            if (o instanceof String) {
                bplan.rechtsstand = (String)o;
            } else if (o instanceof PGobject){
                bplan.rechtsstand = getString((PGobject)o);
            }


            bplan.planart = getStrings(rs.getArray(i++));


            JtsGeometry pGobject = (JtsGeometry) rs.getObject(i++);          
            bplan.geom = pGobject!=null ? pGobject.getGeometry() : null;
            
            bplan.konvertierung_id = (Integer)rs.getObject(i++);
            
            
            bplan.internalid = rs.getString(i++); 
            bplan.aendert = getArray(rs.getArray(i++), PGVerbundenerPlan[].class);
            bplan.wurdegeaendertvon = getArray(rs.getArray(i++), PGVerbundenerPlan[].class);
            
            return bplan;
         
        } 
        catch (Exception e) {                   
            throw new SQLException("Error interpreting Data gml_id=\""+bplan.gml_id+"\"", e);
        }
    }



    private static <T extends Object> T [] getArray(Array array, Class<? extends T[]> clasz) throws SQLException {
        try {
            if (array !=null) {
                Object[] objects = (Object[]) array.getArray();
//                System.err.println(objects[0].getClass());
                return (objects != null) ? Arrays.copyOf(objects, objects.length, clasz) : null;
            }
            return null;
        } 
        catch (Exception ex) {
            throw new SQLException("Fehler:" + clasz, ex);
        }
    }

    private static Array getPGArray(Connection con, String typeName, String[] objects) throws SQLException {    
        final int count = objects.length;
        PGobject[] pGobjects = new PGobject[count];
        for (int i=0; i<count; i++) {
            pGobjects[i] = new PGobject();
            pGobjects[i].setType(typeName);
            pGobjects[i].setValue(objects[i]);
        }
        return con.createArrayOf(typeName, pGobjects);
    }  

    private static String getString(PGobject pgObject) {
        return pgObject == null ? null : pgObject.getValue();
    }
//    private static Date getDate(PGobject pgObject) {
//        return pgObject == null ? null : pgObject.getValue();
//    }

    private static String[] getStrings(Array array) throws SQLException {
//        System.err.println("getStrings =\""+array+"\"");
        if (array != null) {
            Object[] pgObjects = (Object[])array.getArray();
            if (pgObjects != null) {
                final int count = pgObjects.length;
                String[] strings = new String[count];
                if (count>0) {
                    for (int i=0; i<count; i++) {
                        strings[i] = getString((PGobject)pgObjects[i]);
                    }
                }
                return strings;
            }
        }
        return null;
    }

    private static Date[] getDates(Array array) throws SQLException {
      if (array != null) {
          return (java.sql.Date[])array.getArray();
      }
      return null;
  }    



    @SuppressWarnings("unused")
    private void truncate() throws SQLException {
        Statement stmt = con.createStatement();
        stmt.execute("truncate table rtr_test.bp_plan");
    }

    @SuppressWarnings("unused")
    private void testRead() throws SQLException {
        List<BPlan> bPlans = this.findAll();

        for (BPlan bPlan : bPlans) {
            logger.debug("{}", bPlan);
            logger.debug("\t"+Arrays.toString(bPlan.getGemeinde()));
            logger.debug("\t"+Arrays.toString(bPlan.getExternereferenzes()));
            logger.debug("\t"+Arrays.toString(bPlan.getPlanart()));
            logger.debug("\t"+bPlan.getRechtsstand());
        } 
    }



    void testExRefTypes() throws SQLException {
        List<BPlan> l = findAll();
        printTypes(l);

    }


    static void printTypes(List<BPlan> bPlans) {
        TreeSet<String> set = new TreeSet<>();
        for (BPlan plan : bPlans) {
            if (plan.externeReferenzes !=null) {
                for (PGExterneReferenz ref : plan.externeReferenzes) {
                    String s = ref.object.typ + " " + ref.object.beschreibung;
                    set.add(s);
                }
            }
        }

        for (String s : set ) {
            logger.debug(s);
        }
    }

    public String validateGeom(Geometry geom) throws SQLException {
        PreparedStatement stmt = null;
        String result = null;
        try {
            stmt = con.prepareStatement("select public.st_isvalidreason(?)");
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

    public void update(BPlan bplan) throws SQLException {
        
        PreparedStatement stmt = null;

        try {           
            String sql = DBUtil.getUpdateSQLString(tableName, COLLUMN_NAMES, KEY_COLLUMN);
            logger.debug(sql);
            stmt = con.prepareStatement(sql);

            int i = 1;

            
            stmt.setString(i++, bplan.name);
            stmt.setString(i++, bplan.nummer);


            if (bplan.gemeinde != null) {
                stmt.setArray(i++, con.createArrayOf("\"xplan_gml\".\"xp_gemeinde\"", bplan.gemeinde));
            } else {
                stmt.setArray(i++, null);
            }

            if (bplan.externeReferenzes != null) {
//                System.err.println("bplan.externeReferenzes.length "+bplan.externeReferenzes.length);
//                for (PGExterneReferenz exRef : bplan.externeReferenzes) {
//                    System.err.println("\t"+exRef);
//                }
                stmt.setArray(i++, con.createArrayOf("\"xplan_gml\".\"xp_spezexternereferenz\"", bplan.externeReferenzes));
            } else {
                stmt.setArray(i++, null);
            }        

            if (bplan.inkrafttretensdatum != null) {
                stmt.setDate(i++, new java.sql.Date(bplan.inkrafttretensdatum.getTime()));
            } else {
                stmt.setObject(i++, null);
            }
            
            if (bplan.auslegungsstartdatum != null && bplan.auslegungsstartdatum.length>0) {
                stmt.setArray(i++, con.createArrayOf("DATE", bplan.auslegungsstartdatum));
            } else {
                stmt.setArray(i++, null);
            }
            if (bplan.auslegungsenddatum != null && bplan.auslegungsenddatum.length>0) {
                stmt.setArray(i++, con.createArrayOf("DATE", bplan.auslegungsenddatum));
            } else {
                stmt.setArray(i++, null);
            }            

            if (bplan.rechtsstand != null) {
                PGobject pgObject = new PGobject();
                pgObject.setType("\"xplan_gml\".\"bp_rechtsstand\"");
                pgObject.setValue(bplan.rechtsstand);
                stmt.setObject(i++, pgObject);
            } else {
                stmt.setObject(i++, null);
            }

            // bplan.setPlanart((PGobject)rs.getObject(i++));
            //        System.out.println("bplan.planart "+bplan.planart);
            if (bplan.planart !=null ) {
                stmt.setArray(i++,getPGArray(con, "\"xplan_gml\".\"bp_planart\"", bplan.planart));
            } else {
                stmt.setObject(i++, null);
            }

            if (bplan.geom!=null) {
                stmt.setObject(i++, new JtsGeometry(bplan.geom));
            } else {
                stmt.setString(i++, null);
            }
            stmt.setObject(i++, bplan.konvertierung_id);
            
            
            stmt.setObject(i++, bplan.internalid); 
            
            if (bplan.aendert != null) {
                stmt.setArray(i++, con.createArrayOf("\"xplan_gml\".\"xp_verbundenerplan\"", bplan.aendert));
            } else {
                stmt.setArray(i++, null);
            }
            if (bplan.wurdegeaendertvon != null) {
                stmt.setArray(i++, con.createArrayOf("\"xplan_gml\".\"xp_verbundenerplan\"", bplan.wurdegeaendertvon));
            } else {
                stmt.setArray(i++, null);
            }
            
            
            // where clause
            stmt.setObject(i++, bplan.gml_id);
            
            
            try {
                logger.info("stmt={}", stmt);
                stmt.execute();
            }
            catch (SQLException ex) {
                logger.error("Fehler writing: \""+bplan+"\"", ex);
                throw ex;
            }


            //      stmt.executeBatch();

        }
        finally {
            if (stmt!=null) {
                try { stmt.close(); } catch (SQLException e) {}
            }
        }    
    }



}
