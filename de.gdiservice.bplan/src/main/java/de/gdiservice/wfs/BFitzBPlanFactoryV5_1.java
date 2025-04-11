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
//import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.postgresql.util.PGtokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import de.gdiservice.bplan.poi.BPlan;
import de.gdiservice.bplan.poi.ExterneRefAuslegung;
import de.gdiservice.bplan.poi.Gemeinde;
import de.gdiservice.bplan.poi.PGSpezExterneReferenz;
import de.gdiservice.bplan.poi.PGSpezExterneReferenzAuslegung;

public class BFitzBPlanFactoryV5_1 implements WFSFactory<BPlan>  {
    
    final static Logger logger = LoggerFactory.getLogger(BFitzBPlanFactoryV5_1.class);
    
    boolean usePGExterneReferenzAuslegung = false;
    
    @SuppressWarnings("unused")
    private BFitzBPlanFactoryV5_1() {
        
    }

    public BFitzBPlanFactoryV5_1(boolean usePGExterneReferenzAuslegung) {
        this.usePGExterneReferenzAuslegung = usePGExterneReferenzAuslegung;
    }

    @Override
    public BPlan build(SimpleFeature f) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.getDateFormat().setTimeZone(TimeZone.getTimeZone("CEST"));
        ObjectReader objectReader = objectMapper.reader();
        

        final BPlan bplan = new BPlan();

        bplan.setId(f.getID());        
        bplan.setName ( (String) f.getAttribute("name"));
        String gml_id = (String) f.getAttribute("gml_id");
        if (gml_id==null) {
            throw new IllegalArgumentException("gml_id is not set");
        }        
        bplan.setGml_id( (String) f.getAttribute("gml_id")); //"67db195e-9203-4856-9bc9-8ea491153652"
        bplan.setNummer( (String) f.getAttribute("nummer")); //"3.1, 1. Änderung"
        bplan.setPlanart( getPlanArten((String)f.getAttribute("planart"))); // "{10001}"
        bplan.setRechtsstand((String) f.getAttribute("rechtsstand")); //="4000"

        String sDate = (String) f.getAttribute("inkrafttretensdatum");
        if (sDate!=null && sDate.trim().length()>0) {
            try {
                bplan.setInkrafttretensdatum(  LocalDate.parse(sDate));
            } catch (DateTimeParseException e) {
                throw new IOException("Konnt Inkrafttretensdatum nicht \""+sDate+"\" parsen");
            } //="2018-01-05"
        }
        
        String sAuslegungsstartdatum = (String) f.getAttribute("auslegungsstartdatum");
        if (sAuslegungsstartdatum!=null && sAuslegungsstartdatum.trim().length()>0) {
            try {
                bplan.setAuslegungsstartdatum( LocalDate.parse(sAuslegungsstartdatum));
            } catch (DateTimeParseException e) {
                throw new IOException("Konnt Auslegungssstartdatum nicht \""+sAuslegungsstartdatum+"\" parsen");
            }
        }        
        String sAuslegungsenddatum = (String) f.getAttribute("auslegungsenddatum");
        if (sAuslegungsenddatum!=null && sAuslegungsenddatum.trim().length()>0) {
            try {
                bplan.setAuslegungsenddatum( LocalDate.parse(sAuslegungsenddatum));
            } catch (DateTimeParseException e) {
                throw new IOException("Konnt Auslegungsenddatum nicht \""+sAuslegungsenddatum+"\" parsen");
            }
        }

        String sGemeinde = (String) f.getAttribute("gemeinde");
        if (sGemeinde!=null) {
            Gemeinde gemeinde = objectReader.readValue(sGemeinde, Gemeinde.class);
            logger.info("gemeinde="+gemeinde);
            bplan.setGemeinde ( new Gemeinde[] {gemeinde} ); // "{"ags" : "13072072", "rs" : "130725260072", "gemeindename" : "Mönchhagen", "ortsteilname" : "Mönchhagen"}"
        }

        Geometry geom = (Geometry)f.getDefaultGeometry();        
        if (geom instanceof Polygon) {
            final MultiPolygon mPolygon = new MultiPolygon(new Polygon[] {(Polygon)geom}, new GeometryFactory());
            mPolygon.setSRID(25833);
            bplan.setGeom(mPolygon);
        } else {
            if (geom instanceof MultiPolygon) {
                geom.setSRID(25833);
                bplan.setGeom((MultiPolygon)geom);
            }
        }
        
        String sExtenalRefs = (String) f.getAttribute("externereferenz");
        if (sExtenalRefs!=null) {
            ExterneRefAuslegung[] extRefs;
            try {
                extRefs = objectReader.readValue(sExtenalRefs, ExterneRefAuslegung[].class);
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
                bplan.setExterneReferenzes(pgRefs);
                // bplan.externeReferenzes = extRefs; // ="[{"georefurl" : null, "georefmimetype" : null, "art" : "Dokument", "informationssystemurl" : "https://www.amt-rostocker-heide.de/amt-rostocker-heide/Geo-Daten-Amt-Rostocker-Heide/", "referenzname" : "amt_rostocker_heide_moenchhagen_bplan_3_1_1_1.pdf", "referenzurl" : "https://service.btfietz.de/wmsdata/amt_rostocker_heide/amt_rostocker_heide_moenchhagen_bplan_3_1_1_1.pdf", "referenzmimetype" : {"codespace" : "https://bauleitplaene-mv.de/codelist/XP_MimeTypes.xml", "id" : "application/pdf", "value" : "application/pdf"}, "beschreibung" : "Satzung über 1. Änderung des Bebauungsplans (438,49 KB)", "datum" : "2003-10-07", "typ" : "1060"}, {"georefurl" : null, "georefmimetype" : null, "art" : "Dokument", "informationssystemurl" : "https://www.amt-rostocker-heide.de/amt-rostocker-heide/Geo-Daten-Amt-Rostocker-Heide/", "referenzname" : "amt_rostocker_heide_moenchhagen_bplan_3_1_2_1.pdf", "referenzurl" : "https://service.btfietz.de/wmsdata/amt_rostocker_heide/amt_rostocker_heide_moenchhagen_bplan_3_1_2_1.pdf", "referenzmimetype" : {"codespace" : "https://bauleitplaene-mv.de/codelist/XP_MimeTypes.xml", "id" : "application/pdf", "value" : "application/pdf"}, "beschreibung" : "Satzung über 2. Änderung des Bebauungsplans (2,47 MB)", "datum" : "2012-04-03", "typ" : "1060"}, {"georefurl" : null, "georefmimetype" : null, "art" : "Dokument", "informationssystemurl" : "https://www.amt-rostocker-heide.de/amt-rostocker-heide/Geo-Daten-Amt-Rostocker-Heide/", "referenzname" : "amt_rostocker_heide_moenchhagen_bplan_3_1_3_1.pdf", "referenzurl" : "https://service.btfietz.de/wmsdata/amt_rostocker_heide/amt_rostocker_heide_moenchhagen_bplan_3_1_3_1.pdf", "referenzmimetype" : {"codespace" : "https://bauleitplaene-mv.de/codelist/XP_MimeTypes.xml", "id" : "application/pdf", "value" : "application/pdf"}, "beschreibung" : "Satzung über 3. Änderung des Bebauungsplans (2,3 MB)", "datum" : "2017-12-01", "typ" : "1060"}, {"georefurl" : null, "georefmimetype" : null, "art" : "Dokument", "informationssystemurl" : "https://www.amt-rostocker-heide.de/amt-rostocker-heide/Geo-Daten-Amt-Rostocker-Heide/", "referenzname" : "amt_rostocker_heide_moenchhagen_bplan_3_1_3_2.pdf", "referenzurl" : "https://service.btfietz.de/wmsdata/amt_rostocker_heide/amt_rostocker_heide_moenchhagen_bplan_3_1_3_2.pdf", "referenzmimetype" : {"codespace" : "https://bauleitplaene-mv.de/codelist/XP_MimeTypes.xml", "id" : "application/pdf", "value" : "application/pdf"}, "beschreibung" : "Begründung zur 3. Änderung des Bebauungsplans (186,38 KB)", "datum" : "2017-12-01", "typ" : "1010"}]"
            }
        }
        return bplan;
    }



    
    

    private String[] getPlanArten(String s) {
        if (s!=null) {
            s = PGtokenizer.removeCurlyBrace(s);
            return s.split(",");
        }
        return null;
    }

}
