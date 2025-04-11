package de.gdiservice.bplan;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class PGUtil {
    
    static final String getString(String s) {       
        if (s!=null && s.startsWith("\"") && s.endsWith("\"")) {            
            return s.substring(1, s.length()-1);
        }
        return "".equals(s) ? null : s; 
    }
    
    
    static final String getStringValue(String s) {
        if (s==null) {
            return "";
        } else {
            // s = s.replaceAll(",", "%2C");
            // TODO rtr
            return s.replaceAll("\"", "\"\"").replace("(", "\\(").replace(")", "\\)");
//            return "\\\"" + s + "\\\"";
        }
//        return s == null ? "" : s.replaceAll("\"", "\"\""); 
    }
    
    static final String getDateValue(Date date) {
        return date == null ? "" : new SimpleDateFormat("yyyy-MM-dd").format(date); 
    }
    
    static Date getDate(String s) throws ParseException {
        // System.out.println("getDate(\""+s+"\")");
        if (s!=null && s.length()>0) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            return sdf.parse(s);
        }
        else {
            return null;
        }
        // return s==null || s.length()==0 ? null : (new SimpleDateFormat("yyyy-MM-dd")).parse(s);
    }
    
    
    static String trim(String gemeindename) {
        if (gemeindename==null || gemeindename.length()==0) {
            return null;
        }
        if (gemeindename.startsWith("\"")) {
            gemeindename = gemeindename.substring(1);
        }
        if (gemeindename.endsWith("\"")) {
            gemeindename = gemeindename.substring(0, gemeindename.length()-1);
        }
        return gemeindename;
    }

}
