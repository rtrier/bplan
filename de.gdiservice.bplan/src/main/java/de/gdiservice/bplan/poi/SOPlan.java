package de.gdiservice.bplan.poi;

import java.time.LocalDate;
import java.util.UUID;


public class SOPlan extends XPPlan {


    public enum PlanArt {
        Sanierungssatzung_SAS(1100),
        StaedtebaulicheEntwicklungsmassnahme(1200),
        Stadtumbaugebiet(1300),
        Erhaltungssatzung_SOS(1999),
        SonstigeBausatzung(9999);

        private Integer art; 

        PlanArt(Integer art) {
            this.art = art;
        }
        
        public Integer getArt() {
            return art;
        }
        
        public static PlanArt get(int art) {
            return java.util.Arrays.stream(PlanArt.values())
            .filter(v -> v.art.equals(art))
            .findFirst()
            .orElse(null);
        }
    }

    public String id; // "B_Plan.67db195e-9203-4856-9bc9-8ea491153652"

    public PG_SO_Planart planart; // "{10001}"

    public LocalDate technHerstellDatum; // neu

    public LocalDate[] auslegungsstartdatum;
    public LocalDate[] auslegungsenddatum;

    public Gemeinde[] gemeinde; // "{"ags" : "13072072", "rs" : "130725260072", "gemeindename" : "Mönchhagen", "ortsteilname" : "Mönchhagen"}"

    public Gemeinde[] planaufstellendegemeinde; // neu
    
    public LocalDate veroeffentlichungsDatum; // neu
    
    public LocalDate aufstellungsbeschlussDatum; // neu
    
    


    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public PG_SO_Planart getPlanart() {
        return planart;
    }
    public void setPlanart(PG_SO_Planart planart) {
        this.planart = planart;
    }

    
    public LocalDate[] getAuslegungsstartdatum() {
        return auslegungsstartdatum;
    }
    public void setAuslegungsstartdatum(LocalDate auslegungsstartdatum) {
        this.auslegungsstartdatum = new LocalDate[] {auslegungsstartdatum};
    }
    public LocalDate[] getAuslegungsenddatum() {
        return auslegungsenddatum;
    }
    public void setAuslegungsenddatum(LocalDate auslegungsenddatum) {
        this.auslegungsenddatum = new LocalDate[] {auslegungsenddatum};
    }    
    
    public void setTechnHerstellDatum(LocalDate localDate) {
        this.technHerstellDatum = localDate;
    }
    public LocalDate getTechnHerstellDatum() {
        return technHerstellDatum;
    }
    
    public Gemeinde[] getGemeinde() {
        return gemeinde;
    }

    public void setGemeinde(Gemeinde[] gemeinden) {
        gemeinde = gemeinden;
    }
    
    public Gemeinde[] getPlanaufstellendegemeinde() {
        return planaufstellendegemeinde;
    }
    public void setPlanaufstellendegemeinde(Gemeinde[] planaufstellendegemeinde) {
        this.planaufstellendegemeinde = planaufstellendegemeinde;
    }
    

    @Override
    public UUID getKey() {
        return gml_id;
    }
    @Override
    public LocalDate getPublishDate() {
        if (wurdegeaendertvon==null && auslegungsstartdatum!=null && auslegungsstartdatum.length>0) {
            return auslegungsstartdatum[auslegungsstartdatum.length-1];                                        
        }
        return genehmigungsdatum;
    }
    
    

    public LocalDate getVeroeffentlichungsDatum() {
        return veroeffentlichungsDatum;
    }
    public void setVeroeffentlichungsDatum(LocalDate veroeffentlichungsDatum) {
        this.veroeffentlichungsDatum = veroeffentlichungsDatum;
    }
    
    public LocalDate getAufstellungsbeschlussdatum() {
        return aufstellungsbeschlussDatum;
        
    }
    public void setAufstellungsbeschlussdatum(LocalDate localDate) {
        this.aufstellungsbeschlussDatum = localDate;
        
    }






}
