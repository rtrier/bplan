package de.gdiservice.bplan.dao;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.postgis.jts.JtsGeometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gdiservice.bplan.poi.*;
import de.gdiservice.dao.AbstractDAO;
import de.gdiservice.util.DBUtil;



public class SOPlanDAO extends AbstractDAO<UUID, SOPlan> {

    final static Logger logger = LoggerFactory.getLogger(SOPlanDAO.class);

    final static String[] COLUMN_NAMES = new String[] {            
            "gml_id","name","nummer","gemeinde","externereferenz","genehmigungsdatum", 
            "planart","raeumlichergeltungsbereich","konvertierung_id","internalid","aendert","wurdegeaendertvon"
    };
    

    final static String KEY_COLUMN = "gml_id";
    
    public SOPlanDAO(Connection conWrite, Connection conRead, String tableName) {
        super(conWrite, conRead, tableName);
    }

//    public void insert(SOPlan soplan) throws SQLException {
//        PreparedStatement stmt = null;
//
//        try {           
//            String sql = DBUtil.getInsertSQLString(tableName, COLUMN_NAMES);
//            logger.debug(sql);
//            stmt = conWrite.prepareStatement(sql);
//
//            int i = 1;
//
//            stmt.setObject(i++, soplan.gml_id);
//            stmt.setString(i++, soplan.name);
//            stmt.setString(i++, soplan.nummer);
//
//
//            if (soplan.gemeinde != null) {
//                stmt.setArray(i++, conWrite.createArrayOf("\"xplan_gml\".\"xp_gemeinde\"", soplan.gemeinde));
//            } else {
//                stmt.setArray(i++, null);
//            }
//
//            if (soplan.externeReferenzes != null) {
//                if (soplan.externeReferenzes instanceof PGSpezExterneReferenzAuslegung[]) {                    
//                    stmt.setArray(i++, conWrite.createArrayOf("\"xplankonverter\".\"xp_spezexternereferenzauslegung\"", soplan.externeReferenzes));
//                } else {
//                    stmt.setArray(i++, conWrite.createArrayOf("\"xplan_gml\".\"xp_spezexternereferenz\"", soplan.externeReferenzes));
//                }
//            } else {
//                stmt.setArray(i++, null);
//            }        
//
//            if (soplan.genehmigungsdatum != null) {
//                stmt.setDate(i++, new java.sql.Date(soplan.genehmigungsdatum.getTime()));
//            } else {
//                stmt.setObject(i++, null);
//            }
//            
////            if (soplan.auslegungsstartdatum != null && soplan.auslegungsstartdatum.length>0) {
////                stmt.setArray(i++, con.createArrayOf("DATE", soplan.auslegungsstartdatum));
////            } else {
////                stmt.setArray(i++, null);
////            }
////            if (soplan.auslegungsenddatum != null && soplan.auslegungsenddatum.length>0) {
////                stmt.setArray(i++, con.createArrayOf("DATE", soplan.auslegungsenddatum));
////            } else {
////                stmt.setArray(i++, null);
////            }
//            
//
////            if (soplan.rechtsstand != null) {
////                PGobject pgObject = new PGobject();
////                pgObject.setType("\"xplan_gml\".\"fp_rechtsstand\"");
////                pgObject.setValue(soplan.rechtsstand);
////                stmt.setObject(i++, pgObject);
////            } else {
////                stmt.setObject(i++, null);
////            }
//            
////            if (soplan.verfahren != null) {
////                PGobject pgObject = new PGobject();
////                pgObject.setType("\"xplan_gml\".\"fp_verfahren\"");
////                pgObject.setValue(soplan.rechtsstand);
////                stmt.setObject(i++, pgObject);
////            } else {
////                stmt.setObject(i++, null);
////            }
//
//            // bplan.setPlanart((PGobject)rs.getObject(i++));
//            //        System.out.println("bplan.planart "+bplan.planart);
//            if (soplan.planart !=null ) {
////                PGobject pgObject = new PGobject();
////                pgObject.setType("\"xplan_gml\".\"so_planart\"");
////                pgObject.setValue(soplan.planart);
//                stmt.setObject(i++, soplan.planart );
//            } else {
//                stmt.setObject(i++, null);
//            }
//
//            if (soplan.geom!=null) {
//                stmt.setObject(i++, new JtsGeometry(soplan.geom));
//            } else {
//                stmt.setObject(i++, null);
//            }
//            
//            
//            stmt.setObject(i++, soplan.konvertierung_id);
//
//
//            stmt.setObject(i++, soplan.internalid); 
//            
//            if (soplan.aendert != null) {
//                stmt.setArray(i++, conWrite.createArrayOf("\"xplan_gml\".\"xp_verbundenerplan\"", soplan.aendert));
//            } else {
//                stmt.setArray(i++, null);
//            }
//            if (soplan.wurdegeaendertvon != null) {
//                stmt.setArray(i++, conWrite.createArrayOf("\"xplan_gml\".\"xp_verbundenerplan\"", soplan.wurdegeaendertvon));
//            } else {
//                stmt.setArray(i++, null);
//            }
//            
//            try {
//                logger.debug("stmt: "+stmt.toString());
//                stmt.execute();
//            }
//            catch (SQLException ex) {
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


//    @Override
//    public SOPlan findById(UUID gmlId) throws SQLException {
//       logger.debug("findById "+tableName);
//        SOPlan bplan = null;
//        
//        PreparedStatement stmt = null;
//        ResultSet rs = null;
//        try {    
//            String sql = DBUtil.getSelectSQLString(tableName, COLUMN_NAMES, new String[]  {"gml_id=?"});      
//            logger.debug(sql);
//            stmt = conRead.prepareStatement(sql);
//            stmt.setObject(1, gmlId);
//            rs = stmt.executeQuery();
//            if (rs.next()) {
//                bplan = createObject(rs);
//            }
//        } finally {
//            if (rs!=null) {
//                try { rs.close(); } catch (SQLException e) {}
//            }
//            if (stmt!=null) {
//                try { stmt.close(); } catch (SQLException e) {}
//            }
//        }
//        return bplan;
//    }  
    
    public List<SOPlan> findByInternalIdLikeGmlId(UUID gmlId) throws SQLException {
        logger.debug("findById "+tableName);

         List<SOPlan> plaene = new ArrayList<>();
         
         PreparedStatement stmt = null;
         ResultSet rs = null;
         try {    
             String sql = DBUtil.getSelectSQLString(tableName, COLUMN_NAMES, new String[]  {"internalid like ? or gml_id=?"});
             sql = sql + " order by internalid";
             logger.debug(sql);
             stmt = conRead.prepareStatement(sql);
             stmt.setObject(1, gmlId.toString()+"%");
             stmt.setObject(2, gmlId);
             rs = stmt.executeQuery();
             while (rs.next()) {
                 SOPlan bplan = createObject(rs);
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
    
    

//    public List<SOPlan> findAll() throws SQLException {
//        return findAll(null, null);
//    }

//    public List<SOPlan> findAll(String[] whereClauses, Integer maxCount) throws SQLException {
//        logger.debug("findAll {}", tableName);
//        List<SOPlan> bPlans = new ArrayList<>();
//
//        Statement stmt = null;
//        ResultSet rs = null;
//        try {      
//            stmt = conRead.createStatement();
//            String sql = DBUtil.getSelectSQLString(tableName, COLUMN_NAMES, whereClauses, maxCount);
//            logger.debug(sql);
//            rs = stmt.executeQuery(sql);
//            while (rs.next()) {
//                bPlans.add(createObject(rs));
//            }
//            return bPlans;
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
    
    @Override
    protected SOPlan createObject(ResultSet rs) throws SQLException {
        // "gml_id","name","nummer","gemeinde","externereferenz","inkrafttretensdatum", "auslegungsstartdatum[]", "auslegungsenddatum[]",
        //                                                                             "rechtsstand","planart","raeumlichergeltungsbereich","konvertierung_id","internalid","aendert","wurdegeaendertvon"
        // "gml_id","name","nummer","gemeinde","externereferenz","inkrafttretensdatum","rechtsstand","planart","raeumlichergeltungsbereich","konvertierung_id","internalid","aendert","wurdegeaendertvon"
        int i=1;
        SOPlan soplan = new SOPlan();
        soplan.setGml_id( rs.getObject(i++, UUID.class));
        soplan.setName(rs.getString(i++));
        soplan.setNummer( rs.getString(i++));

        try {

            soplan.gemeinde = getArray(rs.getArray(i++), Gemeinde[].class);
            
//            fplan.externeReferenzes = getArray(rs.getArray(i++), PGExterneReferenz[].class);
            
            Array ar = rs.getArray(i++);
            if (ar !=null) {
                if ("\"xplankonverter\".\"xp_spezexternereferenzauslegung\"".equals(ar.getBaseTypeName())) {
                    soplan.setExterneReferenzes( getArray(ar, PGSpezExterneReferenzAuslegung[].class));
                } else {
                    soplan.setExterneReferenzes( getArray(ar, PGSpezExterneReferenz[].class));
                }
            }

            soplan.setGenehmigungsdatum(rs.getObject(i++, LocalDate.class));
            
//            soplan.auslegungsstartdatum = getDates(rs.getArray(i++));
//            soplan.auslegungsenddatum = getDates(rs.getArray(i++));            
            
//            Object o = rs.getObject(i++);
//            if (o instanceof String) {
//                soplan.rechtsstand = (String)o;
//            } else if (o instanceof PGobject){
//                soplan.rechtsstand = getString((PGobject)o);
//            }

//            o = rs.getObject(i++);
//            if (o instanceof String) {
//                soplan.verfahren = (String)o;
//            } else if (o instanceof PGobject){
//                soplan.verfahren = getString((PGobject)o);
//            }

            soplan.planart = (PG_SO_Planart)rs.getObject(i++);


            JtsGeometry pGobject = (JtsGeometry) rs.getObject(i++);          
            soplan.setGeom(pGobject!=null ? pGobject.getGeometry() : null);
            
            soplan.setKonvertierungId(rs.getObject(i++, Integer.class));
            
            
            soplan.setInternalId( rs.getString(i++)); 
            soplan.setAendert( getArray(rs.getArray(i++), PGVerbundenerPlan[].class));
            soplan.setWurdeGeaendertVon( getArray(rs.getArray(i++), PGVerbundenerPlan[].class));
            
            return soplan;
         
        } 
        catch (Exception e) {                   
            throw new SQLException("Error interpreting Data gml_id=\""+soplan.getGml_id()+"\"", e);
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

//    private static Array getPGArray(Connection con, String typeName, String[] objects) throws SQLException {    
//        final int count = objects.length;
//        PGobject[] pGobjects = new PGobject[count];
//        for (int i=0; i<count; i++) {
//            pGobjects[i] = new PGobject();
//            pGobjects[i].setType(typeName);
//            pGobjects[i].setValue(objects[i]);
//        }
//        return con.createArrayOf(typeName, pGobjects);
//    }  

//    private static String getString(PGobject pgObject) {
//        return pgObject == null ? null : pgObject.getValue();
//    }
//    private static Date getDate(PGobject pgObject) {
//        return pgObject == null ? null : pgObject.getValue();
//    }

//    private static String[] getStrings(Array array) throws SQLException {
////        System.err.println("getStrings =\""+array+"\"");
//        if (array != null) {
//            Object[] pgObjects = (Object[])array.getArray();
//            if (pgObjects != null) {
//                final int count = pgObjects.length;
//                String[] strings = new String[count];
//                if (count>0) {
//                    for (int i=0; i<count; i++) {
//                        strings[i] = getString((PGobject)pgObjects[i]);
//                    }
//                }
//                return strings;
//            }
//        }
//        return null;
//    }

//    private static Date[] getDates(Array array) throws SQLException {
//      if (array != null) {
//          return (java.sql.Date[])array.getArray();
//      }
//      return null;
//  }    



//    @SuppressWarnings("unused")
//    private void truncate() throws SQLException {
//        Statement stmt = conWrite.createStatement();
//        stmt.execute("truncate table rtr_test.bp_plan");
//    }
//
//    @SuppressWarnings("unused")
//    private void testRead() throws SQLException {
//        List<SOPlan> bPlans = this.findAll();
//
//        for (SOPlan bPlan : bPlans) {
//            logger.debug("{}", bPlan);
//            logger.debug("\t"+Arrays.toString(bPlan.getGemeinde()));
//            logger.debug("\t"+Arrays.toString(bPlan.getExternereferenzes()));
//            logger.debug("\t"+bPlan.getPlanart());
////            logger.debug("\t"+bPlan.getRechtsstand());
//        } 
//    }



//    void testExRefTypes() throws SQLException {
//        List<SOPlan> l = findAll();
//        printTypes(l);
//
//    }


//    static void printTypes(List<SOPlan> soPlans) {
//        TreeSet<String> set = new TreeSet<>();
//        for (SOPlan plan : soPlans) {
//            if (plan.externeReferenzes !=null) {
//                for (PGSpezExterneReferenz ref : plan.externeReferenzes) {
//                    String s = ref.object.typ + " " + ref.object.beschreibung;
//                    set.add(s);
//                }
//            }
//        }
//
//        for (String s : set ) {
//            logger.debug(s);
//        }
//    }

//    public String validateGeom(Geometry geom) throws SQLException {
//        PreparedStatement stmt = null;
//        String result = null;
//        try {
//            stmt = conWrite.prepareStatement("select public.st_isvalidreason(?)");
//            WKTWriter writer = new WKTWriter(2);
//            stmt.setObject(1, writer.write(geom), Types.OTHER);
//            ResultSet rs = stmt.executeQuery();
//            if (rs.next()) {
//                result = rs.getString(1);
//            }
//        }
//        finally {
//            if (stmt!=null) {
//                try { stmt.close(); } catch (SQLException e) {}
//            }
//        } 
//        return result;
//    }

//    public void updateO(SOPlan soplan) throws SQLException {
//        
//        PreparedStatement stmt = null;
//
//        try {           
//            String sql = DBUtil.getUpdateSQLString(tableName, COLUMN_NAMES, KEY_COLUMN);
////            logger.debug(sql);
//            stmt = conWrite.prepareStatement(sql);
//
//            int i = 1;
//
//            
//            stmt.setString(i++, soplan.name);
//            stmt.setString(i++, soplan.nummer);
//
//
//            if (soplan.gemeinde != null) {
//                stmt.setArray(i++, conWrite.createArrayOf("\"xplan_gml\".\"xp_gemeinde\"", soplan.gemeinde));
//            } else {
//                stmt.setArray(i++, null);
//            }
//
//            if (soplan.externeReferenzes != null) {
////                System.err.println("bplan.externeReferenzes.length "+bplan.externeReferenzes.length);
////                for (PGExterneReferenz exRef : bplan.externeReferenzes) {
////                    System.err.println("\t"+exRef);
////                }
//                if (soplan.externeReferenzes instanceof PGSpezExterneReferenzAuslegung[]) {                    
//                    stmt.setArray(i++, conWrite.createArrayOf("\"xplankonverter\".\"xp_spezexternereferenzauslegung\"", soplan.externeReferenzes));
//                } else {
//                    stmt.setArray(i++, conWrite.createArrayOf("\"xplan_gml\".\"xp_spezexternereferenz\"", soplan.externeReferenzes));
//                }                
//            } else {
//                stmt.setArray(i++, null);
//            }        
//
//            
//                    
//            if (soplan.genehmigungsdatum != null) {
//                stmt.setDate(i++, new java.sql.Date(soplan.genehmigungsdatum.getTime()));
//            } else {
//                stmt.setObject(i++, null);
//            }
//            
////            if (soplan.auslegungsstartdatum != null && soplan.auslegungsstartdatum.length>0) {
////                stmt.setArray(i++, con.createArrayOf("DATE", soplan.auslegungsstartdatum));
////            } else {
////                stmt.setArray(i++, null);
////            }
////            if (soplan.auslegungsenddatum != null && soplan.auslegungsenddatum.length>0) {
////                stmt.setArray(i++, con.createArrayOf("DATE", soplan.auslegungsenddatum));
////            } else {
////                stmt.setArray(i++, null);
////            }            
//            
////            if (soplan.rechtsstand != null) {
////                PGobject pgObject = new PGobject();
////                pgObject.setType("\"xplan_gml\".\"fp_rechtsstand\"");
////                pgObject.setValue(soplan.rechtsstand);
////                stmt.setObject(i++, pgObject);
////            } else {
////                stmt.setObject(i++, null);
////            }
////            if (soplan.verfahren != null) {
////                PGobject pgObject = new PGobject();
////                pgObject.setType("\"xplan_gml\".\"fp_verfahren\"");
////                pgObject.setValue(soplan.verfahren);
////                stmt.setObject(i++, pgObject);
////            } else {
////                stmt.setObject(i++, null);
////            }
//            if (soplan.planart != null) {
////                PGobject pgObject = new PGobject();
////                pgObject.setType("\"xplan_gml\".\"fp_planart\"");
////                pgObject.setValue(soplan.planart);
//                stmt.setObject(i++, soplan.planart);
//            } else {
//                stmt.setObject(i++, null);
//            }
//            
//            if (soplan.geom!=null) {
//                stmt.setObject(i++, new JtsGeometry(soplan.geom));
//            } else {
//                stmt.setObject(i++, null);
//            }
//            stmt.setObject(i++, soplan.konvertierung_id);
//            
//            
//            stmt.setObject(i++, soplan.internalid); 
//            
//            if (soplan.aendert != null) {
//                stmt.setArray(i++, conWrite.createArrayOf("\"xplan_gml\".\"xp_verbundenerplan\"", soplan.aendert));
//            } else {
//                stmt.setArray(i++, null);
//            }
//            if (soplan.wurdegeaendertvon != null) {
//                stmt.setArray(i++, conWrite.createArrayOf("\"xplan_gml\".\"xp_verbundenerplan\"", soplan.wurdegeaendertvon));
//            } else {
//                stmt.setArray(i++, null);
//            }
//            
//            
//            // where clause
//            stmt.setObject(i++, soplan.gml_id);
//            
//            
//            try {
//                stmt.execute();
//            }
//            catch (SQLException ex) {
//                logger.info("stmt={}", stmt);
//                logger.error("Fehler writing: \""+soplan+"\"", ex);
//                throw ex;
//            }
//
//
//            //      stmt.executeBatch();
//
//        }
//        finally {
//            if (stmt!=null) {
//                try { stmt.close(); } catch (SQLException e) {}
//            }
//        }    
//    }

    @Override
    public String[] getColumns() {
        return SOPlanDAO.COLUMN_NAMES;
    }

    @Override
    public String getKeyColumn() {
        return SOPlanDAO.KEY_COLUMN;
    }

    @Override
    protected int setSQLParameter(Connection con, PreparedStatement stmt, SOPlan soplan, int i) throws SQLException {
        stmt.setString(i++, soplan.getName());
        stmt.setString(i++, soplan.getNummer());


        if (soplan.gemeinde != null) {
            stmt.setArray(i++, conWrite.createArrayOf("\"xplan_gml\".\"xp_gemeinde\"", soplan.gemeinde));
        } else {
            stmt.setArray(i++, null);
        }

        if (soplan.getExternereferenzes() != null) {
            if (soplan.getExternereferenzes() instanceof PGSpezExterneReferenzAuslegung[]) {                    
                stmt.setArray(i++, conWrite.createArrayOf("\"xplankonverter\".\"xp_spezexternereferenzauslegung\"", soplan.getExternereferenzes()));
            } else {
                stmt.setArray(i++, conWrite.createArrayOf("\"xplan_gml\".\"xp_spezexternereferenz\"", soplan.getExternereferenzes()));
            }
        } else {
            stmt.setArray(i++, null);
        }        

        
        stmt.setObject(i++, soplan.getGenehmigungsdatum());
        
        if (soplan.planart !=null ) {
            stmt.setObject(i++, soplan.planart );
        } else {
            stmt.setObject(i++, null);
        }

        if (soplan.getGeom()!=null) {
            stmt.setObject(i++, new JtsGeometry(soplan.getGeom()));
        } else {
            stmt.setObject(i++, null);
        }
        
        
        stmt.setObject(i++, soplan.getKonvertierungId());


        stmt.setObject(i++, soplan.getInternalId()); 
        
        if (soplan.getAendert() != null) {
            stmt.setArray(i++, conWrite.createArrayOf("\"xplan_gml\".\"xp_verbundenerplan\"", soplan.getAendert()));
        } else {
            stmt.setArray(i++, null);
        }
        if (soplan.getWurdeGeaendertVon() != null) {
            stmt.setArray(i++, conWrite.createArrayOf("\"xplan_gml\".\"xp_verbundenerplan\"", soplan.getWurdeGeaendertVon()));
        } else {
            stmt.setArray(i++, null);
        }
        return i;
    }



}
