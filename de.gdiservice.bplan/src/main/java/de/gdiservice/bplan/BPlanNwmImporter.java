package de.gdiservice.bplan;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import de.gdiservice.bplan.konvertierung.Gemeinde;
import de.gdiservice.bplan.konvertierung.GemeindeDAO;
import de.gdiservice.bplan.konvertierung.Konvertierung;
import de.gdiservice.bplan.konvertierung.Konvertierung.KonvertierungStatus;
import de.gdiservice.bplan.konvertierung.KonvertierungDAO;
import de.gdiservice.wfs.GeolexBPlanFactory;
import de.gdiservice.wfs.WFSFactory;

public class BPlanNwmImporter extends BPlanImporter {

    public BPlanNwmImporter(String konvertierungTable, String bplanTable, Version version, String kvwmapUrl, String kvwmapLoginName, String kvwmapPassword) {
        super(konvertierungTable, bplanTable, version, kvwmapUrl, kvwmapLoginName, kvwmapPassword);
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
    
    
    public void updateBPlaene(Connection conWrite, Connection conRead, ImportLogger importLogger, ImportConfigEntry entry, List<BPlan> bPlans) throws SQLException  {
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
        
        GemeindeDAO gemeindeDAO = new GemeindeDAO(conRead);
        BPlanDAO bplanDao = new BPlanDAO(conWrite, conRead, bplanTable);
        KonvertierungDAO konvertierungDAO = new KonvertierungDAO(conWrite, konvertierungTable);

        for (int i=0; i<bPlans.size(); i++) {
            BPlan plan = bPlans.get(i);
            if (lDoubleGmlIds.contains(plan.gml_id)) {
                countSkipped++;
                continue;
            }
            logger.debug("Verarbeite: {} {}", plan.getGml_id(), plan.getName());            
            try {
                de.gdiservice.bplan.Gemeinde gemeinde = plan.getGemeinde()[0];
                Gemeinde kvGemeinde = gemeindeDAO.find(gemeinde.getRs(), gemeinde.getAgs(), gemeinde.getGemeindename(),gemeinde.getOrtsteilname());
                if (kvGemeinde == null) {
                    throw new IllegalArgumentException("BPLanImporter: Plan gmlId=\""+plan.getGml_id()+"\" konnte Gemeinde \""+ gemeinde + "\" nicht finden.");
                }
                String geomValidierungsResult = bplanDao.validateGeom(plan.getGeom());
                if (!"Valid Geometry".equals(geomValidierungsResult)) {
                    logger.info("BPLanImporter: Plan gmlId=\""+plan.getGml_id()+"\" Geometry is not valid: "+ geomValidierungsResult +".");
                    throw new IllegalArgumentException("BPlanImporter: Plan gmlId=\""+plan.getGml_id()+"\" hat keine gültige Geometry: \""+geomValidierungsResult+"\".");
                } 
                BPlan dbPlan = bplanDao.findById(plan.getGml_id());
                Konvertierung konvertierung = null;
                if (dbPlan != null) {
                    logger.debug("dbPlan.getStatus() " + dbPlan.getStatus());
                    if (BPlanImporter.hasChanged(plan, dbPlan)) { 
                        if (dbPlan.getKonvertierungId() != null) {
                            plan.setKonvertierungId(dbPlan.getKonvertierungId());
                            int updateCount = konvertierungDAO.update(plan.konvertierung_id);
                            if (updateCount == 1) {
                                // throw new IllegalArgumentException("In der DB existiert ein BPlan mit der gmlId. Der zugehörige Eintrag in der Konvertierungs-Tabelle existiert aber nicht.");
                                konvertierungDAO.updatePublishFlag(plan.konvertierung_id, false);
                                konvertierung = konvertierungDAO.find(plan.konvertierung_id);
                            }   
                        }
                        if (konvertierung == null) {
                            konvertierung = createKonvertierung(dbPlan, kvGemeinde);
                            Konvertierung dbKonvertierung = konvertierungDAO.insert(konvertierung);                    
                            plan.setKonvertierungId(dbKonvertierung.id);
                        }
                         
                        boolean sucess = bplanDao.update(plan);
                        logger.info("plan exists - changed =>update sucess="+sucess);
                        importLogger.addLine(String.format("updated %s", plan.getGml_id()));
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
                    } else {                                
                        logger.debug("plan exists - unchanged");
//                        lSuccededGmlIds.add(plan.getGml_id().toString());
                        importLogger.addLine(String.format("unchanged %s", plan.getGml_id()));
                        countSucceded++;
                    }
                } else {
                    logger.debug("plan doesnt exist =>insert");
                    konvertierung = createKonvertierung(plan, kvGemeinde);
                    Konvertierung dbKonvertierung = konvertierungDAO.insert(konvertierung);                    
                    plan.setKonvertierungId(dbKonvertierung.id);
                    bplanDao.insert(plan);
                    importLogger.addLine(String.format("inserted %s", plan.getGml_id()));
                    conWrite.commit();
                    boolean succeded = validate(konvertierung, plan, kvwmapUrl, importLogger);
                    // konvertierungDAO.updatePublishFlag(konvertierung.id, succeded);
                    if (succeded) {
                        if (plan.inkrafttretensdatum!=null) {
                            konvertierungDAO.updatePublishDate(konvertierung.id, plan.inkrafttretensdatum);
                            conWrite.commit();
                        }
                        countSucceded++;
                    } else {
                        countNotValidated++;
                    }
                }
            } catch (Exception e) {
                conWrite.rollback();
                importLogger.addError("Fehler import Plan mit gml_id=\""+plan.getGml_id()+"\": " +e.getMessage());
                e.printStackTrace();
                countFailed++;
            }
        } 
        logger.info("count="+bPlans.size()+" succeded="+countSucceded+ " failed="+countFailed+ " skipped="+countSkipped+" countNotValidated="+countNotValidated);          

    }    

}
