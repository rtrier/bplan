package de.gdiservice.bplan.konvertierung;

import java.util.Date;





public class Konvertierung {
    
    public enum KonvertierungStatus {
        inErstellung("in Erstellung"),
        erstellt("erstellt"),
        AngabenVollstaendig("Angaben vollständig"),
        inKonvertierung("in Konvertierung"),
        KonvertierungAbgeschlossen("Konvertierung abgeschlossen"),
        KonvertierungAbgebrochen("Konvertierung abgebrochen"),
        inGMLErstellung("in GML-Erstellung"),
        GMLErstellungAbgeschlossen("GML-Erstellung abgeschlossen"),
        GMLErstellungAbgebrochen("GML-Erstellung abgebrochen"),
        INSPIRE_GMLErstellungAbgeschlossen("INSPIRE-GML-Erstellung abgeschlossen"),
        INSPIRE_GMLErstellungAbgebrochen("INSPIRE-GML-Erstellung abgebrochen"),
        inINSPIRE_GMLErstellung("in INSPIRE-GML-Erstellung");
        
        private String text;
        
        KonvertierungStatus(String s) {
            text = s;
        }
        
        public String getText() {
            return text;
        }
        
        public static KonvertierungStatus get(String text) {
            return java.util.Arrays.stream(KonvertierungStatus.values())
            .filter(v -> v.text.equals(text))
            .findFirst()
            .orElse(null);
        }
    }
    

    
    public enum EPSGCodes {
    	EPSG_31462(31462),
    	EPSG_31463(31463),
    	EPSG_31467(31467),
    	EPSG_31468(31468),
    	EPSG_31469(31469),
    	EPSG_25832(25832),
    	EPSG_25833(25833),
    	EPSG_325833(325833),
    	EPSG_3857(3857),
    	EPSG_32633(32633),
    	EPSG_3044(3044),
    	EPSG_4647(4647),
    	EPSG_5650(5650),
    	EPSG_2398(2398);
	
    	private Integer epsg;
    
        EPSGCodes(Integer code) {
            epsg = code;
        }
        
        public Integer getCode() {
            return epsg;
        }
        
        public static EPSGCodes get(Integer code) {
            return java.util.Arrays.stream(EPSGCodes.values())
            .filter(v -> v.epsg.equals(code))
            .findFirst()
            .orElse(null);
        }
    }


    public Integer id;
    public String bezeichnung;
    public KonvertierungStatus status;
    public Integer stelle_id;
    public String beschreibung;
    public Integer shape_layer_group_id;
    public Date created_at;
    public Date updated_at;
    public Integer user_id;
    public Integer geom_precision;
    public Integer gml_layer_group_id;
    public EPSGCodes epsg;
    public EPSGCodes output_epsg;
    public EPSGCodes input_epsg;
    public String gebietseinheiten;
    public String planart;
    public Boolean veroeffentlicht;
    
}
