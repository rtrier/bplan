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
import java.util.TreeSet;
import java.util.UUID;

import org.postgis.jts.JtsGeometry;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gdiservice.bplan.poi.CodeList;
import de.gdiservice.bplan.poi.FPlan;
import de.gdiservice.bplan.poi.Gemeinde;
import de.gdiservice.bplan.poi.PGSpezExterneReferenz;
import de.gdiservice.bplan.poi.PGSpezExterneReferenzAuslegung;
import de.gdiservice.bplan.poi.PGVerbundenerPlan;
import de.gdiservice.bplan.poi.PlanaufstellendeGemeinde;
import de.gdiservice.dao.AbstractDAO;
import de.gdiservice.util.DBUtil;



public class FPlanDAO extends AbstractDAO<UUID, FPlan> {

    final static Logger logger = LoggerFactory.getLogger(FPlanDAO.class);

    final static String[] COLUMN_NAMES = new String[] {            
            "gml_id","name","nummer","gemeinde","externereferenz","wirksamkeitsdatum", "auslegungsstartdatum", "auslegungsenddatum", "rechtsstand","verfahren",
            "planart","raeumlichergeltungsbereich","konvertierung_id","internalid","aendert","wurdegeaendertvon",
            "aufstellungsbeschlussDatum","entwurfsbeschlussdatum","genehmigungsdatum","planaufstellendeGemeinde","planbeschlussdatum",
            "status","technherstelldatum","traegerbeteiligungsenddatum","traegerbeteiligungsstartdatum","untergangsdatum"
    };
    

    
    
    
    final static String KEY_COLUMN = "gml_id";

//    private final Connection conWrite;
//    private final Connection conRead;
//
//    private final String tableName;

    public FPlanDAO(Connection conWrite, Connection conRead, String tableName) {
        super(conWrite, conRead, tableName);
//        this.conWrite = conWrite;
//        this.conRead = conRead;
//        this.tableName = tableName;
    }




//    public void insert(FPlan fplan) throws SQLException {
//        PreparedStatement stmt = null;
//
//        try {           
//            String sql = DBUtil.getInsertSQLString(tableName, COLUMN_NAMES);
////            logger.debug(sql);
//            stmt = conWrite.prepareStatement(sql);
//
//            int i = 1;
//            stmt.setObject(i++, fplan.gml_id);            
//            setSQLParameter(conWrite, stmt, fplan, i);
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

    @Override    
    protected int setSQLParameter(Connection con, PreparedStatement stmt, FPlan fplan, int i) throws SQLException {
        
        
        stmt.setString(i++, fplan.getName());
        stmt.setString(i++, fplan.getNummer());

        if (fplan.gemeinde != null) {
            stmt.setArray(i++, conWrite.createArrayOf("\"xplan_gml\".\"xp_gemeinde\"", fplan.gemeinde));
        } else {
            stmt.setArray(i++, null);
        }

        if (fplan.getExternereferenzes() != null) {
            if (fplan.getExternereferenzes() instanceof PGSpezExterneReferenzAuslegung[]) {                    
                stmt.setArray(i++, conWrite.createArrayOf("\"xplankonverter\".\"xp_spezexternereferenzauslegung\"", fplan.getExternereferenzes()));
            } else {
                stmt.setArray(i++, conWrite.createArrayOf("\"xplan_gml\".\"xp_spezexternereferenz\"", fplan.getExternereferenzes()));
            }
        } else {
            stmt.setArray(i++, null);
        }        

        if (fplan.wirksamkeitsdatum != null) {
            stmt.setObject(i++, fplan.getWirksamkeitsdatum());
        } else {
            stmt.setObject(i++, null);
        }
        
        if (fplan.auslegungsstartdatum != null && fplan.auslegungsstartdatum.length>0) {
            stmt.setArray(i++, conWrite.createArrayOf("DATE", fplan.auslegungsstartdatum));
        } else {
            stmt.setArray(i++, null);
        }
        if (fplan.auslegungsenddatum != null && fplan.auslegungsenddatum.length>0) {
            stmt.setArray(i++, conWrite.createArrayOf("DATE", fplan.auslegungsenddatum));
        } else {
            stmt.setArray(i++, null);
        }
        

        if (fplan.rechtsstand != null) {
            PGobject pgObject = new PGobject();
            pgObject.setType("\"xplan_gml\".\"fp_rechtsstand\"");
            pgObject.setValue(fplan.rechtsstand);
            stmt.setObject(i++, pgObject);
        } else {
            stmt.setObject(i++, null);
        }
        
        if (fplan.verfahren != null) {
            PGobject pgObject = new PGobject();
            pgObject.setType("\"xplan_gml\".\"fp_verfahren\"");
            pgObject.setValue(fplan.verfahren);
            stmt.setObject(i++, pgObject);
        } else {
            stmt.setObject(i++, null);
        }

        // bplan.setPlanart((PGobject)rs.getObject(i++));
        //        System.out.println("bplan.planart "+bplan.planart);
        if (fplan.planart !=null ) {
            PGobject pgObject = new PGobject();
            pgObject.setType("\"xplan_gml\".\"fp_planart\"");
            pgObject.setValue(fplan.planart);
            stmt.setObject(i++, pgObject);
        } else {
            stmt.setObject(i++, null);
        }

        if (fplan.getGeom()!=null) {
            stmt.setObject(i++, new JtsGeometry(fplan.getGeom()));
        } else {
            stmt.setObject(i++, null);
        }
        
        
        stmt.setObject(i++, fplan.konvertierung_id);


        stmt.setObject(i++, fplan.getInternalId()); 
        
        if (fplan.getAendert() != null) {
            stmt.setArray(i++, conWrite.createArrayOf("\"xplan_gml\".\"xp_verbundenerplan\"", fplan.getAendert()));
        } else {
            stmt.setArray(i++, null);
        }
        if (fplan.getWurdeGeaendertVon() != null) {
            stmt.setArray(i++, conWrite.createArrayOf("\"xplan_gml\".\"xp_verbundenerplan\"", fplan.getWurdeGeaendertVon()));
        } else {
            stmt.setArray(i++, null);
        }

        if (fplan.aufstellungsbeschlussDatum != null) {
            stmt.setObject(i++, fplan.aufstellungsbeschlussDatum);
        } else {
            stmt.setObject(i++, null);            
        }
        if (fplan.entwurfsbeschlussdatum != null) {
            stmt.setObject(i++, fplan.entwurfsbeschlussdatum);
        } else {
            stmt.setObject(i++, null);
        }
        if (fplan.getGenehmigungsdatum() != null) {
            stmt.setObject(i++, fplan.getGenehmigungsdatum());
        } else {
            stmt.setObject(i++, null);
        }
        if (fplan.planaufstellendeGemeinde != null) {
            stmt.setArray(i++, conWrite.createArrayOf("\"xplan_gml\".\"xp_planaufstellendegemeinde\"", fplan.planaufstellendeGemeinde));
        } else {
            stmt.setObject(i++, null);
        }
        if (fplan.planbeschlussdatum != null) {
            stmt.setObject(i++, fplan.planbeschlussdatum);
        } else {
            stmt.setObject(i++, null);
        }    
        if (fplan.status != null) {
            stmt.setObject(i++, fplan.getStatus());
        } else {
            stmt.setObject(i++, null);
        }
        if (fplan.technherstelldatum != null) {
            stmt.setObject(i++, fplan.technherstelldatum);
        } else {
            stmt.setObject(i++, null);
        }
        
        if (fplan.traegerbeteiligungsenddatum != null) {
            stmt.setArray(i++, conWrite.createArrayOf("DATE", fplan.traegerbeteiligungsenddatum));                
        } else {
            stmt.setObject(i++, null);
        }
        
        if (fplan.traegerbeteiligungsstartdatum != null) {                
            stmt.setArray(i++, conWrite.createArrayOf("DATE", fplan.traegerbeteiligungsstartdatum));
        } else {
            stmt.setObject(i++, null);
        }
        
        if (fplan.getUntergangsdatum() != null) {
            stmt.setObject(i++, fplan.getUntergangsdatum());
        } else {
            stmt.setObject(i++, null);
        }
        
        return i;
    }

//    public FPlan findById(UUID gmlId) throws SQLException {
//       logger.debug("findById "+tableName);
//        FPlan fplan = null;
//        
//        PreparedStatement stmt = null;
//        ResultSet rs = null;
//        try {    
//            String sql = DBUtil.getSelectSQLString(tableName, COLUMN_NAMES, new String[]  {"gml_id=?"});      
////            logger.debug(sql);
//            stmt = conRead.prepareStatement(sql);
//            stmt.setObject(1, gmlId);
//            rs = stmt.executeQuery();
//            if (rs.next()) {
//                fplan = createFPlan(rs);
//            }
//        } finally {
//            if (rs!=null) {
//                try { rs.close(); } catch (SQLException e) {}
//            }
//            if (stmt!=null) {
//                try { stmt.close(); } catch (SQLException e) {}
//            }
//        }
//        return fplan;
//    }  
    
    public List<FPlan> findByInternalIdLikeGmlId(UUID gmlId) throws SQLException {
        logger.debug("findById "+tableName);

         List<FPlan> plaene = new ArrayList<>();
         
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
                 FPlan bplan = createObject(rs);
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
    
    
//    @Override
//    public List<FPlan> findAll() throws SQLException {
//        return findAll(null, null);
//    }
//
//    @Override
//    public List<FPlan> findAll(String[] whereClauses, Integer maxCount) throws SQLException {
//        logger.debug("findAll {}", tableName);
//        List<FPlan> bPlans = new ArrayList<>();
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
    
    protected FPlan createObject(ResultSet rs) throws SQLException {
        // "gml_id","name","nummer","gemeinde","externereferenz","inkrafttretensdatum", "auslegungsstartdatum[]", "auslegungsenddatum[]",
        //                                                                             "rechtsstand","planart","raeumlichergeltungsbereich","konvertierung_id","internalid","aendert","wurdegeaendertvon"
        // "gml_id","name","nummer","gemeinde","externereferenz","inkrafttretensdatum","rechtsstand","planart","raeumlichergeltungsbereich","konvertierung_id","internalid","aendert","wurdegeaendertvon"
        int i=1;
        FPlan fplan = new FPlan();
        fplan.setGml_id( rs.getObject(i++, UUID.class));
        fplan.setName (rs.getString(i++));
        fplan.setNummer (rs.getString(i++));

        try {

            fplan.gemeinde = getArray(rs.getArray(i++), Gemeinde[].class);
            
//            fplan.externeReferenzes = getArray(rs.getArray(i++), PGExterneReferenz[].class);
            
            Array ar = rs.getArray(i++);
            if (ar !=null) {
                if ("\"xplankonverter\".\"xp_spezexternereferenzauslegung\"".equals(ar.getBaseTypeName())) {
                    fplan.setExterneReferenzes( getArray(ar, PGSpezExterneReferenzAuslegung[].class));
                } else {
                    fplan.setExterneReferenzes (getArray(ar, PGSpezExterneReferenz[].class));
                }
            }

            fplan.wirksamkeitsdatum = rs.getObject(i++, LocalDate.class);
            
            fplan.auslegungsstartdatum =  BPlanDAO.toLocalDates(rs.getArray(i++));
            fplan.auslegungsenddatum =  BPlanDAO.toLocalDates(rs.getArray(i++));            
            
            Object o = rs.getObject(i++);
            if (o instanceof String) {
                fplan.rechtsstand = (String)o;
            } else if (o instanceof PGobject){
                fplan.rechtsstand = getString((PGobject)o);
            }

            o = rs.getObject(i++);
            if (o instanceof String) {
                fplan.verfahren = (String)o;
            } else if (o instanceof PGobject){
                fplan.verfahren = getString((PGobject)o);
            }

            fplan.planart = rs.getString(i++);


            
            JtsGeometry pGobject = (JtsGeometry) rs.getObject(i++);
            fplan.setGeom (pGobject!=null ? pGobject.getGeometry() : null);
            
            fplan.konvertierung_id = (Integer)rs.getObject(i++);
            
            
            
            fplan.setInternalId( rs.getString(i++)); 
            fplan.setAendert( getArray(rs.getArray(i++), PGVerbundenerPlan[].class));
            fplan.setWurdeGeaendertVon(getArray(rs.getArray(i++), PGVerbundenerPlan[].class));
            
            
            fplan.aufstellungsbeschlussDatum = BPlanDAO.toLocalDate(rs.getDate(i++));
            fplan.entwurfsbeschlussdatum = BPlanDAO.toLocalDate(rs.getDate(i++));
            fplan.setGenehmigungsdatum(rs.getObject(i++, LocalDate.class));
            fplan.planaufstellendeGemeinde= BPlanDAO.getArray(rs.getArray(i++), PlanaufstellendeGemeinde[].class);
            fplan.planbeschlussdatum = BPlanDAO.toLocalDate(rs.getDate(i++));
            fplan.status = (CodeList)rs.getObject(i++);
            fplan.technherstelldatum = BPlanDAO.toLocalDate(rs.getDate(i++));
            
            fplan.traegerbeteiligungsenddatum = BPlanDAO.toLocalDates(rs.getArray(i++)); 
            fplan.traegerbeteiligungsstartdatum = BPlanDAO.toLocalDates(rs.getArray(i++));
            fplan.setUntergangsdatum(rs.getObject(i++, LocalDate.class));
//            fplan.veroeffentlichungsdatum = BPlanDAO.toLocalDate(rs.getDate(i++));
            
            return fplan;
         
        } 
        catch (Exception e) {                   
            throw new SQLException("Error interpreting Data gml_id=\""+fplan.getGml_id()+"\"", e);
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

    private static String getString(PGobject pgObject) {
        return pgObject == null ? null : pgObject.getValue();
    }
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
//        List<FPlan> bPlans = this.findAll();
//
//        for (FPlan bPlan : bPlans) {
//            logger.debug("{}", bPlan);
//            logger.debug("\t"+Arrays.toString(bPlan.getGemeinde()));
//            logger.debug("\t"+Arrays.toString(bPlan.getExternereferenzes()));
//            logger.debug("\t"+bPlan.getPlanart());
//            logger.debug("\t"+bPlan.getRechtsstand());
//        } 
//    }



//    void testExRefTypes() throws SQLException {
//        List<FPlan> l = findAll();
//        printTypes(l);
//
//    }


    static void printTypes(List<FPlan> fPlans) {
        TreeSet<String> set = new TreeSet<>();
        for (FPlan plan : fPlans) {
            if (plan.getExternereferenzes() !=null) {
                for (PGSpezExterneReferenz ref : plan.getExternereferenzes()) {
                    String s = ref.object.typ + " " + ref.object.beschreibung;
                    set.add(s);
                }
            }
        }

        for (String s : set ) {
            logger.debug(s);
        }
    }

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

//    public void update(FPlan fplan) throws SQLException {
//        
//        PreparedStatement stmt = null;
//
//        try {           
//            String sql = DBUtil.getUpdateSQLString(tableName, COLUMN_NAMES, KEY_COLUMN);
//            logger.debug(sql);
//            stmt = conWrite.prepareStatement(sql);
//
//            int i = setSQLParameter(conWrite, stmt, fplan, 1);
//            // where clause
//            stmt.setObject(i++, fplan.gml_id);
//            
//            
//            try {
//                logger.info("stmt={}", stmt);
//                stmt.execute();
//            }
//            catch (SQLException ex) {
//                logger.error("Fehler writing: \""+fplan+"\"", ex);
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
        return FPlanDAO.COLUMN_NAMES;
    }




    @Override
    public String getKeyColumn() {
        return FPlanDAO.KEY_COLUMN;
    }





}
