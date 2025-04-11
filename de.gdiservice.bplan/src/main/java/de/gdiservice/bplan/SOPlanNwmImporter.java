package de.gdiservice.bplan;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import de.gdiservice.bplan.konvertierung.Gemeinde;
import de.gdiservice.bplan.konvertierung.GemeindeDAO;
import de.gdiservice.bplan.konvertierung.Konvertierung;
import de.gdiservice.bplan.konvertierung.Konvertierung.KonvertierungStatus;
import de.gdiservice.bplan.konvertierung.KonvertierungDAO;

public class SOPlanNwmImporter extends SOPlanImporter {

    public SOPlanNwmImporter(String konvertierungTable, String soplanTable, String kvwmapUrl, String kvwmapLoginName, String kvwmapPassword) {
        super(konvertierungTable, soplanTable, null, kvwmapUrl, kvwmapLoginName, kvwmapPassword);
    }
    
    
    Konvertierung createKonvertierung(SOPlan plan, Gemeinde gemeinde) {
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
            if (plan.getPlanart()!=null) {
                iPlanArt = Integer.parseInt(plan.getPlanart().value);
            } else {
                throw new IllegalArgumentException("WFS enthält keine Planart.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Planart für gmlId=\""+plan.getGml_id()+"\" \""+plan.getPlanart()+"\" ist nicht gültig.");
        }                    
        SOPlan.PlanArt planArt = SOPlan.PlanArt.get(iPlanArt);
        if (planArt==null) {
            throw new IllegalArgumentException("Planart für gmlId=\""+plan.getGml_id()+"\" \""+planArt+"\" ist im System nicht bekannt.");
        }
        konvertierung.planart = "SO-Plan";
        konvertierung.epsg = Konvertierung.EPSGCodes.EPSG_25833;
        konvertierung.input_epsg = Konvertierung.EPSGCodes.EPSG_25833;
        konvertierung.output_epsg = Konvertierung.EPSGCodes.EPSG_25833;
        konvertierung.geom_precision =3;
        return konvertierung;
    }

    
    public void updateSOPlaene(Connection conWrite, Connection conRead, ImportLogger importLogger, ImportConfigEntry entry, List<SOPlan> soPlans) throws SQLException  {

        
        System.err.println("SOPlanNwmImporter.updateSOPlaene");
        
        if (soPlans != null) {
            conWrite.setAutoCommit(false);
            
            int countSucceded = 0;
            int countFailed = 0;
            int countSkipped = 0;
            int countNotValidated = 0;        
            
            HashMap<UUID, SOPlan> lGmlIds = new HashMap<>();
            HashSet<UUID> lDoubleGmlIds = new HashSet<>();
            for (int i=0; i<soPlans.size(); i++) {
                SOPlan plan = soPlans.get(i);
                if (lGmlIds.containsKey(plan.getGml_id())) {
                    System.err.println(plan);
                    System.err.println(lGmlIds.get(plan.getGml_id()));
                    lDoubleGmlIds.add(plan.getGml_id());
                    importLogger.addError("Die GmlId =\""+plan.getGml_id()+"\" ist mehrfach vorhanden. Pläne mit dieser GmlId werden nicht eingelesen.");
    
                }
                lGmlIds.put(plan.getGml_id(), plan);
            }
        
        
            GemeindeDAO gemeindeDAO = new GemeindeDAO(conRead);
            SOPlanDAO soplanDao = new SOPlanDAO(conWrite, conRead, soplanTable);
            KonvertierungDAO konvertierungDAO = new KonvertierungDAO(conWrite, konvertierungTable);
            
            for (int i=0; i<soPlans.size(); i++) {
                SOPlan plan = soPlans.get(i);
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
                    String geomValidierungsResult = soplanDao.validateGeom(plan.getGeom());
                    if (!"Valid Geometry".equals(geomValidierungsResult)) {
                        logger.info("SOPLanImporter: Plan gmlId=\""+plan.getGml_id()+"\" Geometry is not valid: "+ geomValidierungsResult +".");
                        throw new IllegalArgumentException("SOPlanImporter: Plan gmlId=\""+plan.getGml_id()+"\" hat keine gültige Geometry: \""+geomValidierungsResult+"\".");
                    }
                    SOPlan dbPlan = soplanDao.findById(plan.getGml_id());
                    Konvertierung konvertierung = null;
                    if (dbPlan == null) {
                        // neuer SOPlan
                        konvertierung = createKonvertierung(plan, kvGemeinde);
                        Konvertierung dbKonvertierung = konvertierungDAO.insert(konvertierung);                    
                        plan.setKonvertierungId(dbKonvertierung.id);
                        soplanDao.insert(plan);
                        conWrite.commit();
                        boolean succeded = validate(konvertierung, plan, kvwmapUrl, importLogger);   

                        if (succeded) {
                            if (plan.genehmigungsdatum!=null) {
                                konvertierungDAO.updatePublishDate(konvertierung.id, new Timestamp(plan.genehmigungsdatum.getTime()));
                                conWrite.commit();
                            }
                            countSucceded++;
                        } else {
                            countNotValidated++;
                        }
                        logger.info("BPLanImpoter: Plan gmlId=\""+plan.getGml_id()+"\" inserted.");
                        importLogger.addLine(String.format("inserted %s", plan.getGml_id()));
                    } else {
                        // update SOPlan                                
                        if (SOPlanImporter.hasChanged(plan, dbPlan)) {
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
                                    soplanDao.update(plan);
                                    conWrite.commit();
                                    
                                    int updateCount = konvertierungDAO.update(plan.konvertierung_id);
                                    if (updateCount == 0) {
                                        throw new IllegalArgumentException("In der DB existiert ein SOPlan mit der gmlId. Der zugehörige Eintrag in der Konvertierungs-Tabelle existiert aber nicht.");
                                    }
                                    konvertierungDAO.updatePublishFlag(plan.konvertierung_id, false);
                                    
                                    konvertierung = konvertierungDAO.find(plan.konvertierung_id); 
                                    
                                    logger.info("SOPLanImpoter: Plan gmlId=\""+plan.getGml_id()+"\" updated.");
                                    importLogger.addLine(String.format("updated %s", plan.getGml_id()));
                                    conWrite.commit();
                                    
                                    boolean succeded = validate(konvertierung, plan, kvwmapUrl, importLogger);
                                    if (succeded) {
                                        if (plan.genehmigungsdatum!=null) {
                                            konvertierungDAO.updatePublishDate(konvertierung.id, new Timestamp(plan.genehmigungsdatum.getTime()));
                                        }
                                        conWrite.commit();
                                        countSucceded++;
                                    } else {
                                        countNotValidated++;
                                    }
                                    
                                } else {
                                    logger.info("SOPLanImpoter: Plan gmlId=\""+plan.getGml_id()+"\" unchanged.");
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
                    importLogger.addError("error updating SOPlan [gmlId="+ plan.gml_id +" name=\""+ plan.name +"\"] from service \"" + entry.bezeichnung + "\" with url=\"" + entry.onlineresource +"\" error:["+ex.getMessage()+"]");
                    logger.error("error updating SOPlan [gmlId="+ plan.gml_id +" name=\""+ plan.name +"\"] from service \"" + entry.bezeichnung + "\" with url=\"" + entry.onlineresource +"\"", ex);
                } 
            }
            logger.info("count="+soPlans.size()+" succeded="+countSucceded+ " failed="+countFailed+ " skipped="+countSkipped+" countNotValidated="+countNotValidated);
        }

    }
}
