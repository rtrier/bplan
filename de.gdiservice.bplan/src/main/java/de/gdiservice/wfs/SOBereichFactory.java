package de.gdiservice.wfs;

import java.io.IOException;
import java.util.UUID;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import de.gdiservice.bplan.poi.ExterneRef;
import de.gdiservice.bplan.poi.SOBereich;
import de.gdiservice.bplan.poi.PGExterneReferenz;

public class SOBereichFactory implements WFSFactory<SOBereich>  {
    
    final static Logger logger = LoggerFactory.getLogger(SOBereichFactory.class);
    

	@Override
	public SOBereich build(SimpleFeature f) throws IOException {
		
		ObjectReader objectReader = new ObjectMapper().reader();
		
		final SOBereich bereich = new SOBereich();
		
		
//	    UUID gml_id;
		String sUUID = WFSFactory.getAsString(f, "gml_id");
		if  (sUUID!=null) {		    
		        try {
                    UUID uuid = UUID.fromString(sUUID);
                    bereich.setGml_id(uuid);
                } catch (IllegalArgumentException e) {
                    int idx = sUUID.indexOf("\n");
                    if (idx>36) {
                        sUUID = sUUID.substring(0, idx)+"..";
                    }
                    throw new IOException("gml_id \""+  sUUID +"\" ist fehlerhaft. "+e.getMessage());
                }
//		} else {
//		    throw new IOException("gml_id fehlt (name="+ WFSFactory.getAsString(f, "name") + " gehörtzuplan="+WFSFactory.getAsString(f, "gehoertzuplan")+"). ");
		}
		
//	    Integer nummer;
        bereich.setNummer( WFSFactory.getAsInteger(f, "nummer") );        		
//	    String name;
        bereich.setName( WFSFactory.getAsString(f, "name") );
//	    String bedeutung; // Integer 
        bereich.setBedeutung( WFSFactory.getAsString(f, "bedeutung") );
//	    String detailliertebedeutung;
        bereich.setDetailliertebedeutung( WFSFactory.getAsString(f, "detailliertebedeutung"));
//	    Integer erstellungsmassstab;
        bereich.setErstellungsmassstab( WFSFactory.getAsInteger(f, "erstellungsmassstab"));
//	    Geometry geltungsbereich;
        Geometry geom = (Geometry)f.getDefaultGeometry();
        if (geom instanceof Polygon) {
            final MultiPolygon mPolygon = new MultiPolygon(new Polygon[] {(Polygon)geom}, new GeometryFactory());
            mPolygon.setSRID(25833);
            bereich.setGeltungsbereich(mPolygon);
        } else {
            if (geom instanceof MultiPolygon) {
                geom.setSRID(25833);
                bereich.setGeltungsbereich((MultiPolygon)geom);
            }
        }
//	    Integer user_id;
        bereich.setUser_id( WFSFactory.getAsInteger(f, "user_id"));
//	    LocalDate created_at;
        bereich.setCreated_at( WFSFactory.getAsLocalDate(f, "created_at"));
//	    LocalDate updated_at;
        bereich.setCreated_at( WFSFactory.getAsLocalDate(f, "updated_at"));

        bereich.setUser_id( WFSFactory.getAsInteger(f, "user_id"));
//	    String planinhalt;
        bereich.setPlaninhalt( WFSFactory.getAsString(f, "planinhalt") );
//	    String praesentationsobjekt;
        bereich.setPraesentationsobjekt( WFSFactory.getAsString(f, "praesentationsobjekt") );
//	    String rasterbasis;
        bereich.setRasterbasis( WFSFactory.getAsString(f, "rasterbasis") );
//	    PGExterneReferenz[] refscan;
        String sExtenalRefs = WFSFactory.getAsString( f, "refscan");
        if (sExtenalRefs!=null) {
          ExterneRef[] extRefs = null;
          try {
              extRefs = objectReader.readValue(sExtenalRefs, ExterneRef[].class);
          } catch (IOException e) {
             throw new IllegalArgumentException("String \""+sExtenalRefs+"\" could not be parsed as an Array of ExterneRef (attName=ref"); 
          }   
          if (extRefs != null && extRefs.length>0) {
              PGExterneReferenz[] pgRefs = new PGExterneReferenz[extRefs.length];
              for (int i=0; i<extRefs.length; i++) {
                  pgRefs[i] = new PGExterneReferenz(extRefs[i]);
              }
              bereich.setRefscan(pgRefs);
          }
      }
//	    UUID gehoertzuplan;		
        String sGehoertzuplan = WFSFactory.getAsString(f, "gehoertzuplan");
        if (sGehoertzuplan == null) {
            sGehoertzuplan = WFSFactory.getAsString(f, "plan_gml_id");
            if (sGehoertzuplan == null) {
                throw new IllegalArgumentException("Feature ohne Wert für gehoertzuplan");
            }
        }
        try {
            bereich.setGehoertzuplan( UUID.fromString(sGehoertzuplan) );
        } catch (Exception e) {
            throw new IllegalArgumentException("Wert \""+sGehoertzuplan+"\" für gehoertzuplan konnte nicht als UUID interpretiert werden.");
        }
		
		return bereich;
	}


}
