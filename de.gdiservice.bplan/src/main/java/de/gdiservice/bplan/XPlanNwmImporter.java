package de.gdiservice.bplan;

import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gdiservice.bplan.dao.BPBereichDAO;
import de.gdiservice.bplan.dao.BPlanDAO;
import de.gdiservice.bplan.dao.FPBereichDAO;
import de.gdiservice.bplan.dao.FPlanDAO;
import de.gdiservice.bplan.dao.SOBereichDAO;
import de.gdiservice.bplan.dao.SOPlanDAO;
import de.gdiservice.bplan.dao.XPBereichDAO;
import de.gdiservice.bplan.konvertierung.Gemeinde;
import de.gdiservice.bplan.konvertierung.GemeindeDAO;
import de.gdiservice.bplan.konvertierung.Konvertierung;
import de.gdiservice.bplan.konvertierung.Konvertierung.KonvertierungStatus;
import de.gdiservice.bplan.konvertierung.KonvertierungDAO;
import de.gdiservice.bplan.poi.BPlan;
import de.gdiservice.bplan.poi.FPlan;
import de.gdiservice.bplan.poi.HasChangedFunctions;
import de.gdiservice.bplan.poi.SOPlan;
import de.gdiservice.bplan.poi.XPBereich;
import de.gdiservice.bplan.poi.XPPlan;
import de.gdiservice.dao.AbstractDAO;
import de.gdiservice.wfs.BPBereichFactory;
import de.gdiservice.wfs.FPBereichFactory;
import de.gdiservice.wfs.SOBereichFactory;
import de.gdiservice.wfs.WFSClient;
import de.gdiservice.wfs.WFSFactory;

public class XPlanNwmImporter<T extends XPPlan, B extends XPBereich<T>> extends XPPlanImporter<T> {
    
    final static Logger logger = LoggerFactory.getLogger(XPlanNwmImporter.class);
    
    protected String bpbereichTable;
    
    List<B> bereiche;
    

    public XPlanNwmImporter(Class<T> clasz, String konvertierungTable, String bplanTable, String bpbereichTable, String kvwmapUrl, String kvwmapLoginName, String kvwmapPassword) {
        super(clasz, konvertierungTable, bplanTable, null, kvwmapUrl, kvwmapLoginName, kvwmapPassword);
        this.bpbereichTable = bpbereichTable;
        System.err.println("newBPlanNwmImporter");
    }
    

    void checkPlanArt(FPlan plan) {
        Integer iPlanArt = null;        
        try {
            if (plan.getPlanart()!=null) {
                iPlanArt = Integer.parseInt(plan.getPlanart());
            } else {
                throw new IllegalArgumentException("WFS enthält keine Planart.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Planart für gmlId=\""+plan.getGml_id()+"\" \""+plan.getPlanart()+"\" ist nicht gültig.");
        }                    
        FPlan.PlanArt planArt = FPlan.PlanArt.get(iPlanArt);
        if (planArt==null) {
            throw new IllegalArgumentException("Planart für gmlId=\""+plan.getGml_id()+"\" \""+planArt+"\" ist im System nicht bekannt.");
        }
    }    
    
    
    void checkPlanArt(BPlan plan) {
        Integer iPlanArt = null;
        
        try {
            if (plan.getPlanart()!=null && plan.getPlanart().length>0) {
                iPlanArt = Integer.parseInt(plan.getPlanart()[0]);
            } else {
                throw new IllegalArgumentException("WFS enthält keine Planart.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Planart für gmlId=\""+plan.getGml_id()+"\" \""+plan.getPlanart()[0]+"\" ist nicht gültig.");
        }                    
        BPlan.PlanArt planArt = BPlan.PlanArt.get(iPlanArt);
        if (planArt==null) {
            throw new IllegalArgumentException("Planart für gmlId=\""+plan.getGml_id()+"\" \""+planArt+"\" ist im System nicht bekannt.");
        }
    }
    
    
    static Konvertierung createKonvertierung(XPPlan plan, Gemeinde gemeinde) {
        Konvertierung konvertierung = new Konvertierung();
        konvertierung.stelle_id = gemeinde.stelle_id;
        konvertierung.status = KonvertierungStatus.erstellt;
        konvertierung.user_id = 1;
        konvertierung.gebietseinheiten = gemeinde.rs;
        konvertierung.bezeichnung = plan.getName()+" "+gemeinde.gmd_name+" "+plan.getNummer();
        konvertierung.veroeffentlicht = false;
        konvertierung.beschreibung = "automatically created from wfs-nwm";
        
        
        
        if (plan instanceof BPlan) {
            konvertierung.planart = "BP-Plan";    
        } else if (plan instanceof FPlan) {
            konvertierung.planart = "FP-Plan";    
        } else if (plan instanceof SOPlan) {
            konvertierung.planart = "SO-Plan";    
        } else {
            throw new IllegalArgumentException(plan.getClass().getSimpleName()+ " not supported");
        }
        
        
        
        konvertierung.epsg = Konvertierung.EPSGCodes.EPSG_25833;
        konvertierung.input_epsg = Konvertierung.EPSGCodes.EPSG_25833;
        konvertierung.output_epsg = Konvertierung.EPSGCodes.EPSG_25833;
        konvertierung.geom_precision =3; 
        return konvertierung;
    }    
    
    
    @SuppressWarnings("unchecked")
    public void importWFS(Connection conWrite, Connection conRead, ImportConfigEntry entry, ImportLogger importLogger) throws Exception  {
        
        bereiche = null;        
         
        
        String sVersion = (entry.onlineresource.indexOf("nwm")>=0) ? "2.0.0" : "1.1.0";
            
            
        WFSFactory<B> wfsBereichFactory = null;
        String featureTypBereich = null;
        if (planClasz.equals(BPlan.class)) {
            wfsBereichFactory = (WFSFactory<B>) new BPBereichFactory();
            featureTypBereich = "BP_Bereich";
        } if (planClasz.equals(FPlan.class)) {
            wfsBereichFactory = (WFSFactory<B>) new FPBereichFactory();
            featureTypBereich = "FP_Bereich";
        } if (planClasz.equals(SOPlan.class)) {
            wfsBereichFactory = (WFSFactory<B>) new SOBereichFactory();
            featureTypBereich = "SO_Bereich";
        }
            
        final String wfsUrl = "https://nwm.bf.geoplex.de/interface/wfs-ms/"+featureTypBereich+"?service=WFS&VERSION="+sVersion+"&REQUEST=GetFeature&TYPENAME="+featureTypBereich+"&SRSNAME=epsg:25833";
        importLogger.addLine("Reading WFS: \""+ wfsUrl + "\"");
        
        try {
            bereiche = WFSClient.read(wfsUrl, wfsBereichFactory, importLogger);
            logger.info("read Bereiche done."+bereiche.size()+" Bereiche");
        } 
        catch (Exception ex) {
            logger.error("error reading from service " + entry.bezeichnung + " with url=\"" + wfsUrl +"\"", ex);
            importLogger.addError("ERROR - Reading WFS: \""+ wfsUrl + "\" error:["+ex.getMessage()+"]");                
        }
        super.importWFS(conWrite, conRead, entry, importLogger);
    }
    
    
    private Gemeinde checkGemeinde(GemeindeDAO gemeindeDAO, XPPlan plan) throws SQLException {
        de.gdiservice.bplan.poi.Gemeinde[] gemeinden = null;
        if (plan instanceof BPlan) {
            gemeinden = ((BPlan)plan).getGemeinde();
        } else if (plan instanceof FPlan) {
            gemeinden = ((FPlan)plan).getGemeinde();
        } else if (plan instanceof SOPlan) {
            gemeinden = ((SOPlan)plan).getGemeinde();
        }
        if (gemeinden!=null && gemeinden.length>0) {
            de.gdiservice.bplan.poi.Gemeinde gemeinde = gemeinden[0];
            Gemeinde kvGemeinde = gemeindeDAO.find(gemeinde.getRs(), gemeinde.getAgs(), gemeinde.getGemeindename(),gemeinde.getOrtsteilname());
            if (kvGemeinde == null) {
                throw new IllegalArgumentException("BPLanImporter: Plan gmlId=\""+plan.getGml_id()+"\" konnte Gemeinde \""+ gemeinden + "\" nicht finden.");
            }
            return kvGemeinde;
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public void updatePlaene(Connection conWrite, Connection conRead, ImportLogger importLogger, ImportConfigEntry entry, List<T> bPlans) throws SQLException  {
        System.err.println("bplanTable="+bplanTable);
        System.err.println("bpbereichTable="+bpbereichTable);
        System.err.println(importLogger.getText());
        
        // TODO temp disabled
        conWrite.setAutoCommit(false);
        
        int countSucceded = 0;
        int countFailed = 0;
        int countSkipped = 0;
        int countNotValidated = 0;
        
        HashMap<UUID, T> lGmlIds = new HashMap<>();
        HashSet<UUID> lDoubleGmlIds = new HashSet<>();
        for (int i=0; i<bPlans.size(); i++) {
            T plan = bPlans.get(i);
            if (lGmlIds.containsKey(plan.getGml_id())) {
                System.err.println(plan);
                System.err.println(lGmlIds.get(plan.getGml_id()));
                lDoubleGmlIds.add(plan.getGml_id());
                importLogger.addError("Die GmlId =\""+plan.getGml_id()+"\" ist mehrfach vorhanden. Pläne mit dieser GmlId werden nicht eingelesen.");

            }
            lGmlIds.put(plan.getGml_id(), plan);
        }
        
        HashMap<UUID, B> lBPBereichGmlIds = new HashMap<>();
        HashSet<UUID> lDoubleBPBereichGmlIds = new HashSet<>();
        HashMap<UUID, B[]> lGehoertzuplan2BereicheereichGmlIds = new HashMap<>();
        List<B> lValidBereiche = new ArrayList<>();
        logger.info("verarbeite Bereiche");
        for (int i=0; i<bereiche.size(); i++) {
            B bereich = bereiche.get(i);
            if (lGmlIds.containsKey(bereich.getGml_id())) {
                lDoubleBPBereichGmlIds.add(bereich.getGml_id());
                logger.info("Es gibt mehrere Bereiche mit der GmlId =\""+bereich.getGml_id()+"\". Bereiche mit dieser GmlId werden nicht eingelesen.");
                importLogger.addError("Es gibt mehrere Bereiche mit der GmlId =\""+bereich.getGml_id()+"\". Bereiche mit dieser GmlId werden nicht eingelesen.");
            }
            lBPBereichGmlIds.put(bereich.getGml_id(), bereich);
            UUID gehoertzuplan = bereich.getGehoertzuplan();
            if (gehoertzuplan == null) {
                logger.info("Bei dem Bereich mit GmlId =\""+bereich.getGml_id()+"\" ist der Wert gehoertzuplan nicht gesetzt. Wird ignoriert");
                importLogger.addError("Bei dem BP_Bereich mit GmlId =\""+bereich.getGml_id()+"\" ist der Wert gehoertzuplan nicht gesetzt. Wird ignoriert");
            } else {                
                T plan = lGmlIds.get(gehoertzuplan);
                if (plan == null) {
                    logger.info("Der Bereich mit GmlId =\""+bereich.getGml_id()+"\" verweist auf einen nicht existierenden PLan (UUID="+gehoertzuplan+"). Wird ignoriert");                    
                    importLogger.addError("Der Bereich mit GmlId =\""+bereich.getGml_id()+"\" verweist auf einen nicht existierenden PLan (UUID="+gehoertzuplan+"). Wird ignoriert");                    
                } else {
                    if (bereich.gml_id == null) {                    
                        bereich.gml_id = UUID.randomUUID();
                        logger.info("Bei einem Bereich für den Plan \""+bereich.getGehoertzuplan()+"\" ist keine GmlId angegeben. Zufällige GmlId \""+bereich.gml_id +"\" wurde erzeugt.");
                        importLogger.addError("Bei einem BP_Bereich für den Plan \""+bereich.getGehoertzuplan()+"\" ist keine GmlId angegeben. Zufällige GmlId \""+bereich.gml_id +"\" wurde erzeugt.");
                    }
                    lValidBereiche.add(bereich);
                    B[] bereiche = lGehoertzuplan2BereicheereichGmlIds.get(gehoertzuplan);
                    if (bereiche == null) {                       
                        
                        B[] arrGehoertZuPlan = (B[]) Array.newInstance(XPBereich.class, 1);
                        arrGehoertZuPlan[0] = bereich;
//                        lGehoertzuplan2BereicheereichGmlIds.put(gehoertzuplan, (B[])Array.newInstance(bereich.getClass(), 1));
                        lGehoertzuplan2BereicheereichGmlIds.put(gehoertzuplan, arrGehoertZuPlan);
                    } else {
                        bereiche = Arrays.copyOf(bereiche, bereiche.length + 1);
                        bereiche[bereiche.length-1] = bereich;
//                        logger.debug("mehr als einer");
//                        for (int bNr=0; bNr<bereiche.length; bNr++) {
//                            logger.debug(bereiche[bNr].gml_id+"  gehörtzu="+bereiche[bNr].getGehoertzuplan());
//                        }
                        lGehoertzuplan2BereicheereichGmlIds.put(gehoertzuplan, bereiche);
                    }
                }
            }
        }
        logger.debug("Verarbeite Pläne");
        GemeindeDAO gemeindeDAO = new GemeindeDAO(conRead);
        
      
        
        AbstractDAO<UUID, T> bplanDao = null;
        XPBereichDAO<B> bereicheDAO = null;
      
        if (planClasz.equals(BPlan.class)) {
            bplanDao = (AbstractDAO<UUID, T>) new BPlanDAO(conWrite, conRead, bplanTable);
            bereicheDAO = (XPBereichDAO<B>) new BPBereichDAO(conWrite, conRead, bpbereichTable);
        } if (planClasz.equals(FPlan.class)) {
            bplanDao = (AbstractDAO<UUID, T>) new FPlanDAO(conWrite, conRead, bplanTable);
            bereicheDAO = (XPBereichDAO<B>) new FPBereichDAO(conWrite, conRead, bpbereichTable);
        } if (planClasz.equals(SOPlan.class)) {
            bplanDao = (AbstractDAO<UUID, T>) new SOPlanDAO(conWrite, conRead, bplanTable);
            bereicheDAO = (XPBereichDAO<B>) new SOBereichDAO(conWrite, conRead, bpbereichTable);
        }
           
        
        
        
        KonvertierungDAO konvertierungDAO = new KonvertierungDAO(conWrite, konvertierungTable);
        

        for (int i=0; i<bPlans.size(); i++) {
            
            
            
            
            T plan = bPlans.get(i);
            if ("d8969319-3f61-4377-9159-ba4f5a35f7e2".equals(plan.getGml_id().toString())) {
                SOPlan splan = (SOPlan)plan;
                logger.info("-----------------------------------");
                logger.info(plan.getName());
                logger.info(""+plan.getGml_id());
                logger.info("Nummer\t"+plan.getNummer());
                logger.info("Ändert\t"+Arrays.toString(plan.getAendert()));
                logger.info("Planart\t"+splan.getPlanart());
                logger.info("Gemeinde\t"+Arrays.toString(splan.getGemeinde()));
                logger.info("InternalId\t"+splan.getInternalId());
                logger.info("Externereferenzes\t"+Arrays.toString(splan.getExternereferenzes()));
                logger.info("Untergangsdatum\t"+splan.getUntergangsdatum());
                logger.info("Genehmigungsdatum\t"+splan.getGenehmigungsdatum());
                logger.info("WurdeGeaendertVon\t"+Arrays.toString(splan.getWurdeGeaendertVon()));
                logger.info("TechnHerstellDatum\t"+splan.getTechnHerstellDatum());
                logger.info("VeroeffentlichungsDatum\t"+splan.getVeroeffentlichungsDatum());
                logger.info("Planaufstellendegemeinde\t"+Arrays.toString(splan.getPlanaufstellendegemeinde()));
            }
            if ("a8bf465c-6557-4264-8759-3a3bef60efe8".equals(plan.getGml_id().toString())) {
                FPlan splan = (FPlan)plan;
                logger.info("name\t" + splan.getName());
                logger.info("gml_id\t" + splan.getGml_id());
                logger.info("nummer\t" + splan.getNummer());
                logger.info("status\t" + splan.getStatus());
                logger.info("aendert\t" + Arrays.toString(splan.getAendert()));
                logger.info("planart\t" + splan.getPlanart());
                logger.info("gemeinde\t" + Arrays.toString(splan.getGemeinde()));
                logger.info("verfahren\t" + splan.getVerfahren());
                logger.info("internalid\t" + splan.getInternalId());
                logger.info("rechtsstand\t" + splan.getRechtsstand());
                logger.info("externereferenz\t" + Arrays.toString(splan.getExternereferenzes()));
                logger.info("untergangsdatum\t" + splan.getUntergangsdatum());
                logger.info("genehmigungsdatum\t" + splan.getGenehmigungsdatum());
                logger.info("wirksamkeitsdatum\t" + splan.getWirksamkeitsdatum());
                logger.info("wurdegeaendertvon\t" + Arrays.toString(splan.getWurdeGeaendertVon()));
                logger.info("auslegungsenddatum\t" + Arrays.toString(splan.getAuslegungsenddatum()));
                logger.info("planbeschlussdatum\t" + splan.getPlanbeschlussdatum());
                logger.info("technherstelldatum\t" + splan.getTechnherstelldatum());
                logger.info("auslegungsstartdatum\t" + Arrays.toString(splan.getAuslegungsstartdatum()));
                logger.info("entwurfsbeschlussdatum\t" + splan.getEntwurfsbeschlussdatum());
                logger.info("veroeffentlichungsdatum\t" + splan.getVeroeffentlichungsdatum());
                logger.info("planaufstellendegemeinde\t" + Arrays.toString(splan.getPlanaufstellendeGemeinde()));
                logger.info("aufstellungsbeschlussdatum\t" + splan.getAufstellungsbeschlussDatum());
                logger.info("traegerbeteiligungsenddatum\t" + Arrays.toString(splan.getTraegerbeteiligungsenddatum()));
                logger.info("traegerbeteiligungsstartdatum\t" + Arrays.toString(splan.getTraegerbeteiligungsstartdatum()));
            }
            if (lDoubleGmlIds.contains(plan.getGml_id())) {
                countSkipped++;
                continue;
            }
            logger.debug("Verarbeite: {} {}", plan.getGml_id(), plan.getName());            
            try {
                Gemeinde kvGemeinde = checkGemeinde(gemeindeDAO, plan);
                String geomValidierungsResult = bplanDao.validateGeom(plan.getGeom());
                if (!"Valid Geometry".equals(geomValidierungsResult)) {
                    logger.info("Plan mit der gmlId=\""+plan.getGml_id()+"\" Geometry is not valid: "+ geomValidierungsResult +".");
                    throw new IllegalArgumentException("Plan mit der gmlId=\""+plan.getGml_id()+"\" hat keine gültige Geometry: \""+geomValidierungsResult+"\".");
                } 
                
                B[] bereiche = lGehoertzuplan2BereicheereichGmlIds.get(plan.getGml_id());
                if (bereiche == null) {
                    logger.info("kein Bereich für Plan gmlId=\""+plan.getGml_id()+"\"");
                    importLogger.addLine(String.format("kein Bereich für Plan gmlId=\""+plan.getGml_id()+"\""));
                } else {
                    StringJoiner sj = new StringJoiner(", ");
                    Arrays.stream(bereiche).forEach((a)->sj.add(a.gml_id+"=>"+a.getGehoertzuplan()));
                    logger.info(bereiche.length+" Bereich(e) in der WFS für Plan gmlId=\""+plan.getGml_id()+"\" "+sj);                    
                }
                
                T dbPlan = bplanDao.findById(plan.getGml_id());
                Konvertierung konvertierung = null;
                boolean hasPlanChanged = false;
                boolean hasBereicheChanged = false;
                if (dbPlan != null) {
                    logger.debug("Plan in der DB gefunden konvertierungsId=" + dbPlan.getKonvertierungId());
                    if (dbPlan.getKonvertierungId() != null) {
                        konvertierung = konvertierungDAO.find(dbPlan.getKonvertierungId());
                        if (konvertierung != null) {
                            plan.setKonvertierungId(konvertierung.id);
                            konvertierungDAO.updatePublishFlag(plan.getKonvertierungId(), false);
                            if (bereiche != null) {
                                for (B b : bereiche) {
                                    b.konvertierung_id = konvertierung.id;
                                }
                            }
                        } else {
                            logger.info("plan exists - keine zugehörige Konvertierung");
                            hasPlanChanged = true;
                            konvertierung = createKonvertierung(dbPlan, kvGemeinde);
                            konvertierung = konvertierungDAO.insert(konvertierung);
                            logger.info("neue Konvertierung mit der Id "+ konvertierung.id+" gespeichert.");
                            plan.setKonvertierungId(konvertierung.id);
                        }
                    }
                    if (hasPlanChanged || HasChangedFunctions.hasChanged(plan, dbPlan)) {                                                
                        int count = bplanDao.update(plan);                        
                        hasBereicheChanged = updateBereiche(bereicheDAO, bereiche, konvertierung.id);                        
                        logger.info("plan exists - changed => update sucess="+(count==1));
                        hasPlanChanged = true;                       
                    } else {
                        hasBereicheChanged = updateBereiche(bereicheDAO, bereiche, dbPlan.getKonvertierungId());                        
                        if (hasBereicheChanged) {                            
                            logger.debug(String.format("unchanged %s, Bereiche changed", plan.getGml_id()));
                            importLogger.addLine(String.format("unchanged %s, Bereiche changed", plan.getGml_id()));
                        } else {
                            logger.debug(String.format("unchanged %s", plan.getGml_id()));
                            importLogger.addLine(String.format("unchanged %s", plan.getGml_id()));
                        }
                        countSucceded++;
                    }
                    if (hasPlanChanged || hasBereicheChanged) {
                        conWrite.commit();
                        if (!hasPlanChanged) {
                            logger.debug(String.format("updated %s (nur Bereiche)", plan.getGml_id()));
                            importLogger.addLine(String.format("updated %s (nur Bereiche)", plan.getGml_id()));
                        } else {
                            logger.debug(String.format("updated %s", plan.getGml_id()));
                            importLogger.addLine(String.format("updated %s", plan.getGml_id()));
                        }
                    }
                } else {
                    logger.debug("plan doesnt exist =>insert");                    
                    if (plan instanceof BPlan) {
                        checkPlanArt((BPlan)plan);
                    } else if (plan instanceof FPlan) {
                        checkPlanArt((FPlan)plan);                        
                    }
                    
                    konvertierung = createKonvertierung(plan, kvGemeinde);
                    konvertierung = konvertierungDAO.insert(konvertierung);                    
                    plan.setKonvertierungId(konvertierung.id);
                    logger.info("neue Konvertierung mit der Id "+ konvertierung.id+" gespeichert.");
                    bplanDao.insert(plan);
                    try {
                        updateBereiche(bereicheDAO, bereiche, konvertierung.id);
                    } catch (Exception e) {
                        throw new BereichsImportException("Bereich(e) konnte(n) nicht aktualisiert werden: "+e.getMessage(), e);
                    }
                    conWrite.commit();
                    importLogger.addLine(String.format("inserted %s", plan.getGml_id()));
                    hasPlanChanged = true;
                }
                
                if (hasPlanChanged || hasBereicheChanged) {
                    
                    boolean succeded = validate(konvertierung, plan, kvwmapUrl, importLogger);
                    if (succeded) {
                        LocalDate publishDate = plan.getPublishDate();
                        if (publishDate!=null) {
                            konvertierungDAO.updatePublishDate(konvertierung.id, publishDate);
                        }
                        conWrite.commit();                                  
                        countSucceded++;
                    } else {
                        countNotValidated++;
                    }
                }
                
            } catch (Exception e) {
                importLogger.addError("Fehler beim Import Plan mit gml_id=\""+plan.getGml_id()+"\": " +e.getMessage());
                e.printStackTrace();
                conWrite.rollback();
                countFailed++;
            }
            
        } 
        logger.info("count="+bPlans.size()+" succeded="+countSucceded+ " failed="+countFailed+ " skipped="+countSkipped+" countNotValidated="+countNotValidated);          

    }
    
    private boolean hasChanged(List<B> dbBereiche, B[] bereiche) {
        if (dbBereiche.size() != bereiche.length) {
            return true;
        }
        for (int i=0; i<bereiche.length; i++) {
            boolean hasChanged = HasChangedFunctions.hasChanged(dbBereiche.get(i), bereiche[i]);
            if (hasChanged) {
                for (int nr=0; nr<bereiche.length; nr++) {
                    B b01 = dbBereiche.get(nr);
                    B b02 = bereiche[nr];
                    logger.info("nr: "+nr);
                    logger.info("\t"+b01.toString());
                    logger.info("\t"+b02.toString());
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Update or insert Bereich if necessary
     * 
     * @param bereicheDAO
     * @param wfsBereiche
     * @param konvertierung_id
     * @return true if bereiche needed to upate and was updated
     * @throws SQLException
     */
    private boolean updateBereiche(XPBereichDAO<B> bereicheDAO, B[] wfsBereiche, Integer konvertierung_id) throws SQLException, BereichsImportException {
        boolean updated = false;
        if (wfsBereiche != null && wfsBereiche.length>0) {
            List<B> dbBereicheByPlan = bereicheDAO.findByGehoertzuplan(wfsBereiche[0].getGehoertzuplan());
            List<B> dbBereicheByKonvertierung = bereicheDAO.findByKonvertierungsId(konvertierung_id);
            if (dbBereicheByPlan.size()==0 && dbBereicheByKonvertierung.size()==0) {
                // keine Bereiche für den Plan in der DB
                logger.debug("keine Bereich(e) in der DB gefunden");
                updated = true;
                StringJoiner sjInserted = new StringJoiner(", ");
                for (B wfsBereich : wfsBereiche) {
                    wfsBereich.konvertierung_id = konvertierung_id;
                    try {
                        bereicheDAO.insert(wfsBereich);
                    } catch (Exception ex) {
                        throw new BereichsImportException("Bereich "+wfsBereich.getGml_id()+" konnte nicht eingefügt werden. "+ex.getMessage(), ex);
                    }
                    sjInserted.add(wfsBereich.gml_id+" konfId="+wfsBereich.konvertierung_id+" => "+wfsBereich.getGehoertzuplan());
                }                    
                logger.debug("Inserted "+wfsBereiche.length+" Bereich(e) bereiche="+sjInserted);
            } else {
                Arrays.sort(wfsBereiche, (a, b) -> a.gml_id.compareTo(b.gml_id));
                Collections.sort(dbBereicheByPlan, (a, b) -> a.gml_id.compareTo(b.gml_id));
                Collections.sort(dbBereicheByKonvertierung, (a, b) -> a.gml_id.compareTo(b.gml_id));
                if (hasChanged(dbBereicheByPlan, wfsBereiche) ||  hasChanged(dbBereicheByKonvertierung, wfsBereiche)) {
                    Map<UUID, B> lBereiche = new HashMap<>();
                    for (B b : dbBereicheByKonvertierung) {                        
                        lBereiche.put(b.gml_id, b);
                    }                    
                    for (B b : dbBereicheByPlan) {
                        lBereiche.put(b.gml_id, b);
                    }
                    List<B> lBereichToDelete = new ArrayList<>(lBereiche.values());
                    int countDeleted = bereicheDAO.delete(lBereichToDelete);
                    
                    StringJoiner sj = new StringJoiner(", ");
                    lBereichToDelete.forEach((a)->sj.add(a.gml_id+" konfId="+a.konvertierung_id+" => "+a.getGehoertzuplan()));
                    logger.debug("Deleted "+countDeleted+" Bereich(e) bereiche="+sj);
                    
                    StringJoiner sjInserted = new StringJoiner(", ");
                    for (B wfsBereich : wfsBereiche) {
                        wfsBereich.konvertierung_id = konvertierung_id;
                        try {
                            bereicheDAO.insert(wfsBereich);
                        } catch (Exception ex) {
                            throw new BereichsImportException("Bereich "+wfsBereich.getGml_id()+" konnte nicht eingefügt werden. "+ex.getMessage(), ex);
                        }                        
                        sjInserted.add(wfsBereich.gml_id+" konfId="+wfsBereich.konvertierung_id+" => "+wfsBereich.getGehoertzuplan());
                    }                    
                    logger.debug("Inserted "+wfsBereiche.length+" Bereich(e) bereiche="+sjInserted);
                    
                    updated = true;
                } else {
                    logger.debug(wfsBereiche.length +"Bereich(e) unchanged");
                }
            }
        }
        return updated;
    }
    
    
    static class BereichsImportException extends Exception {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public BereichsImportException(String message) {
            super(message);
        }

        public BereichsImportException(String message, Throwable cause) {
            super(message, cause);
        }

    }

}
