package de.gdiservice.wfs;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.postgresql.util.PGtokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import de.gdiservice.bplan.poi.BPlan;
import de.gdiservice.bplan.poi.CodeList;
import de.gdiservice.bplan.poi.ExterneRefAuslegung;
import de.gdiservice.bplan.poi.Gemeinde;
import de.gdiservice.bplan.poi.PGSpezExterneReferenz;
import de.gdiservice.bplan.poi.PGSpezExterneReferenzAuslegung;
import de.gdiservice.bplan.poi.PGVerbundenerPlan;
import de.gdiservice.bplan.poi.PlanaufstellendeGemeinde;
import de.gdiservice.bplan.poi.VerbundenerPlan;

public class GeolexBPlanFactory implements WFSFactory<BPlan>  {
    
    final static Logger logger = LoggerFactory.getLogger(GeolexBPlanFactory.class);
    
    boolean usePGExterneReferenzAuslegung = false;
    
    @SuppressWarnings("unused")
    private GeolexBPlanFactory() {
        
    }

    public GeolexBPlanFactory(boolean usePGExterneReferenzAuslegung) {
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
        if (gml_id==null || gml_id.length()==0) {
            bplan.setGml_id(f.getID()); //"67db195e-9203-4856-9bc9-8ea491153652"
        } else {
            bplan.setGml_id( (String) f.getAttribute("gml_id")); //"67db195e-9203-4856-9bc9-8ea491153652"
        }
//        System.err.println("fddfdfd "+feature.getProperty("gemeinde").getValue());
        


        
//        getAsString(f, "rechtsstand"); ok
//        getAsString(f, "rechtsverordnungsdatum"); ok
//        getAsString(f, "geometryProperty");
//        getAsString(f, "ausfertigungsdatum"); ok
//        getAsString(f, "description");
//        getAsString(f, "erschliessungsvertrag"); ok
//        getAsString(f, "nummer"); ok
//        getAsString(f, "auslegungsstartdatum"); ok
//        getAsString(f, "veroeffentlichungsdatum"); ok
//        getAsString(f, "traegerbeteiligungsstartdatum"); ok
//        getAsString(f, "planart"); ok
//        getAsString(f, "gruenordnungsplan"); ok
//        getAsString(f, "verlaengerungveraenderungssperre"); ok
//        getAsString(f, "traegerbeteiligungsenddatum"); ok
//        getAsString(f, "veraenderungssperrebeschlussdatum"); ok
//        getAsString(f, "aendert"); ok
//        getAsString(f, "veraenderungssperredatum"); ok
//        getAsString(f, "veraenderungssperreenddatum"); ok
//        getAsString(f, "satzungsbeschlussdatum");
//        getAsString(f, "wurdegeaendertvon"); ok
//        getAsString(f, "externereferenz"); ok
//        getAsString(f, "boundedBy");
//        getAsString(f, "genehmigungsdatum"); ok
//        getAsString(f, "staedtebaulichervertrag"); ok
//        getAsString(f, "aufstellungsbeschlussdatum"); ok
//        getAsString(f, "verfahren"); ok
//        getAsString(f, "internalid");
//        getAsString(f, "durchfuehrungsvertrag"); ok
//        getAsString(f, "gemeinde"); ok
//        getAsString(f, "name"); ok
//        getAsString(f, "auslegungsenddatum"); ok
//        getAsString(f, "planaufstellendegemeinde");
//        getAsString(f, "untergangsdatum"); ok
//        getAsString(f, "inkrafttretensdatum"); ok
//        getAsString(f, "status"); ok
        
//        if (1==1) {
//            return null;
//        }
        
        bplan.setNummer( getAsString(f, "nummer")); //"3.1, 1. Änderung"
        bplan.setPlanart( getEnumValues((String)f.getAttribute("planart"))); // "{10001}"

        bplan.setRechtsstand(getAsString(f, "rechtsstand")); //="4000"
        bplan.setVerfahren(getAsString(f, "verfahren")); //="4000"
        
//        aendert = {"rechtscharakter":"1100","verbundenerplan":"96cedb90-4910-11ec-aace-17ecec70de25"}        
        String sAendert = getAsString(f, "aendert");
        if (sAendert!=null) {
            VerbundenerPlan[] geaenderterPlAN = objectReader.readValue(sAendert, VerbundenerPlan[].class);      
            PGVerbundenerPlan[] arr = new PGVerbundenerPlan[geaenderterPlAN.length];
            for (int i=0; i<geaenderterPlAN.length; i++) {
                arr[i] = new PGVerbundenerPlan(geaenderterPlAN[i]);
            }
            bplan.setAendert( arr ); // "{"ags" : "13072072", "rs" : "130725260072", "gemeindename" : "Mönchhagen", "ortsteilname" : "Mönchhagen"}"
        }
        String sWurdegeaendertvon = getAsString(f, "wurdegeaendertvon");
//        System.err.println("!!!sWurdegeaendertvon="+sWurdegeaendertvon);
        if (sWurdegeaendertvon!=null) {
            VerbundenerPlan[] geaenderterPlAN = objectReader.readValue(sWurdegeaendertvon, VerbundenerPlan[].class);      
            PGVerbundenerPlan[] arr = new PGVerbundenerPlan[geaenderterPlAN.length];
            for (int i=0; i<geaenderterPlAN.length; i++) {
                arr[i] = new PGVerbundenerPlan(geaenderterPlAN[i]);
            }  
            bplan.setWurdeGeaendertVon( arr ); // "{"ags" : "13072072", "rs" : "130725260072", "gemeindename" : "Mönchhagen", "ortsteilname" : "Mönchhagen"}"
        }
        

        String sInternalid = getAsString(f, "internalid");
        bplan.setInternalId(sInternalid);
        
        String sInkrafttretensdatum = getAsString(f, "inkrafttretensdatum");
        if (sInkrafttretensdatum!=null && sInkrafttretensdatum.trim().length()>0) {
            sInkrafttretensdatum = PGtokenizer.removeCurlyBrace(sInkrafttretensdatum);
            try {
//                bplan.setInkrafttretensdatum( (new SimpleDateFormat("yyyy-MM-dd")).parse(sInkrafttretensdatum));
                bplan.setInkrafttretensdatum( LocalDate.parse(sInkrafttretensdatum));
            } catch (Exception e) {
                throw new IOException("Konnt Inkrafttretensdatum nicht \""+sInkrafttretensdatum+"\" parsen");
            } //="2018-01-05"
        }        
        
        String sRechtsverordnungsdatum = getAsString(f, "rechtsverordnungsdatum");
        if (sRechtsverordnungsdatum!=null && sRechtsverordnungsdatum.trim().length()>0) {
            sRechtsverordnungsdatum = PGtokenizer.removeCurlyBrace(sRechtsverordnungsdatum);
            try {
                bplan.setRechtsverordnungsdatum( LocalDate.parse(sRechtsverordnungsdatum));
            } catch (DateTimeParseException e) {
                throw new IOException("Konnt Inkrafttretensdatum nicht \""+sRechtsverordnungsdatum+"\" parsen");
            } //="2018-01-05"
        }
        
        String sAusfertigungsdatum = getAsString(f, "ausfertigungsdatum");
        if (sAusfertigungsdatum!=null && sAusfertigungsdatum.trim().length()>0) {
            sAusfertigungsdatum = PGtokenizer.removeCurlyBrace(sAusfertigungsdatum);
            try {
                bplan.setAusfertigungsdatum( LocalDate.parse(sAusfertigungsdatum));
            } catch (DateTimeParseException e) {
                throw new IOException("Konnt Inkrafttretensdatum nicht \""+sAusfertigungsdatum+"\" parsen");
            } //="2018-01-05"
        }

        String sVeroeffentlichungsdatum = getAsString(f, "veroeffentlichungsdatum");
        if (sVeroeffentlichungsdatum!=null && sVeroeffentlichungsdatum.trim().length()>0) {
            sVeroeffentlichungsdatum = PGtokenizer.removeCurlyBrace(sVeroeffentlichungsdatum);
            try {
                bplan.setVeroeffentlichungsdatum( LocalDate.parse(sVeroeffentlichungsdatum));
            } catch (DateTimeParseException e) {
                throw new IOException("Konnt Veroeffentlichungsdatum nicht \""+sVeroeffentlichungsdatum+"\" parsen");
            } //="2018-01-05"
        }        
        
        String sIsErschliessungsvertrag =  getAsString(f, "erschliessungsvertrag");
        if (sIsErschliessungsvertrag != null) {
            sIsErschliessungsvertrag = sIsErschliessungsvertrag.trim().toLowerCase();
            if ("true".equals(sIsErschliessungsvertrag)) {
                bplan.setIsErschliessungsvertrag(true);
            } else if ("false".equals(sIsErschliessungsvertrag)) {
                bplan.setIsErschliessungsvertrag(false);                
            } else {
                throw new IllegalArgumentException("Erschliessungsvertrag is \""+sIsErschliessungsvertrag+"\" not true or false");
            }
        }
        String sVeraenderungssperrebeschlussdatum = getAsString(f, "veraenderungssperrebeschlussdatum");
        if (sVeraenderungssperrebeschlussdatum!=null && sVeraenderungssperrebeschlussdatum.trim().length()>0) {
            sVeraenderungssperrebeschlussdatum = PGtokenizer.removeCurlyBrace(sVeraenderungssperrebeschlussdatum);
            try {
                bplan.setVeraenderungssperrebeschlussdatum( LocalDate.parse(sVeraenderungssperrebeschlussdatum));
            } catch (DateTimeParseException e) {
                throw new IOException("Konnt Veraenderungssperrebeschlussdatum nicht \""+sVeraenderungssperrebeschlussdatum+"\" parsen");
            } //="2018-01-05"
        }    
        String sVeraenderungssperredatum = getAsString(f, "veraenderungssperredatum");
        if (sVeraenderungssperredatum!=null && sVeraenderungssperredatum.trim().length()>0) {
            sVeraenderungssperredatum = PGtokenizer.removeCurlyBrace(sVeraenderungssperredatum);
            try {
                bplan.setVeraenderungssperredatum( LocalDate.parse(sVeraenderungssperredatum));
            } catch (DateTimeParseException e) {
                throw new IOException("Konnt Veraenderungssperredatum nicht \""+sVeraenderungssperredatum+"\" parsen");
            } //="2018-01-05"
        }           

        String sVeraenderungssperreenddatum = getAsString(f, "veraenderungssperreenddatum");
        if (sVeraenderungssperreenddatum!=null && sVeraenderungssperreenddatum.trim().length()>0) {
            sVeraenderungssperreenddatum = PGtokenizer.removeCurlyBrace(sVeraenderungssperreenddatum);
            try {
                bplan.setVeraenderungssperreenddatum( LocalDate.parse(sVeraenderungssperreenddatum));
            } catch (DateTimeParseException e) {
                throw new IOException("Konnt Veraenderungssperreenddatum nicht \""+sVeraenderungssperreenddatum+"\" parsen");
            } //="2018-01-05"
        }            
        
        bplan.setVerlaengerungveraenderungssperre( getAsString(f, "verlaengerungveraenderungssperre")); // "{10001}"
        
        String sSatzungsbeschlussdatum = getAsString(f, "satzungsbeschlussdatum");
        if (sSatzungsbeschlussdatum!=null && sSatzungsbeschlussdatum.trim().length()>0) {
            sSatzungsbeschlussdatum = PGtokenizer.removeCurlyBrace(sSatzungsbeschlussdatum);
            try {
                bplan.setSatzungsbeschlussdatum( LocalDate.parse(sSatzungsbeschlussdatum));
            } catch (DateTimeParseException e) {
                throw new IOException("Konnt Satzungsbeschlussdatum nicht \""+sSatzungsbeschlussdatum+"\" parsen");
            } //="2018-01-05"
        }            
        
        String sGenehmigungsdatum = getAsString(f, "genehmigungsdatum");
        if (sGenehmigungsdatum!=null && sGenehmigungsdatum.trim().length()>0) {
            sGenehmigungsdatum = PGtokenizer.removeCurlyBrace(sGenehmigungsdatum);
            try {
                bplan.setGenehmigungsdatum( LocalDate.parse(sGenehmigungsdatum));
            } catch (DateTimeParseException e) {
                throw new IOException("Konnt Genehmigungsdatum nicht \""+sGenehmigungsdatum+"\" parsen");
            } //="2018-01-05"
        }          

        String sUntergangsdatum = getAsString(f, "untergangsdatum");
        if (sUntergangsdatum!=null && sUntergangsdatum.trim().length()>0) {
            sUntergangsdatum = PGtokenizer.removeCurlyBrace(sUntergangsdatum);
            try {
                bplan.setUntergangsdatum( LocalDate.parse(sUntergangsdatum));
            } catch (DateTimeParseException e) {
                throw new IOException("Konnt Untergangsdatum nicht \""+sUntergangsdatum+"\" parsen");
            } //="2018-01-05"
        } 
        
        String sIsDurchfuehrungsvertrag =  getAsString(f, "durchfuehrungsvertrag");
        if (sIsDurchfuehrungsvertrag != null) {
            sIsDurchfuehrungsvertrag = sIsDurchfuehrungsvertrag.trim().toLowerCase();
            if ("true".equals(sIsDurchfuehrungsvertrag)) {
                bplan.setIsDurchfuehrungsvertrag(true);
            } else if ("false".equals(sIsDurchfuehrungsvertrag)) {
                bplan.setIsDurchfuehrungsvertrag(false);                
            } else {
                throw new IllegalArgumentException("Durchfuehrungsvertrag is \""+sIsDurchfuehrungsvertrag+"\" not true or false");
            }
        }
        
        String sIsGruenordnungsplan =  getAsString(f, "gruenordnungsplan");
        if (sIsGruenordnungsplan != null) {
            sIsGruenordnungsplan = sIsGruenordnungsplan.trim().toLowerCase();
            if ("true".equals(sIsGruenordnungsplan)) {
                bplan.setIsGruenordnungsplan(true);
            } else if ("false".equals(sIsGruenordnungsplan)) {
                bplan.setIsGruenordnungsplan(false);                
            } else {
                throw new IllegalArgumentException("Gruenordnungsplan is \""+sIsGruenordnungsplan+"\" not true or false");
            }
        }      
        
        String sIsStaedtebaulichervertrag =  getAsString(f, "staedtebaulichervertrag");
        if (sIsStaedtebaulichervertrag != null) {
            sIsStaedtebaulichervertrag = sIsStaedtebaulichervertrag.trim().toLowerCase();
            if ("true".equals(sIsStaedtebaulichervertrag)) {
                bplan.setIsStaedtebaulichervertrag(true);
            } else if ("false".equals(sIsStaedtebaulichervertrag)) {
                bplan.setIsStaedtebaulichervertrag(false);                
            } else {
                throw new IllegalArgumentException("Staedtebaulichervertrag is \""+sIsStaedtebaulichervertrag+"\" not true or false");
            }
        }        
        
        String sAufstellungsbeschlussdatum = getAsString(f, "aufstellungsbeschlussdatum");
        if (sAufstellungsbeschlussdatum!=null && sAufstellungsbeschlussdatum.trim().length()>0) {
            sAufstellungsbeschlussdatum = PGtokenizer.removeCurlyBrace(sAufstellungsbeschlussdatum);
            try {
                bplan.setAufstellungsbeschlussdatum( LocalDate.parse(sAufstellungsbeschlussdatum));
            } catch (DateTimeParseException e) {
                throw new IOException("Konnt Aufstellungsbeschlussdatum nicht \""+sAufstellungsbeschlussdatum+"\" parsen");
            } //="2018-01-05"
        }  
        
        
        String sPlanaufstellendegemeinde = (String) getAsString(f, "planaufstellendegemeinde");
        if (sPlanaufstellendegemeinde!=null) {
            try {
                PlanaufstellendeGemeinde[] gemeinden = objectReader.readValue(sPlanaufstellendegemeinde, PlanaufstellendeGemeinde[].class);
                bplan.setPlanaufstellendegemeinde ( gemeinden ); // "{"ags" : "13072072", "rs" : "130725260072", "gemeindename" : "Mönchhagen", "ortsteilname" : "Mönchhagen"}"
            } catch (MismatchedInputException ex) {
                throw new IllegalArgumentException("Konnte den String \""+sPlanaufstellendegemeinde+"\" für planaufstellendegemeinde nicht als JSON interpretieren.", ex);
            }
        }        
        
        String sAuslegungsstartdatum = getAsString(f, "auslegungsstartdatum"); 
        if (sAuslegungsstartdatum!=null && sAuslegungsstartdatum.trim().length()>0) {
            try {
                bplan.setAuslegungsstartdatum( parseLocalDateArr(sAuslegungsstartdatum));
            } catch (DateTimeParseException e) {
                throw new IOException("Konnt Auslegungssstartdatum nicht \""+sAuslegungsstartdatum+"\" parsen");
            }
        }        
        String sAuslegungsenddatum = getAsString(f, "auslegungsenddatum");
        if (sAuslegungsenddatum!=null && sAuslegungsenddatum.trim().length()>0) {
            try {
                bplan.setAuslegungsenddatum( parseLocalDateArr(sAuslegungsenddatum));
            } catch (DateTimeParseException e) {
                throw new IOException("Konnt Auslegungsenddatum nicht \""+sAuslegungsenddatum+"\" parsen");
            }
        }
        
        String sTraegerbeteiligungsStartDatum = getAsString(f, "traegerbeteiligungsstartdatum");
        if (sTraegerbeteiligungsStartDatum!=null && sTraegerbeteiligungsStartDatum.trim().length()>0) {
            try {
                bplan.setTraegerbeteiligungsstartdatum( parseLocalDateArr(sTraegerbeteiligungsStartDatum));
            } catch (DateTimeParseException e) {
                throw new IOException("Konnt TraegerbeteiligungsStartDatum nicht \""+sTraegerbeteiligungsStartDatum+"\" parsen");
            }
        }

        String sTraegerbeteiligungsEndDatum = getAsString(f, "traegerbeteiligungsenddatum");
        if (sTraegerbeteiligungsEndDatum!=null && sTraegerbeteiligungsEndDatum.trim().length()>0) {
           try {
                bplan.setTraegerbeteiligungsenddatum( parseLocalDateArr(sTraegerbeteiligungsEndDatum));
            } catch (DateTimeParseException e) {
                throw new IOException("Konnt TraegerbeteiligungsEndDatum nicht \""+sTraegerbeteiligungsEndDatum+"\" parsen");
            }
        }        
        
        
        String sGemeinde = (String) getAsString(f, "gemeinde");
        if (sGemeinde!=null && sGemeinde.length()>0) {
            Gemeinde[] gemeinde = objectReader.readValue(sGemeinde, Gemeinde[].class);      
            
            bplan.setGemeinde ( gemeinde ); // "{"ags" : "13072072", "rs" : "130725260072", "gemeindename" : "Mönchhagen", "ortsteilname" : "Mönchhagen"}"
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
        
        
        
        
        
        
        
        
        
        
//        String sExtenalRefs = (String) f.getAttribute("externereferenz");
        String sExtenalRefs = getAsString( f, "externereferenz");
//        System.err.println("externereferenz=\""+sExtenalRefs+"\"");
        if (sExtenalRefs!=null) {
            ExterneRefAuslegung[] extRefs = null;
            try {
                extRefs = objectReader.readValue(sExtenalRefs, ExterneRefAuslegung[].class);
            } catch (IOException e) {
//                System.err.println("Fehler extern");
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
        
        
        
        CodeList status = getCodeList(objectReader, "xplan_gml.bp_status", f, "status");
        bplan.setStatus(status);
        return bplan;
    }


    static CodeList getCodeList(ObjectReader objectReader, String codelistTable, SimpleFeature f, String attN ) throws IOException {        
        Property property = f.getProperty(attN);
        if (property==null) {
            return null;
        } else {
            Object v = property.getValue();
            if (v != null) {
                String s = property.getValue().toString();
                s = PGtokenizer.removeBox(s);
                CodeList codeList = objectReader.readValue(s, CodeList.class);
                return codeList;
            } else {
                logger.debug("getAsString("+attN+")  name=\""+property.getName()+"\"  \""+property.getType()+"\"  => \"null\"");
                return null;
            }
            
        }
        
    }    

    static public String getAsString(SimpleFeature f, String attN ) {
        Property property = f.getProperty(attN);
        if (property==null) {
            return null;
        } else {
            Object v = property.getValue();
            if (v != null) {
                String s = property.getValue().toString();
    //            s  = PGtokenizer.removeCurlyBrace(s);
                if ("{}".equals(s) || s.length()==0) {
                    s = null;
                }
//                System.err.println("getAsString("+attN+")  name=\""+property.getName()+"\"  \""+property.getType()+"\"  => \""+s+"\"");
//                for (Map.Entry<?, ?> entry : property.getUserData().entrySet()) {
//                    System.err.println("\t"+entry.getKey()+"="+entry.getValue());
//                }
                return s;
            } else {
//                System.err.println("getAsString("+attN+")  name=\""+property.getName()+"\"  \""+property.getType()+"\"  => \"null\"");
                return null;
            }
            
        }
        
    }
    
    
    

    private String[] getEnumValues(String s) {
//        System.err.println("getPlanArten()  \""+s+"\"");
        if (s!=null) {
            s = PGtokenizer.removeCurlyBrace(s);
            s = PGtokenizer.removeBox(s);
            return s.split(",");
        }
        return null;
    }
    
    
    static LocalDate[] parseLocalDateArr(String s) throws DateTimeException {
        s = PGtokenizer.removeCurlyBrace(s);
        s = PGtokenizer.removeBox(s);
        String[] sArr = s.split(",");
        if (sArr.length == 1) {
            return new LocalDate[] {LocalDate.parse(sArr[0])};
        } else {
            List<LocalDate> localDates = new ArrayList<>();
            for (int i=0; i<sArr.length; i++) {
                if (sArr[i].length()>0) {
                    localDates.add(LocalDate.parse(sArr[i]));
                }
            }
            return localDates.toArray(new LocalDate[sArr.length]);
        }
    }

}
