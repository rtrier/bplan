package de.gdiservice.bplan;

import java.sql.Connection;
import java.sql.SQLException;
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

import de.gdiservice.bplan.poi.*;
import de.gdiservice.bplan.dao.BPBereichDAO;
import de.gdiservice.bplan.dao.BPlanDAO;
import de.gdiservice.bplan.konvertierung.Gemeinde;
import de.gdiservice.bplan.konvertierung.GemeindeDAO;
import de.gdiservice.bplan.konvertierung.Konvertierung;
import de.gdiservice.bplan.konvertierung.Konvertierung.KonvertierungStatus;
import de.gdiservice.bplan.konvertierung.KonvertierungDAO;
import de.gdiservice.wfs.BPBereichFactory;
import de.gdiservice.wfs.GeolexBPlanFactory;
import de.gdiservice.wfs.WFSClient;
import de.gdiservice.wfs.WFSFactory;

public class PlanNwmImporter extends BPlanImporter {
    
    final static Logger logger = LoggerFactory.getLogger(PlanNwmImporter.class);
    
    protected String bpbereichTable;
    
    List<BPBereich> bereiche;
    

    public PlanNwmImporter(String konvertierungTable, String bplanTable, String bpbereichTable, Version version, String kvwmapUrl, String kvwmapLoginName, String kvwmapPassword) {
        super(konvertierungTable, bplanTable, version, kvwmapUrl, kvwmapLoginName, kvwmapPassword);
        this.bpbereichTable = bpbereichTable;
        System.err.println("newBPlanNwmImporter");
    }
    
    protected WFSFactory<BPlan> getWFSFactory(Version version) {
        return new GeolexBPlanFactory(true);
    }
    
    
    static Konvertierung createKonvertierung(BPlan plan, Gemeinde gemeinde) {
        Konvertierung konvertierung = new Konvertierung();
        konvertierung.stelle_id = gemeinde.stelle_id;
        konvertierung.status = KonvertierungStatus.erstellt;
        konvertierung.user_id = 1;
        konvertierung.gebietseinheiten = gemeinde.rs;
        konvertierung.bezeichnung = plan.getName()+" "+gemeinde.gmd_name+" "+plan.getNummer();
        konvertierung.veroeffentlicht = false;
        konvertierung.beschreibung = "automatically created from wfs-nwm";
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
        konvertierung.planart = "BP-Plan";
        konvertierung.epsg = Konvertierung.EPSGCodes.EPSG_25833;
        konvertierung.input_epsg = Konvertierung.EPSGCodes.EPSG_25833;
        konvertierung.output_epsg = Konvertierung.EPSGCodes.EPSG_25833;
        konvertierung.geom_precision =3;  
        return konvertierung;
    }    
    
    
    public void importWFS(Connection conWrite, Connection conRead, ImportConfigEntry entry, ImportLogger importLogger) throws Exception  {
        
        bereiche = null;        
        String onlineresource = "https://nwm.bf.geoplex.de/interface/wfs-ms/BP_Bereich"; 
        try {
            String sVersion = (entry.onlineresource.indexOf("nwm")>=0) ? "2.0.0" : "1.1.0";
            final String wfsUrl = "https://nwm.bf.geoplex.de/interface/wfs-ms/BP_Bereich?service=WFS&VERSION="+sVersion+"&REQUEST=GetFeature&TYPENAME=BP_Bereich&SRSNAME=epsg:25833";
            
            importLogger.addLine("Reading WFS: \""+ onlineresource + "\"");
            bereiche = WFSClient.read(wfsUrl, new BPBereichFactory(), importLogger);
            logger.info("read Bereiche done."+bereiche.size()+" Bereiche");
        } 
        catch (Exception ex) {
            logger.error("error reading from service " + entry.bezeichnung + " with url=\"" + onlineresource +"\"", ex);
            importLogger.addError("ERROR - Reading WFS: \""+ onlineresource + "\" error:["+ex.getMessage()+"]");                
        }
        super.importWFS(conWrite, conRead, entry, importLogger);
    }
    
    
    public void updateBPlaene(Connection conWrite, Connection conRead, ImportLogger importLogger, ImportConfigEntry entry, List<BPlan> bPlans) throws SQLException  {
        System.err.println("bplanTable="+bplanTable);
        System.err.println("bpbereichTable="+bpbereichTable);
        System.err.println(importLogger.getText());
        
        // TODO temp disabled
        conWrite.setAutoCommit(false);
        
        int countSucceded = 0;
        int countFailed = 0;
        int countSkipped = 0;
        int countNotValidated = 0;
        
        HashMap<UUID, BPlan> lGmlIds = new HashMap<>();
        HashSet<UUID> lDoubleGmlIds = new HashSet<>();
        for (int i=0; i<bPlans.size(); i++) {
            BPlan plan = bPlans.get(i);
            if (lGmlIds.containsKey(plan.getGml_id())) {
                System.err.println(plan);
                System.err.println(lGmlIds.get(plan.getGml_id()));
                lDoubleGmlIds.add(plan.getGml_id());
                importLogger.addError("Die GmlId =\""+plan.getGml_id()+"\" ist mehrfach vorhanden. Pläne mit dieser GmlId werden nicht eingelesen.");

            }
            lGmlIds.put(plan.getGml_id(), plan);
        }
        
        HashMap<UUID, BPBereich> lBPBereichGmlIds = new HashMap<>();
        HashSet<UUID> lDoubleBPBereichGmlIds = new HashSet<>();
        HashMap<UUID, BPBereich[]> lGehoertzuplan2BereicheereichGmlIds = new HashMap<>();
        List<BPBereich> lValidBereiche = new ArrayList<>();
        logger.info("verarbeite Bereiche");
        for (int i=0; i<bereiche.size(); i++) {
            BPBereich bereich = bereiche.get(i);
            if (lGmlIds.containsKey(bereich.getGml_id())) {
                lDoubleBPBereichGmlIds.add(bereich.getGml_id());
                logger.info("Es gibt mehrere BP_Bereiche mit der GmlId =\""+bereich.getGml_id()+"\". Bereiche mit dieser GmlId werden nicht eingelesen.");
                importLogger.addError("Es gibt mehrere BP_Bereiche mit der GmlId =\""+bereich.getGml_id()+"\". Bereiche mit dieser GmlId werden nicht eingelesen.");
            }
            lBPBereichGmlIds.put(bereich.getGml_id(), bereich);
            UUID gehoertzuplan = bereich.getGehoertzuplan();
            if (gehoertzuplan == null) {
                logger.info("Bei dem BP_Bereich mit GmlId =\""+bereich.getGml_id()+"\" ist der Wert gehoertzuplan nicht gesetzt. Wird ignoriert");
                importLogger.addError("Bei dem BP_Bereich mit GmlId =\""+bereich.getGml_id()+"\" ist der Wert gehoertzuplan nicht gesetzt. Wird ignoriert");
            } else {                
                BPlan plan = lGmlIds.get(gehoertzuplan);
                if (plan == null) {
                    logger.info("Der BP_Bereich mit GmlId =\""+bereich.getGml_id()+"\" verweist auf einen nicht existierenden BPLan (UUID="+gehoertzuplan+"). Wird ignoriert");                    
                    importLogger.addError("Der BP_Bereich mit GmlId =\""+bereich.getGml_id()+"\" verweist auf einen nicht existierenden BPLan (UUID="+gehoertzuplan+"). Wird ignoriert");                    
                } else {
                    if (bereich.gml_id == null) {                    
                        bereich.gml_id = UUID.randomUUID();
                        logger.info("Bei dem BP_Bereich für den Plan \""+bereich.getGehoertzuplan()+"\" ist keim GmlId angegeben. Zufällige GmlId \""+bereich.gml_id +"\" wurde erzeugt.");
                        importLogger.addError("Bei dem BP_Bereich für den Plan \""+bereich.getGehoertzuplan()+"\" ist keim GmlId angegeben. Zufällige GmlId \""+bereich.gml_id +"\" wurde erzeugt.");
                    }
                    lValidBereiche.add(bereich);
                    BPBereich[] bereiche = lGehoertzuplan2BereicheereichGmlIds.get(gehoertzuplan);
                    if (bereiche == null) {
                        lGehoertzuplan2BereicheereichGmlIds.put(gehoertzuplan, new BPBereich[] {bereich});
                    } else {
                        bereiche = Arrays.copyOf(bereiche, bereiche.length + 1);
                        bereiche[bereiche.length-1] = bereich;
                        logger.debug("mehr als einer");
                        for (int bNr=0; bNr<bereiche.length; bNr++) {
                            logger.debug(bereiche[bNr].gml_id+"  gehörtzu="+bereiche[bNr].getGehoertzuplan());
                        }
                        lGehoertzuplan2BereicheereichGmlIds.put(gehoertzuplan, bereiche);
                    }
                }
            }
        }
        logger.debug("Verarbeite Pläne");
        GemeindeDAO gemeindeDAO = new GemeindeDAO(conRead);
        BPlanDAO bplanDao = new BPlanDAO(conWrite, conRead, bplanTable);
        BPBereichDAO bereicheDAO = new BPBereichDAO(conWrite, conRead, bpbereichTable);
        KonvertierungDAO konvertierungDAO = new KonvertierungDAO(conWrite, konvertierungTable);

        for (int i=0; i<bPlans.size(); i++) {
            BPlan plan = bPlans.get(i);
            if (lDoubleGmlIds.contains(plan.getGml_id())) {
                countSkipped++;
                continue;
            }
            logger.debug("Verarbeite: {} {}", plan.getGml_id(), plan.getName());            
            try {
                de.gdiservice.bplan.poi.Gemeinde gemeinde = plan.getGemeinde()[0];
                Gemeinde kvGemeinde = gemeindeDAO.find(gemeinde.getRs(), gemeinde.getAgs(), gemeinde.getGemeindename(),gemeinde.getOrtsteilname());
                if (kvGemeinde == null) {
                    throw new IllegalArgumentException("BPLanImporter: Plan gmlId=\""+plan.getGml_id()+"\" konnte Gemeinde \""+ gemeinde + "\" nicht finden.");
                }
                String geomValidierungsResult = bplanDao.validateGeom(plan.getGeom());
                if (!"Valid Geometry".equals(geomValidierungsResult)) {
                    logger.info("BPLanImporter: Plan gmlId=\""+plan.getGml_id()+"\" Geometry is not valid: "+ geomValidierungsResult +".");
                    throw new IllegalArgumentException("BPlanImporter: Plan gmlId=\""+plan.getGml_id()+"\" hat keine gültige Geometry: \""+geomValidierungsResult+"\".");
                } 
                
                BPBereich[] bereiche = lGehoertzuplan2BereicheereichGmlIds.get(plan.getGml_id());
                if (bereiche == null) {
                    logger.info("BPLanImporter: kein BP_Bereich für Plan gmlId=\""+plan.getGml_id()+"\"");
                    importLogger.addLine(String.format("BPLanImporter: kein BP_Bereich für Plan gmlId=\""+plan.getGml_id()+"\""));
                } else {
                    StringJoiner sj = new StringJoiner(", ");
                    Arrays.stream(bereiche).forEach((a)->sj.add(a.gml_id+"=>"+a.getGehoertzuplan()));
                    logger.info("BPLanImporter: "+bereiche.length+" BP_Bereich(e) in der WFS für Plan gmlId=\""+plan.getGml_id()+"\" "+sj);                    
                }
                
                BPlan dbPlan = bplanDao.findById(plan.getGml_id());
                Konvertierung konvertierung = null;
                boolean hasChanged = false;
                if (dbPlan != null) {
                    logger.debug("Plan in der DB gefunden konvertierungsId=" + dbPlan.getKonvertierungId());
                    if (dbPlan.getKonvertierungId() != null) {
                        konvertierung = konvertierungDAO.find(dbPlan.getKonvertierungId());
                        if (konvertierung != null) {
                            plan.setKonvertierungId(konvertierung.id);
                            konvertierungDAO.updatePublishFlag(plan.getKonvertierungId(), false);
                            for (BPBereich b : bereiche) {
                                b.konvertierung_id = konvertierung.id;
                            }
                        } else {
                            logger.info("plan exists - keine zugehörige Konvertierung");
                            hasChanged = true;
                            konvertierung = createKonvertierung(dbPlan, kvGemeinde);
                            konvertierung = konvertierungDAO.insert(konvertierung);                    
                            plan.setKonvertierungId(konvertierung.id);
                        }
                    }
                    if (hasChanged || HasChangedFunctions.hasChanged(plan, dbPlan)) {                                                
                        int count = bplanDao.update(plan);
                        updateBereiche(bereicheDAO, bereiche, konvertierung.id);                        
                        logger.info("plan exists - changed => update sucess="+(count==1));
                        hasChanged = true;
                        importLogger.addLine(String.format("updated %s", plan.getGml_id()));
                    } else {
                        hasChanged = updateBereiche(bereicheDAO, bereiche, dbPlan.getKonvertierungId());                        
                        if (hasChanged) {                            
                            logger.debug(String.format("unchanged %s, Bereiche changed", plan.getGml_id()));
                            importLogger.addLine(String.format("unchanged %s, Bereiche changed", plan.getGml_id()));
                        } else {
                            logger.debug(String.format("unchanged %s", plan.getGml_id()));
                            importLogger.addLine(String.format("unchanged %s", plan.getGml_id()));
                        }
                        countSucceded++;
                    }
                } else {
                    logger.debug("plan doesnt exist =>insert");
                    konvertierung = createKonvertierung(plan, kvGemeinde);
                    Konvertierung dbKonvertierung = konvertierungDAO.insert(konvertierung);                    
                    plan.setKonvertierungId(dbKonvertierung.id);
                    bplanDao.insert(plan);
                    importLogger.addLine(String.format("inserted %s", plan.getGml_id()));
                    updateBereiche(bereicheDAO, bereiche, dbKonvertierung.id);                                        
                    hasChanged = true;
                }
                
                if (hasChanged) {
                    conWrite.commit();
                    boolean succeded = validate(konvertierung, plan, kvwmapUrl, importLogger);
                    if (succeded) {
                        if (plan.inkrafttretensdatum!=null && succeded) {
                            konvertierungDAO.updatePublishDate(konvertierung.id, plan.inkrafttretensdatum);
                        }
                        conWrite.commit();                                  
                        countSucceded++;
                    } else {
                        countNotValidated++;
                    }
                }
                
            } catch (Exception e) {
                importLogger.addError("Fehler import Plan mit gml_id=\""+plan.getGml_id()+"\": " +e.getMessage());
                e.printStackTrace();
                conWrite.rollback();
                countFailed++;
            }
            
        } 
        logger.info("count="+bPlans.size()+" succeded="+countSucceded+ " failed="+countFailed+ " skipped="+countSkipped+" countNotValidated="+countNotValidated);          

    }
    
    private boolean hasChanged(List<BPBereich> dbBereiche, BPBereich[] bereiche) {
        if (dbBereiche.size() != bereiche.length) {
            return true;
        }
        for (int i=0; i<bereiche.length; i++) {
            boolean hasChanged = HasChangedFunctions.hasChanged(dbBereiche.get(i), bereiche[i]);
            if (hasChanged) {
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
    private boolean updateBereiche(BPBereichDAO bereicheDAO, BPBereich[] wfsBereiche, Integer konvertierung_id) throws SQLException {
        boolean updated = false;
        if (wfsBereiche != null && wfsBereiche.length>0) {
            List<BPBereich> dbBereicheByPlan = bereicheDAO.findByGehoertzuplan(wfsBereiche[0].getGehoertzuplan());
            List<BPBereich> dbBereicheByKonvertierung = bereicheDAO.findByKonvertierungsId(konvertierung_id);
            if (dbBereicheByPlan.size()==0 && dbBereicheByKonvertierung.size()==0) {
                // keine Bereiche für den Plan in der DB
                logger.debug("keine Bereich(e) in der DB gefunden");
                updated = true;
                StringJoiner sjInserted = new StringJoiner(", ");
                for (BPBereich wfsBereich : wfsBereiche) {
                    wfsBereich.konvertierung_id = konvertierung_id;
                    bereicheDAO.insert(wfsBereich);
                    sjInserted.add(wfsBereich.gml_id+" konfId="+wfsBereich.konvertierung_id+" => "+wfsBereich.getGehoertzuplan());
                }                    
                logger.debug("Inserted "+wfsBereiche.length+" Bereich(e) bereiche="+sjInserted);
            } else {
                Arrays.sort(wfsBereiche, (a, b) -> a.gml_id.compareTo(b.gml_id));
                Collections.sort(dbBereicheByPlan, (a, b) -> a.gml_id.compareTo(b.gml_id));
                Collections.sort(dbBereicheByKonvertierung, (a, b) -> a.gml_id.compareTo(b.gml_id));
                if (hasChanged(dbBereicheByPlan, wfsBereiche) ||  hasChanged(dbBereicheByKonvertierung, wfsBereiche)) {
                    Map<UUID, BPBereich> lBereiche = new HashMap<>();
                    for (BPBereich b : dbBereicheByKonvertierung) {
                        lBereiche.put(b.gml_id, b);
                    }                    
                    for (BPBereich b : dbBereicheByPlan) {
                        lBereiche.put(b.gml_id, b);
                    }
                    List<BPBereich> lBereichToDelete = new ArrayList<>(lBereiche.values());
                    int countDeleted = bereicheDAO.delete(lBereichToDelete);
                    
                    StringJoiner sj = new StringJoiner(", ");
                    lBereichToDelete.forEach((a)->sj.add(a.gml_id+" konfId="+a.konvertierung_id+" => "+a.getGehoertzuplan()));
                    logger.debug("Deleted "+countDeleted+" Bereich(e) bereiche="+sj);
                    
                    StringJoiner sjInserted = new StringJoiner(", ");
                    for (BPBereich wfsBereich : wfsBereiche) {
                        wfsBereich.konvertierung_id = konvertierung_id;
                        bereicheDAO.insert(wfsBereich);
                        sjInserted.add(wfsBereich.gml_id+" konfId="+wfsBereich.konvertierung_id+" => "+wfsBereich.getGehoertzuplan());
                    }                    
                    logger.debug("Inserted "+wfsBereiche.length+" Bereich(e) bereiche="+sjInserted);
                    
                    updated = true;
                }               
            }
        }
        return updated;
    }    

}
