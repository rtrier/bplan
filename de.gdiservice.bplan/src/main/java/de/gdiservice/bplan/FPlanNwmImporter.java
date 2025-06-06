package de.gdiservice.bplan;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import de.gdiservice.bplan.dao.FPlanDAO;
import de.gdiservice.bplan.konvertierung.Gemeinde;
import de.gdiservice.bplan.konvertierung.GemeindeDAO;
import de.gdiservice.bplan.konvertierung.Konvertierung;
import de.gdiservice.bplan.konvertierung.Konvertierung.KonvertierungStatus;
import de.gdiservice.bplan.konvertierung.KonvertierungDAO;
import de.gdiservice.bplan.poi.FPlan;
import de.gdiservice.bplan.poi.HasChangedFunctions;

public class FPlanNwmImporter extends FPlanImporter {

    public FPlanNwmImporter(String konvertierungTable, String fplanTable, String kvwmapUrl, String kvwmapLoginName, String kvwmapPassword) {
        super(konvertierungTable, fplanTable, FPlanImporter.Version.v5_1n, kvwmapUrl, kvwmapLoginName, kvwmapPassword);
    }
    
    
    Konvertierung createKonvertierung(FPlan plan, Gemeinde gemeinde) {
        Konvertierung konvertierung = new Konvertierung();
        konvertierung.stelle_id = gemeinde.stelle_id;
        konvertierung.status = KonvertierungStatus.erstellt;
        konvertierung.user_id = 1;
        konvertierung.gebietseinheiten = gemeinde.rs;
        konvertierung.bezeichnung = plan.getName()+" "+gemeinde.gmd_name+" "+plan.getNummer();
        konvertierung.veroeffentlicht = false;
        konvertierung.beschreibung = "automatically created from wfs-fietz";
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
        konvertierung.planart = "FP-Plan";
        konvertierung.epsg = Konvertierung.EPSGCodes.EPSG_25833;
        konvertierung.input_epsg = Konvertierung.EPSGCodes.EPSG_25833;
        konvertierung.output_epsg = Konvertierung.EPSGCodes.EPSG_25833;
        konvertierung.geom_precision =3;
        return konvertierung;
    }

    @Override    
    public void updateFPlaene(Connection conWrite, Connection conRead, ImportLogger importLogger, ImportConfigEntry entry, List<FPlan> fplans) throws SQLException  {

        
        System.err.println("FPlanNwmImporter.updateFPlaene");
        
        if (fplans != null) {
            conWrite.setAutoCommit(false);
            
            int countSucceded = 0;
            int countFailed = 0;
            int countSkipped = 0;
            int countNotValidated = 0;        
            
            HashMap<UUID, FPlan> lGmlIds = new HashMap<>();
            HashSet<UUID> lDoubleGmlIds = new HashSet<>();
            for (int i=0; i<fplans.size(); i++) {
                FPlan plan = fplans.get(i);
                if (lGmlIds.containsKey(plan.getGml_id())) {
                    System.err.println(plan);
                    System.err.println(lGmlIds.get(plan.getGml_id()));
                    lDoubleGmlIds.add(plan.getGml_id());
                    importLogger.addError("Die GmlId =\""+plan.getGml_id()+"\" ist mehrfach vorhanden. Pläne mit dieser GmlId werden nicht eingelesen.");
    
                }
                lGmlIds.put(plan.getGml_id(), plan);
            }
        
        
            GemeindeDAO gemeindeDAO = new GemeindeDAO(conRead);
            FPlanDAO fplanDao = new FPlanDAO(conWrite, conRead, fplanTable);
            KonvertierungDAO konvertierungDAO = new KonvertierungDAO(conWrite, konvertierungTable);
            
            for (int i=0; i<fplans.size(); i++) {
                FPlan plan = fplans.get(i);
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
                    String geomValidierungsResult = fplanDao.validateGeom(plan.getGeom());
                    if (!"Valid Geometry".equals(geomValidierungsResult)) {
                        logger.info("FPLanImporter: Plan gmlId=\""+plan.getGml_id()+"\" Geometry is not valid: "+ geomValidierungsResult +".");
                        throw new IllegalArgumentException("FPlanImporter: Plan gmlId=\""+plan.getGml_id()+"\" hat keine gültige Geometry: \""+geomValidierungsResult+"\".");
                    }
                    FPlan dbPlan = fplanDao.findById(plan.getGml_id());
                    Konvertierung konvertierung = null;
                    if (dbPlan == null) {
                        // neuer FPlan
                        konvertierung = createKonvertierung(plan, kvGemeinde);
                        Konvertierung dbKonvertierung = konvertierungDAO.insert(konvertierung);                    
                        plan.setKonvertierungId(dbKonvertierung.id);
                        fplanDao.insert(plan);
                        conWrite.commit();
                        importLogger.addLine(String.format("inserted %s", plan.getGml_id()));
                        
                        boolean succeded = validate(konvertierung, plan, kvwmapUrl, importLogger);
                        if (succeded) {
                            if (plan.veroeffentlichungsdatum!=null && succeded) {
                                konvertierungDAO.updatePublishDate(konvertierung.id, plan.veroeffentlichungsdatum);
                                conWrite.commit();
                            }
                            countSucceded++;
                        } else {
                            countNotValidated++;
                        }
                        logger.info("BPLanImpoter: Plan gmlId=\""+plan.getGml_id()+"\" inserted.");
                    } else {
                        // update FPlan                                
                        if (HasChangedFunctions.hasChanged(plan, dbPlan)) {
                            logger.debug("update plan");
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
                                        konvertierung = konvertierungDAO.insert(konvertierung);                    
                                        plan.setKonvertierungId(konvertierung.id);
                                        logger.debug("update konvertierung == null =>"+konvertierung.id);
                                    } else {
                                        plan.setKonvertierungId(dbPlan.getKonvertierungId());
                                    }
                                    fplanDao.update(plan);
                                    conWrite.commit();
                                    
                                    int updateCount = konvertierungDAO.update(plan.konvertierung_id);
                                    if (updateCount == 0) {
                                        throw new IllegalArgumentException("In der DB existiert ein FPlan mit der gmlId. Der zugehörige Eintrag in der Konvertierungs-Tabelle existiert aber nicht.");
                                    }
                                    konvertierungDAO.updatePublishFlag(plan.konvertierung_id, false);
                                    
                                    konvertierung = konvertierungDAO.find(plan.konvertierung_id); 
                                    
                                    logger.info("FPLanImpoter: Plan gmlId=\""+plan.getGml_id()+"\" updated.");
                                    importLogger.addLine(String.format("updated %s", plan.getGml_id()));
                                    conWrite.commit();
                                    
                                    boolean succeded = validate(konvertierung, plan, kvwmapUrl, importLogger);
                                    if (succeded) {
                                        if (plan.wirksamkeitsdatum!=null && succeded) {
                                            konvertierungDAO.updatePublishDate(konvertierung.id, plan.wirksamkeitsdatum);
                                        }                                        
                                        conWrite.commit();
                                        countSucceded++;
                                    } else {
                                        countNotValidated++;
                                    }
                         
                                } else {
                                    logger.info("FPLanImpoter: Plan gmlId=\""+plan.getGml_id()+"\" unchanged.");
                                    importLogger.addLine(String.format("unchanged %s", plan.getGml_id()));
                                    countSucceded++;
                                }
    
                            }
                        
                    
                } catch (Exception ex) {                    
                    try {
                        conWrite.rollback();
                    } 
                    catch (SQLException e) {
                        logger.error("rollback Error", e);
                    }
                    countFailed++;
                    importLogger.addError("error updating FPlan [gmlId="+ plan.getGml_id() +" name=\""+ plan.getName() +"\"] from service \"" + entry.bezeichnung + "\" with url=\"" + entry.onlineresource +"\" error:["+ex.getMessage()+"]");
                    logger.error("error updating FPlan [gmlId="+ plan.getGml_id() +" name=\""+ plan.getName() +"\"] from service \"" + entry.bezeichnung + "\" with url=\"" + entry.onlineresource +"\"", ex);
                } 
            }
            logger.info("count="+fplans.size()+" succeded="+countSucceded+ " failed="+countFailed+ " skipped="+countSkipped+" countNotValidated="+countNotValidated);
        }

    }
}
