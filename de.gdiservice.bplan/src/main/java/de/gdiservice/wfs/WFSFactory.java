package de.gdiservice.wfs;

import java.io.IOException;
import java.time.LocalDate;

import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.postgresql.util.PGtokenizer;

public interface WFSFactory<T> {
	
	T build(SimpleFeature f) throws IOException;

	
    static public String getAsString(SimpleFeature f, String attN ) {
        Property property = f.getProperty(attN);
        if (property==null) {
            return null;
        } else {
            Object v = property.getValue();
            if (v != null) {
                String s = property.getValue().toString();
                if ("{}".equals(s) || s.length()==0) {
                    s = null;
                }
                return s;
            } else {
                return null;
            }
        }
    }
    
    
    static public Integer getAsInteger(SimpleFeature f, String attN ) throws IOException {
        String s = WFSFactory.getAsString(f, attN);
        if (s != null) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                throw new IOException("Konnte "+attN+ " \""+s+"\" nicht als Integer parsen");
            }
        }
        return null;
    }
    
    
    static public LocalDate getAsLocalDate(SimpleFeature f, String attN ) throws IOException {
        String s = WFSFactory.getAsString(f, attN);
        if (s != null) {
            s = PGtokenizer.removeCurlyBrace(s);
            try {
                return LocalDate.parse(s);
            } catch (Exception e) {
                throw new IOException("Konnte "+attN+ " \""+s+"\" nicht Datum parsen");
            }
        }
        return null;
    }
}
