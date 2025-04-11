package de.gdiservice.bplan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


public class VerbundenerPlan {

    final static Logger logger = LoggerFactory.getLogger(VerbundenerPlan.class);

    public enum RechtscharakterPlanaenderung {   
        Aenderung("1000"),
        Ergaenzung("1100"),
        Aufhebung("2000"),
        Aufhebungsverfahren("20000"),
        Ueberplanung("20001");

        private String art; 

        RechtscharakterPlanaenderung(String art) {
            this.art = art;
        }

        public String getArt() {
            return art;
        }

        public static RechtscharakterPlanaenderung get(String art) {
            return java.util.Arrays.stream(RechtscharakterPlanaenderung.values())
                    .filter(v -> v.art.equals(art))
                    .findFirst()
                    .orElse(null);
        }

    }



    String planname;
    @JsonIgnore
    RechtscharakterPlanaenderung rechtscharakter;
    String nummer;
    String verbundenerplan;
    
    @JsonProperty
    public void setRechtscharakter(String v) {
        rechtscharakter = RechtscharakterPlanaenderung.get(v);
    }
    
    public VerbundenerPlan() {
        
    }
    

    
    public VerbundenerPlan(String planname, RechtscharakterPlanaenderung rechtscharakter, String nummer, String verbundenerplan) {
        this.planname = planname;
        this.rechtscharakter = rechtscharakter;
        this.nummer = nummer;
        this.verbundenerplan = verbundenerplan;
    }



    public String getPlanname() {
        return planname;
    }
    public void setPlanname(String planname) {
        this.planname = planname;
    }
    public RechtscharakterPlanaenderung getRechtscharakter() {
        return rechtscharakter;
    }
    public void setRechtscharakter(RechtscharakterPlanaenderung rechtscharakter) {
        this.rechtscharakter = rechtscharakter;
    }
    public String getNummer() {
        return nummer;
    }
    public void setNummer(String nummer) {
        this.nummer = nummer;
    }
    public String getVerbundenerplan() {
        return verbundenerplan;
    }
    public void setVerbundenerplan(String verbundenerplan) {
        this.verbundenerplan = verbundenerplan;
    }
    public static Logger getLogger() {
        return logger;
    }
    @Override
    public String toString() {
        return "Verbundenerplan [planname=" + planname + ", rechtscharakter=" + rechtscharakter + ", nummer=" + nummer
                + ", verbundenerplan=" + verbundenerplan + "]";
    }
    
    

}
