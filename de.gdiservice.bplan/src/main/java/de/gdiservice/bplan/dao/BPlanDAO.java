package de.gdiservice.bplan;

import java.sql.Array;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTWriter;
import org.postgis.jts.JtsGeometry;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gdiservice.util.DBUtil;



public class BPlanDAO {

    final static Logger logger = LoggerFactory.getLogger(BPlanDAO.class);

    final static String[] COLLUMN_NAMES = new String[] {            
            "gml_id",
            "name",
            "nummer",
            "gemeinde",
            "externereferenz",
            "inkrafttretensdatum",
            "auslegungsstartdatum", 
            "auslegungsenddatum",
            "rechtsstand",
            "planart",
            "raeumlichergeltungsbereich",
            "konvertierung_id",
            "internalid",
            "aendert",
            "wurdegeaendertvon",
            // neu
            "status",
            "verfahren",
            "untergangsdatum",
            "genehmigungsdatum",
            "gruenordnungsplan",
            "ausfertigungsdatum",
            "durchfuehrungsvertrag",
            "erschliessungsvertrag",
            "rechtsverordnungsdatum",
            "satzungsbeschlussdatum",
            "staedtebaulichervertrag",
//            "veroeffentlichungsdatum",
            "planaufstellendegemeinde",
            "veraenderungssperredatum",
            "aufstellungsbeschlussdatum",
            "traegerbeteiligungsenddatum",
            "veraenderungssperreenddatum",
            "traegerbeteiligungsstartdatum",
            "verlaengerungveraenderungssperre",
            "veraenderungssperrebeschlussdatum",
    };
    

    final static String KEY_COLLUMN = "gml_id";

    private final Connection conWrite;
    private final Connection conRead;

    private final String tableName;
    
    
    public BPlanDAO(Connection con, String tableName) {
        this.conWrite = con;
        this.conRead = con;
        this.tableName = tableName;
    }


    public BPlanDAO(Connection conWrite, Connection conRead, String tableName) {
        this.conWrite = conWrite;
        this.conRead = conRead;
        this.tableName = tableName;
    }

    private int setSQLParameter(Connection con, PreparedStatement stmt, BPlan bplan, int i) throws SQLException {
        
        
        stmt.setString(i++, bplan.name);
        stmt.setString(i++, bplan.nummer);

        
        if (bplan.gemeinde != null) {
            stmt.setArray(i++, con.createArrayOf("\"xplan_gml\".\"xp_gemeinde\"", bplan.gemeinde));
        } else {
            stmt.setArray(i++, null);
        }

        if (bplan.externeReferenzes != null) {
            if (bplan.externeReferenzes instanceof PGExterneReferenzAuslegung[]) {                    
                stmt.setArray(i++, con.createArrayOf("\"xplankonverter\".\"xp_spezexternereferenzauslegung\"", bplan.externeReferenzes));
            } else {
                stmt.setArray(i++, con.createArrayOf("\"xplan_gml\".\"xp_spezexternereferenz\"", bplan.externeReferenzes));
            }
                
        } else {
            stmt.setArray(i++, null);
        }        

        if (bplan.inkrafttretensdatum != null) {
            stmt.setObject(i++, bplan.inkrafttretensdatum);
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
            stmt.setNull(i++, Types.OTHER);
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
        
        
//      "status",
        stmt.setObject(i++, bplan.getStatus());
//      "verfahren",
        if (bplan.verfahren != null) {
            PGobject pgObject = new PGobject();
            pgObject.setType("\"xplan_gml\".\"bp_verfahren\"");
            pgObject.setValue(bplan.verfahren);
            stmt.setObject(i++, pgObject);
        } else {
            stmt.setObject(i++, null);
        }
//      "untergangsdatum",
        stmt.setObject(i++, bplan.untergangsdatum);
//      "genehmigungsdatum",
        stmt.setObject(i++, bplan.getGenehmigungsdatum());
//      "gruenordnungsplan",
        stmt.setObject(i++, bplan.getIsGruenordnungsplan());
//      "ausfertigungsdatum",
        stmt.setObject(i++, bplan.getAusfertigungsdatum());
//      "durchfuehrungsvertrag",
//        System.err.println("\t"+i+" getIsDurchfuehrungsvertrag");
        stmt.setObject(i++, bplan.getIsDurchfuehrungsvertrag());
//      "erschliessungsvertrag",
        stmt.setObject(i++, bplan.getIsErschliessungsvertrag());
//      "rechtsverordnungsdatum",
        stmt.setObject(i++, bplan.getRechtsverordnungsdatum());
//      "satzungsbeschlussdatum",
        stmt.setObject(i++, bplan.getSatzungsbeschlussdatum());
//      "staedtebaulichervertrag",
        stmt.setObject(i++, bplan.getIsStaedtebaulichervertrag());
//      "planaufstellendegemeinde",
        if (bplan.planaufstellendegemeinde != null) {
            stmt.setArray(i++, con.createArrayOf("\"xplan_gml\".\"xp_planaufstellendegemeinde\"", bplan.planaufstellendegemeinde));
        } else {
            stmt.setArray(i++, null);
        }
        
        
//      "veraenderungssperredatum",
//        System.err.println("\t"+i+" veraenderungssperredatum");
        stmt.setObject(i++, bplan.getVeraenderungssperredatum());
//      "aufstellungsbeschlussdatum",
        stmt.setObject(i++, bplan.getAufstellungsbeschlussdatum());
//      "traegerbeteiligungsenddatum",#
        if (bplan.getTraegerbeteiligungsenddatum() != null && bplan.traegerbeteiligungsenddatum.length>0) {
            stmt.setArray(i++, con.createArrayOf("DATE", bplan.traegerbeteiligungsenddatum));
        } else {
            stmt.setArray(i++, null);
        }
//      "veraenderungssperreenddatum",
        stmt.setObject(i++, bplan.getVeraenderungssperreenddatum());
//      "traegerbeteiligungsstartdatum",
//        System.err.println("\t"+i+" traegerbeteiligungsstartdatum");        
        stmt.setArray(i++, con.createArrayOf("DATE", bplan.getTraegerbeteiligungsstartdatum()));
//      "verlaengerungveraenderungssperre",
//        System.err.println("bplan.getVerlaengerungveraenderungssperre() "+bplan.getVerlaengerungveraenderungssperre());
        
        if (bplan.verlaengerungveraenderungssperre != null) {
            PGobject pgObject = new PGobject();
            pgObject.setType("\"xplan_gml\".\"xp_verlaengerungveraenderungssperre\"");
            pgObject.setValue(bplan.verlaengerungveraenderungssperre);
            stmt.setObject(i++, pgObject);
        } else {
            stmt.setObject(i++, null);
        }
        
//        stmt.setObject(i++, bplan.getVerlaengerungveraenderungssperre());
//      "veraenderungssperrebeschlussdatum",
        stmt.setObject(i++, bplan.getVeraenderungssperrebeschlussdatum());
        
        return i;

    }


    public void insert(BPlan bplan) throws SQLException {
        PreparedStatement stmt = null;
        logger.debug("insert gmlId="+bplan.gml_id);      
        try {           
            String sql = DBUtil.getInsertSQLString(tableName, COLLUMN_NAMES);
//            logger.debug(sql);
            stmt = conWrite.prepareStatement(sql);
            int i=1;
            stmt.setObject(i++, bplan.gml_id);
            setSQLParameter(conWrite, stmt, bplan, i);
            try {
//                logger.debug("stmt: "+stmt.toString());
                stmt.execute();
            }
            catch (SQLException ex) {
                logger.error("stmt: "+stmt.toString());
//                System.err.println("Warnings="+con.getWarnings());
//                PGConnection pgconn = con.unwrap(PGConnection.class);
//                PGNotification[] notifications = pgconn.getNotifications();
//                if (notifications!=null) {
//                    System.err.println("Notifications.length="+notifications.length);
//                    for (PGNotification notification : pgconn.getNotifications()) {
//                        System.err.println(notification);
//                    }
//                } else {
//                    System.err.println("keine Notifications");
//                    System.err.println("keine Notifications");
//                }
//                ex.printStackTrace();
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
       logger.debug("findById "+tableName+" UUID="+gmlId);
//       System.err.println("findById "+tableName+" UUID="+gmlId);
        BPlan bplan = null;
        
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {    
            String sql = DBUtil.getSelectSQLString(tableName, COLLUMN_NAMES, new String[]  {"gml_id=?"});      
//            logger.debug(sql);
//            PGConnection pgconn = conRead.unwrap(PGConnection.class);
//            pgconn.addDataType("\"xplan_gml\".\"bp_status\"", CodeList.class);
            stmt = conRead.prepareStatement(sql);
            stmt.setObject(1, gmlId);
            rs = stmt.executeQuery();
//            printColumns(rs);
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
             stmt = conRead.prepareStatement(sql);
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
            stmt = conRead.createStatement();
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
//        printColumns(rs);
        int i=1;
        BPlan bplan = new BPlan();
        bplan.gml_id = rs.getObject(i++, UUID.class);
        bplan.name = rs.getString(i++);
        bplan.nummer = rs.getString(i++);

        try {

            bplan.gemeinde = getArray(rs.getArray(i++), Gemeinde[].class);
            
            Array ar = rs.getArray(i++);
            if (ar !=null) {
//                logger.info("ar.getBaseTypeName()="+ar.getBaseTypeName());
                if ("\"xplankonverter\".\"xp_spezexternereferenzauslegung\"".equals(ar.getBaseTypeName())) {
                    bplan.externeReferenzes = getArray(ar, PGExterneReferenzAuslegung[].class);
                } else {
                    bplan.externeReferenzes = getArray(ar, PGExterneReferenz[].class);
                }
            }

            bplan.inkrafttretensdatum = toLocalDate(rs.getDate(i++));
            
            bplan.auslegungsstartdatum = toLocalDates(rs.getArray(i++));
            bplan.auslegungsenddatum = toLocalDates(rs.getArray(i++));            
            
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
            
//            "status",
            Object oStatus = rs.getObject(i++);
            logger.debug("oStatus=\""+oStatus+"\" "+((oStatus==null)? "null" : oStatus.getClass()));
            bplan.setStatus((CodeList)oStatus);
//            "verfahren",
            bplan.verfahren = rs.getString(i++);
//            "untergangsdatum",
            bplan.untergangsdatum = toLocalDate(rs.getDate(i++));
//            "genehmigungsdatum",
            bplan.genehmigungsdatum = toLocalDate(rs.getDate(i++));
//            "gruenordnungsplan",
            bplan.gruenordnungsplan = (Boolean)rs.getObject(i++);
//            "ausfertigungsdatum",
            bplan.ausfertigungsdatum = toLocalDate(rs.getDate(i++));
//            "durchfuehrungsvertrag",
            bplan.durchfuehrungsvertrag = (Boolean)rs.getObject(i++);
//            "erschliessungsvertrag",
            bplan.erschliessungsvertrag= (Boolean)rs.getObject(i++);
//            "rechtsverordnungsdatum",
            bplan.rechtsverordnungsdatum = toLocalDate(rs.getDate(i++));
//            "satzungsbeschlussdatum",
            bplan.satzungsbeschlussdatum = toLocalDate(rs.getDate(i++));
//            "staedtebaulichervertrag",
            bplan.staedtebaulichervertrag= (Boolean)rs.getObject(i++);
////            "veroeffentlichungsdatum",
//            "planaufstellendegemeinde",
            bplan.planaufstellendegemeinde= BPlanDAO.getArray(rs.getArray(i++), PlanaufstellendeGemeinde[].class);
//            "veraenderungssperredatum",
            bplan.veraenderungssperredatum = toLocalDate(rs.getDate(i++));
//            "aufstellungsbeschlussdatum",
            bplan.aufstellungsbeschlussdatum = toLocalDate(rs.getDate(i++));
//            "traegerbeteiligungsenddatum",
            bplan.traegerbeteiligungsenddatum = toLocalDates(rs.getArray(i++));
//            "veraenderungssperreenddatum",
            bplan.veraenderungssperreenddatum = toLocalDate(rs.getDate(i++));
//            "traegerbeteiligungsstartdatum",
            bplan.traegerbeteiligungsstartdatum= toLocalDates(rs.getArray(i++));
//            "verlaengerungveraenderungssperre",
            bplan.verlaengerungveraenderungssperre= rs.getString(i++);
//            "veraenderungssperrebeschlussdatum",
            bplan.veraenderungssperrebeschlussdatum= toLocalDate(rs.getDate(i++));
            
            return bplan;
         
        } 
        catch (Exception e) {                   
            if (e.getCause() != null) {
                throw new SQLException("Error interpreting Data gml_id=\""+bplan.gml_id+"\" cause=\""+e.getCause().getMessage()+"\"", e);
            } else {
                printColumns(rs);
                throw new SQLException("Error interpreting Data gml_id=\""+bplan.gml_id+"\" cause=\""+e.getMessage()+"\"", e);
            }
        }
    }

    void printColumns(ResultSet rs) throws SQLException {
        final ResultSetMetaData m = rs.getMetaData();
        System.err.println(String.format("%-40s %-6s %-40s %s", "ColumnName", "Type", "ColumnClassName", "ColumnTypeName"));
        for (int cNr=1; cNr<=m.getColumnCount(); cNr++) {
            System.err.println(String.format("%-40s %-6s %-40s %s", m.getColumnName(cNr), +m.getColumnType(cNr), m.getColumnClassName(cNr), m.getColumnTypeName(cNr)));
        }
    }


    

    public static <T extends Object> T[] getArray(Array array, Class<? extends T[]> clasz) throws SQLException {

        if (array != null) {
            try {
                Object[] objects = (Object[]) array.getArray();
                // System.err.println(objects[0].getClass());
                if (objects.length>0 && objects[0].getClass().getName().equals(PGobject.class.getName())) {
                    logger.debug("getArray "+objects[0].getClass().getName() +"  "+clasz.getName());
                    throw new SQLException(
                        "Fehler: SQLArray could'nt read, should be of class \"" + clasz.getSimpleName() + "\" but is PGobject, Register Class for SQLType "+array.getBaseTypeName());
                }
                return (objects != null) ? Arrays.copyOf(objects, objects.length, clasz) : null;
            } catch (Exception ex) {
                throw new SQLException(
                            "Fehler: SQLArray could'nt read, should be of class \"" + clasz.getSimpleName() + "\", SQLType: "+ array.getBaseTypeName()+")", ex);
                
            }
        }
        return null;
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
                        if (pgObjects[i] instanceof String) {                            
                            strings[i] = (String)pgObjects[i];
                        } else {
                            strings[i] = getString((PGobject)pgObjects[i]);
                        }
                    }
                }
                return strings;
            }
        }
        return null;
    }
    
    
    

    public static Date[] getDates(Array array) throws SQLException {
      if (array != null) {
          return (java.sql.Date[])array.getArray();
      }
      return null;
    }
    public static LocalDate[] toLocalDates(Array array) throws SQLException {
        if (array != null) {
            java.sql.Date[] sqlDates = (java.sql.Date[])array.getArray();
            LocalDate[] lds = new LocalDate[sqlDates.length];
            for (int i=0; i<sqlDates.length; i++) {
                lds[i] = sqlDates[i].toLocalDate();
            }
            return lds;
        }
        return null;
    } 

    public static LocalDate toLocalDate(Date date) {
        if (date != null) {
            return date.toLocalDate();
        }
        return null;
    }  


    @SuppressWarnings("unused")
    private void truncate() throws SQLException {
        Statement stmt = conWrite.createStatement();
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
            stmt = conRead.prepareStatement("select public.st_isvalidreason(?)");
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

    public boolean update(BPlan bplan) throws SQLException {
        
        PreparedStatement stmt = null;
        boolean success = false;
        logger.debug("update gmlId="+bplan.gml_id);
        try {           
            String sql = DBUtil.getUpdateSQLString(tableName, COLLUMN_NAMES, KEY_COLLUMN);
//            logger.debug(sql);
            stmt = conWrite.prepareStatement(sql);

            int i = setSQLParameter(conWrite, stmt, bplan, 1);            
            // where clause
            System.err.println("i="+i);
            stmt.setObject(i++, bplan.gml_id);
            
            
            try {
                logger.info("stmt={}", stmt.toString().substring(0, 240));
                int updatedRows =  stmt.executeUpdate();
                success = updatedRows == 1;
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
        return success;
    }



}
