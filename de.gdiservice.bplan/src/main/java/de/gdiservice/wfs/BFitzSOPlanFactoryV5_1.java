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
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import de.gdiservice.bplan.poi.Gemeinde;
import de.gdiservice.bplan.poi.PGSpezExterneReferenz;
import de.gdiservice.bplan.poi.PGSpezExterneReferenzAuslegung;
import de.gdiservice.bplan.poi.PGVerbundenerPlan;
import de.gdiservice.bplan.poi.PG_SO_Planart;
import de.gdiservice.bplan.poi.PlanaufstellendeGemeinde;
import de.gdiservice.bplan.poi.SOPlan;
import de.gdiservice.bplan.poi.SpezExterneRef;
import de.gdiservice.bplan.poi.VerbundenerPlan;

public class BFitzSOPlanFactoryV5_1 implements WFSFactory<SOPlan>  {

    boolean usePGExterneReferenzAuslegung = false;
    
    @SuppressWarnings("unused")
    private BFitzSOPlanFactoryV5_1() {
        
    }

    public BFitzSOPlanFactoryV5_1(boolean usePGExterneReferenzAuslegung) {
        this.usePGExterneReferenzAuslegung = usePGExterneReferenzAuslegung;
    }
    
    @Override
    public SOPlan build(SimpleFeature f) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.getDateFormat().setTimeZone(TimeZone.getTimeZone("CEST"));
        ObjectReader objectReader = objectMapper.reader();
        

        final SOPlan soplan = new SOPlan();

//        soplan.setInternalId(f.getID());        
        soplan.setName ( (String) f.getAttribute("name"));
        String gml_id = (String) f.getAttribute("gml_id");
        if (gml_id==null) {
            throw new IllegalArgumentException("gml_id is not set");
        }        
        soplan.setGml_id( (String) f.getAttribute("gml_id")); //"67db195e-9203-4856-9bc9-8ea491153652"
        soplan.setNummer( (String) f.getAttribute("nummer")); //"3.1, 1. Änderung"
        soplan.setPlanart( getPlanArt((String) f.getAttribute("planart"))); // "{10001}"
//        fplan.setRechtsstand((String) f.getAttribute("rechtsstand")); //="4000"

        String sDate = (String) f.getAttribute("genehmigungsdatum");
        if (sDate!=null && sDate.trim().length()>0) {
            try {
                soplan.setGenehmigungsdatum(LocalDate.parse(sDate));
            } catch (DateTimeParseException e) {
                throw new IOException("Konnt Datum nicht \""+sDate+"\" parsen");
            } //="2018-01-05"
        }
        
        String sAuslegungsstartdatum = (String) f.getAttribute("auslegungsstartdatum");
        if (sAuslegungsstartdatum!=null && sAuslegungsstartdatum.trim().length()>0) {
            try {
                soplan.setAuslegungsstartdatum( LocalDate.parse(sAuslegungsstartdatum));
            } catch (DateTimeParseException e) {
                throw new IOException("Konnt Auslegungssstartdatum nicht \""+sAuslegungsstartdatum+"\" parsen");
            }
        }        
        String sAuslegungsenddatum = (String) f.getAttribute("auslegungsenddatum");
        if (sAuslegungsenddatum!=null && sAuslegungsenddatum.trim().length()>0) {
            try {
                soplan.setAuslegungsenddatum( LocalDate.parse(sAuslegungsenddatum));
            } catch (DateTimeParseException e) {
                throw new IOException("Konnt Auslegungsenddatum nicht \""+sAuslegungsenddatum+"\" parsen");
            }
        }

        String sGemeinde = (String) f.getAttribute("gemeinde");
        if (sGemeinde!=null ) {
            Gemeinde[] gemeinden = null;
            if (sGemeinde.startsWith("[")) {
                gemeinden = objectReader.readValue(sGemeinde, Gemeinde[].class);
            } else {
                Gemeinde gemeinde = objectReader.readValue(sGemeinde, Gemeinde.class);
                gemeinden = new Gemeinde[] {gemeinde};
            }
            soplan.setGemeinde ( gemeinden ); // "{"ags" : "13072072", "rs" : "130725260072", "gemeindename" : "Mönchhagen", "ortsteilname" : "Mönchhagen"}"
        }

        Geometry geom = (Geometry)f.getDefaultGeometry();        
        if (geom instanceof Polygon) {
            final MultiPolygon mPolygon = new MultiPolygon(new Polygon[] {(Polygon)geom}, new GeometryFactory());
            mPolygon.setSRID(25833);
            soplan.setGeom(mPolygon);
        } else {
            if (geom instanceof MultiPolygon) {
                geom.setSRID(25833);
                soplan.setGeom((MultiPolygon)geom);
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
                soplan.setExterneReferenzes(pgRefs);
                // bplan.externeReferenzes = extRefs; // ="[{"georefurl" : null, "georefmimetype" : null, "art" : "Dokument", "informationssystemurl" : "https://www.amt-rostocker-heide.de/amt-rostocker-heide/Geo-Daten-Amt-Rostocker-Heide/", "referenzname" : "amt_rostocker_heide_moenchhagen_bplan_3_1_1_1.pdf", "referenzurl" : "https://service.btfietz.de/wmsdata/amt_rostocker_heide/amt_rostocker_heide_moenchhagen_bplan_3_1_1_1.pdf", "referenzmimetype" : {"codespace" : "https://bauleitplaene-mv.de/codelist/XP_MimeTypes.xml", "id" : "application/pdf", "value" : "application/pdf"}, "beschreibung" : "Satzung über 1. Änderung des Bebauungsplans (438,49 KB)", "datum" : "2003-10-07", "typ" : "1060"}, {"georefurl" : null, "georefmimetype" : null, "art" : "Dokument", "informationssystemurl" : "https://www.amt-rostocker-heide.de/amt-rostocker-heide/Geo-Daten-Amt-Rostocker-Heide/", "referenzname" : "amt_rostocker_heide_moenchhagen_bplan_3_1_2_1.pdf", "referenzurl" : "https://service.btfietz.de/wmsdata/amt_rostocker_heide/amt_rostocker_heide_moenchhagen_bplan_3_1_2_1.pdf", "referenzmimetype" : {"codespace" : "https://bauleitplaene-mv.de/codelist/XP_MimeTypes.xml", "id" : "application/pdf", "value" : "application/pdf"}, "beschreibung" : "Satzung über 2. Änderung des Bebauungsplans (2,47 MB)", "datum" : "2012-04-03", "typ" : "1060"}, {"georefurl" : null, "georefmimetype" : null, "art" : "Dokument", "informationssystemurl" : "https://www.amt-rostocker-heide.de/amt-rostocker-heide/Geo-Daten-Amt-Rostocker-Heide/", "referenzname" : "amt_rostocker_heide_moenchhagen_bplan_3_1_3_1.pdf", "referenzurl" : "https://service.btfietz.de/wmsdata/amt_rostocker_heide/amt_rostocker_heide_moenchhagen_bplan_3_1_3_1.pdf", "referenzmimetype" : {"codespace" : "https://bauleitplaene-mv.de/codelist/XP_MimeTypes.xml", "id" : "application/pdf", "value" : "application/pdf"}, "beschreibung" : "Satzung über 3. Änderung des Bebauungsplans (2,3 MB)", "datum" : "2017-12-01", "typ" : "1060"}, {"georefurl" : null, "georefmimetype" : null, "art" : "Dokument", "informationssystemurl" : "https://www.amt-rostocker-heide.de/amt-rostocker-heide/Geo-Daten-Amt-Rostocker-Heide/", "referenzname" : "amt_rostocker_heide_moenchhagen_bplan_3_1_3_2.pdf", "referenzurl" : "https://service.btfietz.de/wmsdata/amt_rostocker_heide/amt_rostocker_heide_moenchhagen_bplan_3_1_3_2.pdf", "referenzmimetype" : {"codespace" : "https://bauleitplaene-mv.de/codelist/XP_MimeTypes.xml", "id" : "application/pdf", "value" : "application/pdf"}, "beschreibung" : "Begründung zur 3. Änderung des Bebauungsplans (186,38 KB)", "datum" : "2017-12-01", "typ" : "1010"}]"
            }
            
            
            String sUntergangsdatum = GeolexBPlanFactory.getAsString(f, "untergangsdatum");
            if (sUntergangsdatum!=null && sUntergangsdatum.trim().length()>0) {
                
                sUntergangsdatum = PGtokenizer.removeCurlyBrace(sUntergangsdatum);
                try {
                    soplan.setUntergangsdatum( LocalDate.parse(sUntergangsdatum));
                } catch (DateTimeParseException e) {
                    throw new IOException("Konnt Untergangsdatum nicht \""+sUntergangsdatum+"\" parsen");
                }
            } 
            String sTechnHerstellDatum = GeolexBPlanFactory.getAsString(f, "technherstelldatum");
            if (sTechnHerstellDatum!=null && sTechnHerstellDatum.trim().length()>0) {
                
                sTechnHerstellDatum = PGtokenizer.removeCurlyBrace(sTechnHerstellDatum);
                try {
                    soplan.setTechnHerstellDatum( LocalDate.parse(sTechnHerstellDatum));
                } catch (DateTimeParseException e) {
                    throw new IOException("Konnt TechnHerstellDatum nicht \""+sTechnHerstellDatum+"\" parsen");
                }
            } 
            
            
            
            String sPlanaufstellendegemeinde = GeolexBPlanFactory.getAsString(f, "planaufstellendegemeinde");
            if (sPlanaufstellendegemeinde != null) {
                // "{"ags" : "13072072", "rs" : "130725260072","gemeindename":"Mönchhagen", "ortsteilname":"Mönchhagen"}"
                if (sPlanaufstellendegemeinde.startsWith("[") && sPlanaufstellendegemeinde.endsWith("]")) {
                    try {
                        PlanaufstellendeGemeinde[] gemeinden = objectReader.readValue(sPlanaufstellendegemeinde, PlanaufstellendeGemeinde[].class);
                        soplan.setPlanaufstellendegemeinde(gemeinden); 
                    } catch (MismatchedInputException ex) {
                        throw new IllegalArgumentException("Konnte den String \"" + sPlanaufstellendegemeinde + "\" für planaufstellendegemeinde nicht als Array interpretieren.", ex);
                    } 
                } else {
                    try {
                        PlanaufstellendeGemeinde gemeinde = objectReader.readValue(sPlanaufstellendegemeinde, PlanaufstellendeGemeinde.class);
                        soplan.setPlanaufstellendegemeinde ( new PlanaufstellendeGemeinde[] {gemeinde} );
                    } catch (MismatchedInputException ex) {
                        throw new IllegalArgumentException("Konnte den String \"" + sPlanaufstellendegemeinde + "\" für planaufstellendegemeinde nicht als Object interpretieren.", ex);
                    }           
                }
            }            
            
            String sVeroeffentlichungsDatum = GeolexBPlanFactory.getAsString(f, "veroeffentlichungsdatum");
            if (sVeroeffentlichungsDatum!=null && sVeroeffentlichungsDatum.trim().length()>0) {
                
                sVeroeffentlichungsDatum = PGtokenizer.removeCurlyBrace(sVeroeffentlichungsDatum);
                try {
                    soplan.setVeroeffentlichungsDatum( LocalDate.parse(sVeroeffentlichungsDatum));
                } catch (DateTimeParseException e) {
                    throw new IOException("Konnt Veroeffentlichungsdatumnicht \""+sVeroeffentlichungsDatum+"\" parsen");
                }
            }
            
        }
        
//      aendert = {"rechtscharakter":"1100","verbundenerplan":"96cedb90-4910-11ec-aace-17ecec70de25"}        
      String sAendert = GeolexBPlanFactory.getAsString(f, "aendert");
      if (sAendert!=null) {
          VerbundenerPlan[] geaenderterPlAN = objectReader.readValue(sAendert, VerbundenerPlan[].class);      
          PGVerbundenerPlan[] arr = new PGVerbundenerPlan[geaenderterPlAN.length];
          for (int i=0; i<geaenderterPlAN.length; i++) {
              arr[i] = new PGVerbundenerPlan(geaenderterPlAN[i]);
          }
          soplan.setAendert( arr ); // "{"ags" : "13072072", "rs" : "130725260072", "gemeindename" : "Mönchhagen", "ortsteilname" : "Mönchhagen"}"
      }
      String sWurdegeaendertvon = GeolexBPlanFactory.getAsString(f, "wurdegeaendertvon");
//      System.err.println("!!!sWurdegeaendertvon="+sWurdegeaendertvon);
      if (sWurdegeaendertvon!=null) {
          VerbundenerPlan[] geaenderterPlAN = objectReader.readValue(sWurdegeaendertvon, VerbundenerPlan[].class);      
          PGVerbundenerPlan[] arr = new PGVerbundenerPlan[geaenderterPlAN.length];
          for (int i=0; i<geaenderterPlAN.length; i++) {
              arr[i] = new PGVerbundenerPlan(geaenderterPlAN[i]);
          }  
          soplan.setWurdeGeaendertVon( arr ); // "{"ags" : "13072072", "rs" : "130725260072", "gemeindename" : "Mönchhagen", "ortsteilname" : "Mönchhagen"}"
      }
      

      String sInternalid = GeolexBPlanFactory.getAsString(f, "internalid");
      soplan.setInternalId(sInternalid);
        return soplan;
    }



    private PG_SO_Planart getPlanArt(String s) {
        if (s!=null) {
            s = PGtokenizer.removeCurlyBrace(s);
            s = PGtokenizer.removeBox(s);
            String[] sArray = s.split(",");
            if (sArray.length==1) { 
                return new PG_SO_Planart(sArray[0]);
            } else if (sArray.length>1) {
                throw new RuntimeException("mehr als eine Planart");
            }
        }
        return null;
    }

}
