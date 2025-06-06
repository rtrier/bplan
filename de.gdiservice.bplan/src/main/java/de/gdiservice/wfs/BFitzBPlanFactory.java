package de.gdiservice.wfs;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;
//import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.postgresql.util.PGtokenizer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import de.gdiservice.bplan.poi.BPlan;
import de.gdiservice.bplan.poi.Gemeinde;
import de.gdiservice.bplan.poi.PGSpezExterneReferenz;
import de.gdiservice.bplan.poi.SpezExterneRef;

public class BFitzBPlanFactory implements WFSFactory<BPlan>  {
    

	@Override
	public BPlan build(SimpleFeature f) throws IOException {
		
		ObjectReader objectReader = new ObjectMapper().reader();
		
		final BPlan bplan = new BPlan();
		
		bplan.setId(f.getID());
		bplan.setName ( (String) f.getAttribute("name"));
		bplan.setGml_id( (String) f.getAttribute("gml_id")); //"67db195e-9203-4856-9bc9-8ea491153652"
		bplan.setNummer( (String) f.getAttribute("nummer")); //"3.1, 1. Änderung"
		bplan.setPlanart( getPlanArten((String)f.getAttribute("planart"))); // "{10001}"
		bplan.setRechtsstand((String) f.getAttribute("rechtsstand")); //="4000"
		
			String sDate = (String) f.getAttribute("inkrafttretensdatum");
			if (sDate!=null) {
				try {
					bplan.setInkrafttretensdatum(LocalDate.parse(sDate));
				} catch (DateTimeParseException e) {
					throw new IOException("Konnt Datum noicht \""+sDate+"\" parsen");
				} //="2018-01-05"
			}

		String sGemeinde = (String) f.getAttribute("gemeinde");
		if (sGemeinde!=null) {
			Gemeinde gemeinde = objectReader.readValue(sGemeinde, Gemeinde.class);		
			bplan.setGemeinde ( new Gemeinde[] {gemeinde} ); // "{"ags" : "13072072", "rs" : "130725260072", "gemeindename" : "Mönchhagen", "ortsteilname" : "Mönchhagen"}"
		}
		
		
		String sExtenalRefs = (String) f.getAttribute("externereferenz");
		if (sExtenalRefs!=null) {
		  SpezExterneRef[] extRefs = objectReader.readValue(sExtenalRefs, SpezExterneRef[].class);		
			if (extRefs != null && extRefs.length>0) {
			  PGSpezExterneReferenz[] pgRefs = new PGSpezExterneReferenz[extRefs.length];
  			for (int i=0; i<extRefs.length; i++) {
  			  pgRefs[i] = new PGSpezExterneReferenz(extRefs[i]);
  			}
  			bplan.setExterneReferenzes(pgRefs);
			// bplan.externeReferenzes = extRefs; // ="[{"georefurl" : null, "georefmimetype" : null, "art" : "Dokument", "informationssystemurl" : "https://www.amt-rostocker-heide.de/amt-rostocker-heide/Geo-Daten-Amt-Rostocker-Heide/", "referenzname" : "amt_rostocker_heide_moenchhagen_bplan_3_1_1_1.pdf", "referenzurl" : "https://service.btfietz.de/wmsdata/amt_rostocker_heide/amt_rostocker_heide_moenchhagen_bplan_3_1_1_1.pdf", "referenzmimetype" : {"codespace" : "https://bauleitplaene-mv.de/codelist/XP_MimeTypes.xml", "id" : "application/pdf", "value" : "application/pdf"}, "beschreibung" : "Satzung über 1. Änderung des Bebauungsplans (438,49 KB)", "datum" : "2003-10-07", "typ" : "1060"}, {"georefurl" : null, "georefmimetype" : null, "art" : "Dokument", "informationssystemurl" : "https://www.amt-rostocker-heide.de/amt-rostocker-heide/Geo-Daten-Amt-Rostocker-Heide/", "referenzname" : "amt_rostocker_heide_moenchhagen_bplan_3_1_2_1.pdf", "referenzurl" : "https://service.btfietz.de/wmsdata/amt_rostocker_heide/amt_rostocker_heide_moenchhagen_bplan_3_1_2_1.pdf", "referenzmimetype" : {"codespace" : "https://bauleitplaene-mv.de/codelist/XP_MimeTypes.xml", "id" : "application/pdf", "value" : "application/pdf"}, "beschreibung" : "Satzung über 2. Änderung des Bebauungsplans (2,47 MB)", "datum" : "2012-04-03", "typ" : "1060"}, {"georefurl" : null, "georefmimetype" : null, "art" : "Dokument", "informationssystemurl" : "https://www.amt-rostocker-heide.de/amt-rostocker-heide/Geo-Daten-Amt-Rostocker-Heide/", "referenzname" : "amt_rostocker_heide_moenchhagen_bplan_3_1_3_1.pdf", "referenzurl" : "https://service.btfietz.de/wmsdata/amt_rostocker_heide/amt_rostocker_heide_moenchhagen_bplan_3_1_3_1.pdf", "referenzmimetype" : {"codespace" : "https://bauleitplaene-mv.de/codelist/XP_MimeTypes.xml", "id" : "application/pdf", "value" : "application/pdf"}, "beschreibung" : "Satzung über 3. Änderung des Bebauungsplans (2,3 MB)", "datum" : "2017-12-01", "typ" : "1060"}, {"georefurl" : null, "georefmimetype" : null, "art" : "Dokument", "informationssystemurl" : "https://www.amt-rostocker-heide.de/amt-rostocker-heide/Geo-Daten-Amt-Rostocker-Heide/", "referenzname" : "amt_rostocker_heide_moenchhagen_bplan_3_1_3_2.pdf", "referenzurl" : "https://service.btfietz.de/wmsdata/amt_rostocker_heide/amt_rostocker_heide_moenchhagen_bplan_3_1_3_2.pdf", "referenzmimetype" : {"codespace" : "https://bauleitplaene-mv.de/codelist/XP_MimeTypes.xml", "id" : "application/pdf", "value" : "application/pdf"}, "beschreibung" : "Begründung zur 3. Änderung des Bebauungsplans (186,38 KB)", "datum" : "2017-12-01", "typ" : "1010"}]"
			}
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
