package de.gdiservice.wfs;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.TimeZone;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;
import org.postgresql.util.PGtokenizer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import de.gdiservice.bplan.poi.CodeList;
import de.gdiservice.bplan.poi.SpezExterneRef;
import de.gdiservice.bplan.poi.FPlan;
import de.gdiservice.bplan.poi.Gemeinde;
import de.gdiservice.bplan.poi.PGSpezExterneReferenz;
import de.gdiservice.bplan.poi.PGSpezExterneReferenzAuslegung;
import de.gdiservice.bplan.poi.PlanaufstellendeGemeinde;

public class BFitzFPlanFactoryV5_1 implements WFSFactory<FPlan>  {

    boolean usePGExterneReferenzAuslegung = false;
    
    @SuppressWarnings("unused")
    private BFitzFPlanFactoryV5_1() {
        
    }

    public BFitzFPlanFactoryV5_1(boolean usePGExterneReferenzAuslegung) {
        this.usePGExterneReferenzAuslegung = usePGExterneReferenzAuslegung;
    }
    
    @Override
    public FPlan build(SimpleFeature f) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.getDateFormat().setTimeZone(TimeZone.getTimeZone("CEST"));
        ObjectReader objectReader = objectMapper.reader();
        

        final FPlan fplan = new FPlan();

        fplan.setInternalId(f.getID());        
        fplan.setName ( (String) f.getAttribute("name"));
        String gml_id = (String) f.getAttribute("gml_id");
        if (gml_id==null) {
            throw new IllegalArgumentException("gml_id is not set");
        }        
        fplan.setGml_id( (String) f.getAttribute("gml_id")); //"67db195e-9203-4856-9bc9-8ea491153652"
        fplan.setNummer( (String) f.getAttribute("nummer")); //"3.1, 1. Änderung"
        
        
        String sPlanart = GeolexBPlanFactory.getAsString(f, "planart");
        if (sPlanart == null) {
            throw new IllegalArgumentException("Planart darf nicht null sein");
        }
        sPlanart = PGtokenizer.removeBox(sPlanart);
        fplan.setPlanart( sPlanart); // "{10001}"
        
        fplan.setRechtsstand((String) f.getAttribute("rechtsstand")); //="4000"

        String sDate = (String) f.getAttribute("wirksamkeitsdatum");
        if (sDate!=null && sDate.trim().length()>0) {
            try {
                fplan.setWirksamkeitsdatum( LocalDate.parse(sDate));
            } catch (DateTimeParseException e) {
                throw new IOException("Konnt Datum nicht \""+sDate+"\" parsen");
            } //="2018-01-05"
        }
        
        String sAuslegungsstartdatum = GeolexBPlanFactory.getAsString(f, "auslegungsstartdatum");
        if (sAuslegungsstartdatum!=null && sAuslegungsstartdatum.trim().length()>0) {
            try {
                fplan.setAuslegungsstartdatum( GeolexBPlanFactory.parseLocalDateArr(sAuslegungsstartdatum));
            } catch (DateTimeParseException e) {
                throw new IOException("Konnte Auslegungssstartdatum nicht \""+sAuslegungsstartdatum+"\" parsen");
            }
        }        
        String sAuslegungsenddatum =  GeolexBPlanFactory.getAsString(f, "auslegungsenddatum");
        if (sAuslegungsenddatum!=null && sAuslegungsenddatum.trim().length()>0) {
            try {
                fplan.setAuslegungsenddatum(GeolexBPlanFactory.parseLocalDateArr(sAuslegungsenddatum));
            } catch (DateTimeParseException e) {
                throw new IOException("Konnte Auslegungsenddatum nicht \""+sAuslegungsenddatum+"\" parsen");
            }
        }

        String sGemeinde = (String) f.getAttribute("gemeinde");
        if (sGemeinde!=null) {
            Gemeinde[] gemeinden = null;
            if (sGemeinde.startsWith("[")) {
                gemeinden = objectReader.readValue(sGemeinde, Gemeinde[].class);
            } else {
                Gemeinde gemeinde = objectReader.readValue(sGemeinde, Gemeinde.class);
                gemeinden = new Gemeinde[] {gemeinde};
            }
            fplan.setGemeinde ( gemeinden ); // "{"ags" : "13072072", "rs" : "130725260072", "gemeindename" : "Mönchhagen", "ortsteilname" : "Mönchhagen"}"
        }

        Geometry geom = (Geometry)f.getDefaultGeometry();        
        if (geom instanceof Polygon) {
            final MultiPolygon mPolygon = new MultiPolygon(new Polygon[] {(Polygon)geom}, new GeometryFactory());
            mPolygon.setSRID(25833);
            fplan.setGeom(mPolygon);
        } else {
            if (geom instanceof MultiPolygon) {
                geom.setSRID(25833);
                fplan.setGeom((MultiPolygon)geom);
            }
        }
        
        String sExtenalRefs = (String) f.getAttribute("externereferenz");
        if (sExtenalRefs!=null && sExtenalRefs.length()>0) {
            SpezExterneRef[] extRefs;
            try {
                extRefs = objectReader.readValue(sExtenalRefs, SpezExterneRef[].class);
            } catch (IOException e) {
               throw new IllegalArgumentException("String \""+sExtenalRefs+"\" could not be parsed as an Array of externereferenz"); 
            }	
            if (extRefs != null && extRefs.length>0) {                
                PGSpezExterneReferenz[] pgRefs = usePGExterneReferenzAuslegung?  new PGSpezExterneReferenzAuslegung[extRefs.length] : new PGSpezExterneReferenz[extRefs.length];
                for (int i=0; i<extRefs.length; i++) {
                    String type = extRefs[i].getTyp();
                    if ("5000".equals(type) || "2900".equals(type) || "3100".equals(type)) {
                        extRefs[i].typ = "9999";      
                    }                    
                    pgRefs[i] = usePGExterneReferenzAuslegung ? new PGSpezExterneReferenzAuslegung(extRefs[i]) : new PGSpezExterneReferenz(extRefs[i]);
                }
                fplan.setExterneReferenzes(pgRefs);
                // bplan.externeReferenzes = extRefs; // ="[{"georefurl" : null, "georefmimetype" : null, "art" : "Dokument", "informationssystemurl" : "https://www.amt-rostocker-heide.de/amt-rostocker-heide/Geo-Daten-Amt-Rostocker-Heide/", "referenzname" : "amt_rostocker_heide_moenchhagen_bplan_3_1_1_1.pdf", "referenzurl" : "https://service.btfietz.de/wmsdata/amt_rostocker_heide/amt_rostocker_heide_moenchhagen_bplan_3_1_1_1.pdf", "referenzmimetype" : {"codespace" : "https://bauleitplaene-mv.de/codelist/XP_MimeTypes.xml", "id" : "application/pdf", "value" : "application/pdf"}, "beschreibung" : "Satzung über 1. Änderung des Bebauungsplans (438,49 KB)", "datum" : "2003-10-07", "typ" : "1060"}, {"georefurl" : null, "georefmimetype" : null, "art" : "Dokument", "informationssystemurl" : "https://www.amt-rostocker-heide.de/amt-rostocker-heide/Geo-Daten-Amt-Rostocker-Heide/", "referenzname" : "amt_rostocker_heide_moenchhagen_bplan_3_1_2_1.pdf", "referenzurl" : "https://service.btfietz.de/wmsdata/amt_rostocker_heide/amt_rostocker_heide_moenchhagen_bplan_3_1_2_1.pdf", "referenzmimetype" : {"codespace" : "https://bauleitplaene-mv.de/codelist/XP_MimeTypes.xml", "id" : "application/pdf", "value" : "application/pdf"}, "beschreibung" : "Satzung über 2. Änderung des Bebauungsplans (2,47 MB)", "datum" : "2012-04-03", "typ" : "1060"}, {"georefurl" : null, "georefmimetype" : null, "art" : "Dokument", "informationssystemurl" : "https://www.amt-rostocker-heide.de/amt-rostocker-heide/Geo-Daten-Amt-Rostocker-Heide/", "referenzname" : "amt_rostocker_heide_moenchhagen_bplan_3_1_3_1.pdf", "referenzurl" : "https://service.btfietz.de/wmsdata/amt_rostocker_heide/amt_rostocker_heide_moenchhagen_bplan_3_1_3_1.pdf", "referenzmimetype" : {"codespace" : "https://bauleitplaene-mv.de/codelist/XP_MimeTypes.xml", "id" : "application/pdf", "value" : "application/pdf"}, "beschreibung" : "Satzung über 3. Änderung des Bebauungsplans (2,3 MB)", "datum" : "2017-12-01", "typ" : "1060"}, {"georefurl" : null, "georefmimetype" : null, "art" : "Dokument", "informationssystemurl" : "https://www.amt-rostocker-heide.de/amt-rostocker-heide/Geo-Daten-Amt-Rostocker-Heide/", "referenzname" : "amt_rostocker_heide_moenchhagen_bplan_3_1_3_2.pdf", "referenzurl" : "https://service.btfietz.de/wmsdata/amt_rostocker_heide/amt_rostocker_heide_moenchhagen_bplan_3_1_3_2.pdf", "referenzmimetype" : {"codespace" : "https://bauleitplaene-mv.de/codelist/XP_MimeTypes.xml", "id" : "application/pdf", "value" : "application/pdf"}, "beschreibung" : "Begründung zur 3. Änderung des Bebauungsplans (186,38 KB)", "datum" : "2017-12-01", "typ" : "1010"}]"
            }
        }
        
        
        
        String sAufstellungsbeschlussDatum = (String) f.getAttribute("aufstellungsbeschlussDatum");
        if (sAufstellungsbeschlussDatum!=null && sAufstellungsbeschlussDatum.trim().length()>0) {
            try {
                fplan.setAufstellungsbeschlussDatum(  LocalDate.parse(sAufstellungsbeschlussDatum));
            } catch (DateTimeParseException e) {
                throw new IOException("Konnt AufstellungsbeschlussDatum nicht \""+sAufstellungsbeschlussDatum+"\" parsen");
            } //="2018-01-05"
        }
        
        String sEntwurfsbeschlussdatum = (String) f.getAttribute("entwurfsbeschlussdatum");
        if (sEntwurfsbeschlussdatum!=null && sEntwurfsbeschlussdatum.trim().length()>0) {
            try {
                fplan.setEntwurfsbeschlussdatum(  LocalDate.parse(sEntwurfsbeschlussdatum));
            } catch (DateTimeParseException e) {
                throw new IOException("Konnt Entwurfsbeschlussdatum nicht \""+sEntwurfsbeschlussdatum+"\" parsen");
            }
        }
        String sGenehmigungsdatum = (String) f.getAttribute("genehmigungsdatum");
        if (sGenehmigungsdatum!=null && sGenehmigungsdatum.trim().length()>0) {
            try {
                fplan.setEntwurfsbeschlussdatum(  LocalDate.parse(sGenehmigungsdatum));
            } catch (DateTimeParseException e) {
                throw new IOException("Konnt Genehmigungsdatum nicht \""+sGenehmigungsdatum+"\" parsen");
            }
        }
      String sPlanbeschlussdatum = (String) f.getAttribute("planbeschlussdatum");
      if (sPlanbeschlussdatum!=null && sPlanbeschlussdatum.trim().length()>0) {
          try {
              fplan.setEntwurfsbeschlussdatum(  LocalDate.parse(sPlanbeschlussdatum));
          } catch (DateTimeParseException e) {
              throw new IOException("Konnt Planbeschlussdatum nicht \""+sPlanbeschlussdatum+"\" parsen");
          }
      }        
       String sTechnherstelldatum = (String) f.getAttribute("technherstelldatum");
        if (sTechnherstelldatum!=null && sTechnherstelldatum.trim().length()>0) {
            try {
                fplan.setEntwurfsbeschlussdatum(  LocalDate.parse(sTechnherstelldatum));
            } catch (DateTimeParseException e) {
                throw new IOException("Konnt Technherstelldatum nicht \""+sTechnherstelldatum+"\" parsen");
            }
        }
        String sUntergangsdatum = (String) f.getAttribute("untergangsdatum");
      if (sUntergangsdatum!=null && sUntergangsdatum.trim().length()>0) {
          try {
              fplan.setEntwurfsbeschlussdatum(  LocalDate.parse(sUntergangsdatum));
          } catch (DateTimeParseException e) {
              throw new IOException("Konnt Untergangsdatum nicht \""+sUntergangsdatum+"\" parsen");
          }
      }
        String sVeroeffentlichungsdatum = (String) f.getAttribute("veroeffentlichungsdatum");
      if (sVeroeffentlichungsdatum!=null && sVeroeffentlichungsdatum.trim().length()>0) {
          try {
              fplan.setEntwurfsbeschlussdatum(  LocalDate.parse(sVeroeffentlichungsdatum));
          } catch (DateTimeParseException e) {
              throw new IOException("Konnt Veroeffentlichungsdatum nicht \""+sVeroeffentlichungsdatum+"\" parsen");
          }
      }
                
        
   
      String sPlanaufstellendegemeinde = GeolexBPlanFactory.getAsString(f, "planaufstellendegemeinde");
      if (sPlanaufstellendegemeinde != null) {
          // "{"ags" : "13072072", "rs" : "130725260072","gemeindename":"Mönchhagen", "ortsteilname":"Mönchhagen"}"
          if (sPlanaufstellendegemeinde.startsWith("[") && sPlanaufstellendegemeinde.endsWith("]")) {
              try {
                  PlanaufstellendeGemeinde[] gemeinden = objectReader.readValue(sPlanaufstellendegemeinde, PlanaufstellendeGemeinde[].class);
                  fplan.setPlanaufstellendeGemeinde(gemeinden); 
              } catch (MismatchedInputException ex) {
                  throw new IllegalArgumentException("Konnte den String \"" + sPlanaufstellendegemeinde + "\" für planaufstellendegemeinde nicht als Array interpretieren.", ex);
              } 
          } else {
              try {
                  PlanaufstellendeGemeinde gemeinde = objectReader.readValue(sPlanaufstellendegemeinde, PlanaufstellendeGemeinde.class);
                  fplan.setPlanaufstellendeGemeinde(new PlanaufstellendeGemeinde[] { gemeinde }); 
              } catch (MismatchedInputException ex) {
                  throw new IllegalArgumentException("Konnte den String \"" + sPlanaufstellendegemeinde + "\" für planaufstellendegemeinde nicht als Object interpretieren.", ex);
              }           
          }
      }
        
      CodeList status = GeolexBPlanFactory.getCodeList(objectReader, "xplan_gml.bp_status", f, "status");
      fplan.setStatus(status);        

      String sTraegerbeteiligungsStartDatum = GeolexBPlanFactory.getAsString(f, "traegerbeteiligungsstartdatum");
      if (sTraegerbeteiligungsStartDatum!=null && sTraegerbeteiligungsStartDatum.trim().length()>0) {
          try {
              fplan.setTraegerbeteiligungsstartdatum( GeolexBPlanFactory.parseLocalDateArr(sTraegerbeteiligungsStartDatum));
          } catch (DateTimeParseException e) {
              throw new IOException("Konnt TraegerbeteiligungsStartDatum nicht \""+sTraegerbeteiligungsStartDatum+"\" parsen");
          }
      }

      String sTraegerbeteiligungsEndDatum = GeolexBPlanFactory.getAsString(f, "traegerbeteiligungsenddatum");
      if (sTraegerbeteiligungsEndDatum!=null && sTraegerbeteiligungsEndDatum.trim().length()>0) {
         try {
              fplan.setTraegerbeteiligungsenddatum( GeolexBPlanFactory.parseLocalDateArr(sTraegerbeteiligungsEndDatum));
          } catch (DateTimeParseException e) {
              throw new IOException("Konnt TraegerbeteiligungsEndDatum nicht \""+sTraegerbeteiligungsEndDatum+"\" parsen");
          }
      }           

        
        
        return fplan;
    }



}
