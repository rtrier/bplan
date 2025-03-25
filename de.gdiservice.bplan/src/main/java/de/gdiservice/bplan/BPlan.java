package de.gdiservice.bplan;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.locationtech.jts.geom.Geometry;
//import org.postgresql.util.PGobject;

public class BPlan {


    public enum PlanArt {
        BPlan(1000),
        EinfacherBPlan(10000),
        QualifizierterBPlan(10001),
        VorhabenbezogenerBPlan(3000),
        VorhabenUndErschliessungsplan(3100),
        InnenbereichsSatzung(4000),
        KlarstellungsSatzung(40000),
        EntwicklungsSatzung(40001),
        ErgaenzungsSatzung(40002),
        AussenbereichsSatzung(5000),
        OertlicheBauvorschrift(7000),
        Sonstiges(9999);


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

    String id; // "B_Plan.67db195e-9203-4856-9bc9-8ea491153652"
    String name;
    UUID gml_id; //"67db195e-9203-4856-9bc9-8ea491153652"
    String nummer; //"3.1, 1. Änderung"
    String[] planart; // "{10001}"
    String rechtsstand; //="4000"
    String verfahren; //="4000"



    LocalDate inkrafttretensdatum; //="2018-01-05"
    LocalDate rechtsverordnungsdatum;
    LocalDate ausfertigungsdatum;
    
    // Tabelle xplankonverter.konvertierungen
    LocalDate veroeffentlichungsdatum;
    
    Boolean erschliessungsvertrag = null;



    Boolean durchfuehrungsvertrag = null;


    Boolean gruenordnungsplan = null;
    
    Boolean staedtebaulichervertrag = null;
    


    LocalDate[] auslegungsstartdatum;
    LocalDate[] auslegungsenddatum;
    
    LocalDate[] traegerbeteiligungsStartDatum;
    
    LocalDate veraenderungssperrebeschlussdatum;
    LocalDate veraenderungssperredatum;
    LocalDate veraenderungssperreenddatum;

    String verlaengerungveraenderungssperre;

    LocalDate satzungsbeschlussdatum;
    
    LocalDate aufstellungsbeschlussdatum;

    LocalDate[] traegerbeteiligungsstartdatum;
    LocalDate[] traegerbeteiligungsenddatum;




    LocalDate genehmigungsdatum;
    LocalDate untergangsdatum;










    Gemeinde[] gemeinde; // "{"ags" : "13072072", "rs" : "130725260072", "gemeindename" : "Mönchhagen", "ortsteilname" : "Mönchhagen"}"
    PGExterneReferenz[] externeReferenzes; // ="[{"georefurl" : null, "georefmimetype" : null, "art" : "Dokument", "informationssystemurl" : "https://www.amt-rostocker-heide.de/amt-rostocker-heide/Geo-Daten-Amt-Rostocker-Heide/", "referenzname" : "amt_rostocker_heide_moenchhagen_bplan_3_1_1_1.pdf", "referenzurl" : "https://service.btfietz.de/wmsdata/amt_rostocker_heide/amt_rostocker_heide_moenchhagen_bplan_3_1_1_1.pdf", "referenzmimetype" : {"codespace" : "https://bauleitplaene-mv.de/codelist/XP_MimeTypes.xml", "id" : "application/pdf", "value" : "application/pdf"}, "beschreibung" : "Satzung über 1. Änderung des Bebauungsplans (438,49 KB)", "datum" : "2003-10-07", "typ" : "1060"}, {"georefurl" : null, "georefmimetype" : null, "art" : "Dokument", "informationssystemurl" : "https://www.amt-rostocker-heide.de/amt-rostocker-heide/Geo-Daten-Amt-Rostocker-Heide/", "referenzname" : "amt_rostocker_heide_moenchhagen_bplan_3_1_2_1.pdf", "referenzurl" : "https://service.btfietz.de/wmsdata/amt_rostocker_heide/amt_rostocker_heide_moenchhagen_bplan_3_1_2_1.pdf", "referenzmimetype" : {"codespace" : "https://bauleitplaene-mv.de/codelist/XP_MimeTypes.xml", "id" : "application/pdf", "value" : "application/pdf"}, "beschreibung" : "Satzung über 2. Änderung des Bebauungsplans (2,47 MB)", "datum" : "2012-04-03", "typ" : "1060"}, {"georefurl" : null, "georefmimetype" : null, "art" : "Dokument", "informationssystemurl" : "https://www.amt-rostocker-heide.de/amt-rostocker-heide/Geo-Daten-Amt-Rostocker-Heide/", "referenzname" : "amt_rostocker_heide_moenchhagen_bplan_3_1_3_1.pdf", "referenzurl" : "https://service.btfietz.de/wmsdata/amt_rostocker_heide/amt_rostocker_heide_moenchhagen_bplan_3_1_3_1.pdf", "referenzmimetype" : {"codespace" : "https://bauleitplaene-mv.de/codelist/XP_MimeTypes.xml", "id" : "application/pdf", "value" : "application/pdf"}, "beschreibung" : "Satzung über 3. Änderung des Bebauungsplans (2,3 MB)", "datum" : "2017-12-01", "typ" : "1060"}, {"georefurl" : null, "georefmimetype" : null, "art" : "Dokument", "informationssystemurl" : "https://www.amt-rostocker-heide.de/amt-rostocker-heide/Geo-Daten-Amt-Rostocker-Heide/", "referenzname" : "amt_rostocker_heide_moenchhagen_bplan_3_1_3_2.pdf", "referenzurl" : "https://service.btfietz.de/wmsdata/amt_rostocker_heide/amt_rostocker_heide_moenchhagen_bplan_3_1_3_2.pdf", "referenzmimetype" : {"codespace" : "https://bauleitplaene-mv.de/codelist/XP_MimeTypes.xml", "id" : "application/pdf", "value" : "application/pdf"}, "beschreibung" : "Begründung zur 3. Änderung des Bebauungsplans (186,38 KB)", "datum" : "2017-12-01", "typ" : "1010"}]"
    Geometry geom; // f.getDefaultGeometry()="POLYGON ((318577.09 6003723.31, 318578.06 6003724.87, 318586.83 6003734.74, 318592.79 6003742.57, 318597.54 6003751.46, 318607.22 6003771.9, 318616.56 6003791.61, 318625.9 6003811.31, 318630.57 6003821.16, 318635.24 6003831.01, 318644.58 6003850.71, 318651.44 6003865.19, 318662.59 6003888.51, 318672.32 6003909.05, 318717.18 6004003.81, 318720.7 6004009.51, 318725.38 6004007.67, 318726.88 6004007.11, 318729.52 6004006.12, 318752.86 6003997.38, 318742.62 6003975.75, 318733.81 6003957.13, 318724.99 6003938.53, 318716.19 6003919.91, 318706.08 6003898.58, 318717.52 6003893.16, 318739.18 6003882.9, 318747.08 6003879.15, 318767.53 6003869.45, 318787.41 6003860.03, 318788.7 6003859.42, 318792.47 6003857.44, 318797.27 6003854.39, 318801.8 6003850.97, 318806.04 6003847.18, 318809.95 6003843.07, 318813.51 6003838.64, 318817.31 6003832.99, 318820.51 6003826.98, 318823.11 6003820.7, 318915.34 6003877.92, 318954.47 6003863.19, 318907.6 6003737.12, 318884.84 6003675.88, 318893.12 6003672.84, 318939 6003655.64, 318940.76 6003654.98, 318953.36 6003689.18, 318956.33 6003688.16, 318958.46 6003687.43, 318955.87 6003680.41, 318940.77 6003639.54, 318937.65 6003639.65, 318936.16 6003639.7, 318934.56 6003638.84, 318929.99 6003627.13, 318930.52 6003625.22, 318931.61 6003624.47, 318934.51 6003622.46, 318933.55 6003619.89, 318925.87 6003599.47, 318924.44 6003595.68, 318921.07 6003586.42, 318920.25 6003586.73, 318868.17 6003606.23, 318816.09 6003623.51, 318815.2 6003621.05, 318814.82 6003620.02, 318809.06 6003604.16, 318807.02 6003602.11, 318805.72 6003598.63, 318800.68 6003585.23, 318804.47 6003583.66, 318801.22 6003574.74, 318790.99 6003546.13, 318789.61 6003542.25, 318785.12 6003543.85, 318765.16 6003554.22, 318744.37 6003565.02, 318723.05 6003576.1, 318703.51 6003587.27, 318691.67 6003594.22, 318671.82 6003605.86, 318653.24 6003616.76, 318635.67 6003637.94, 318626.96 6003648.43, 318620.57 6003658, 318607.66 6003677.31, 318583.05 6003714.13, 318577.09 6003723.31))"

    String internalid;
    PGVerbundenerPlan[] aendert;
    PGVerbundenerPlan[] wurdegeaendertvon;
    
    // neu
    CodeList status;
    
    Gemeinde[] planaufstellendegemeinde;
    
    
    Integer konvertierung_id;
    
    


    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public UUID getGml_id() {
        return gml_id;
    }
    public void setGml_id(String gml_id) {
        try {
            this.gml_id = UUID.fromString(gml_id);
        } catch (Exception ex) {
            throw new IllegalArgumentException("gml_id \""+gml_id+"\" is not a UID");
        }
    }
    public void setGml_id(UUID gml_id) {
        this.gml_id = gml_id;
    }
    public String getNummer() {
        return nummer;
    }
    public void setNummer(String nummer) {
        this.nummer = nummer;
    }
    public String[] getPlanart() {
        return planart;
    }
    public void setPlanart(String[] planart) {
        this.planart = planart;
    }
    public String getRechtsstand() {
        return rechtsstand;
    }
    public void setRechtsstand(String rechtsstand) {
        this.rechtsstand = rechtsstand;
    }
    public LocalDate getInkrafttretensdatum() {
        return inkrafttretensdatum;
    }
    public void setInkrafttretensdatum(LocalDate inkrafttretensdatum) {
        this.inkrafttretensdatum = inkrafttretensdatum;
    }
    
    public LocalDate getRechtsverordnungsdatum() {
        return rechtsverordnungsdatum;
    }
    public void setRechtsverordnungsdatum(LocalDate date) {
        this.rechtsverordnungsdatum = date;
    }
    public LocalDate getAusfertigungsdatum() {
        return ausfertigungsdatum;
    }
    public void setAusfertigungsdatum(LocalDate date) {
        this.ausfertigungsdatum = date;
    }
    
    public LocalDate[] getAuslegungsstartdatum() {
        return auslegungsstartdatum;
    }
    public void setAuslegungsstartdatum(LocalDate auslegungsstartdatum) {
        this.auslegungsstartdatum = new LocalDate[] {auslegungsstartdatum};
    }
    public void setAuslegungsstartdatum(LocalDate[] auslegungsstartdatum) {
        this.auslegungsstartdatum = auslegungsstartdatum;
    }    
    
    
    public LocalDate[] getAuslegungsenddatum() {
        return auslegungsenddatum;
    }
    public void setAuslegungsenddatum(LocalDate auslegungsenddatum) {
        this.auslegungsenddatum = new LocalDate[] {auslegungsenddatum};
    }    
    public void setAuslegungsenddatum(LocalDate[] auslegungsenddatum) {
        this.auslegungsenddatum = auslegungsenddatum;
    }    
    
    public Gemeinde[] getPlanaufstellendegemeinde() {
        return planaufstellendegemeinde;
    }
    public void setPlanaufstellendegemeinde(Gemeinde[] planaufstellendegemeinde) {
        this.planaufstellendegemeinde = planaufstellendegemeinde;
    }
    
    
    public Boolean getIsErschliessungsvertrag() {
        return erschliessungsvertrag;
    }
    public void setIsErschliessungsvertrag(Boolean erschliessungsvertrag) {
        this.erschliessungsvertrag = erschliessungsvertrag;
    }    
    
    public Boolean getIsDurchfuehrungsvertrag() {
        return durchfuehrungsvertrag;
    }
    public void setIsDurchfuehrungsvertrag(Boolean durchfuehrungsvertrag) {
        this.durchfuehrungsvertrag = durchfuehrungsvertrag;
    }
    
    public Boolean getIsGruenordnungsplan() {
        return gruenordnungsplan;
    }
    public void setIsGruenordnungsplan(Boolean gruenordnungsplan) {
        this.gruenordnungsplan = gruenordnungsplan;
    }    
    
    public Boolean getIsStaedtebaulichervertrag() {
        return staedtebaulichervertrag;
    }
    public void setIsStaedtebaulichervertrag(Boolean staedtebaulichervertrag) {
        this.staedtebaulichervertrag = staedtebaulichervertrag;
    }
    
    public Gemeinde[] getGemeinde() {
        return gemeinde;
    }

    public void setGemeinde(Gemeinde[] gemeinden) {
        gemeinde = gemeinden;
    }

    public void setExterneReferenzes(PGExterneReferenz[] pgRefs) {
        externeReferenzes = pgRefs;
    }

    public PGExterneReferenz[] getExternereferenzes() {
        return externeReferenzes;
    }
    
    public void setStatus(CodeList status) {
        if (status != null) {
            status.setType("\"xplan_gml\".\"bp_status\"");
        }
        this.status = status;
    }

    public CodeList getStatus() {
        return status;
    }    
        

    public String getInternalId() {
        return internalid;
    }
    public void setInternalId(String internalid) {
        this.internalid = internalid;
    }
    public PGVerbundenerPlan[] getAendert() {
        return aendert;
    }
    public void setAendert(PGVerbundenerPlan[] aendert) {
        this.aendert = aendert;
    }
    public PGVerbundenerPlan[] getWurdeGeaendertVon() {
        return wurdegeaendertvon;
    }
    public void setWurdeGeaendertVon(PGVerbundenerPlan[] wurdegeaendertvon) {
        this.wurdegeaendertvon = wurdegeaendertvon;
    }
    
    public Geometry getGeom() {
        return geom;
    }
    public void setGeom(Geometry geom) {
        this.geom = geom;
    }


    public Integer getKonvertierungId() {
        return konvertierung_id;
    }
    public void setKonvertierungId(Integer konvertierungId) {
        this.konvertierung_id = konvertierungId;
    }
    
    
    public void addExterneReferenz(PGExterneReferenz pgExterneReferenz) {
        if (this.externeReferenzes==null) {
            if (pgExterneReferenz instanceof PGExterneReferenzAuslegung) {
                this.externeReferenzes = new PGExterneReferenzAuslegung[] {(PGExterneReferenzAuslegung)pgExterneReferenz};
            } else {
                this.externeReferenzes = new PGExterneReferenz[] {pgExterneReferenz};
            }
        } else {
            this.externeReferenzes = Arrays.copyOf(this.externeReferenzes, this.externeReferenzes.length + 1);
            this.externeReferenzes[this.externeReferenzes.length - 1] = pgExterneReferenz;
        }
    }
    
    
    public void setAendert(VerbundenerPlan verbundenerPlan) {
        this.aendert = new PGVerbundenerPlan[] {new PGVerbundenerPlan(verbundenerPlan)};
    }
    public void setWurdeGeaendertVon(VerbundenerPlan verbundenerPlan) {
        this.wurdegeaendertvon = new PGVerbundenerPlan[] {new PGVerbundenerPlan(verbundenerPlan)};
    }    
    
    public String getVerfahren() {
        return verfahren;
    }
    public void setVerfahren(String verfahren) {
        this.verfahren = verfahren;
    }    
    
    public LocalDate getVeraenderungssperrebeschlussdatum() {
        return veraenderungssperrebeschlussdatum;
    }
    public void setVeraenderungssperrebeschlussdatum(LocalDate veraenderungssperrebeschlussdatum) {
        this.veraenderungssperrebeschlussdatum = veraenderungssperrebeschlussdatum;
    }    
    
    public LocalDate getVeraenderungssperredatum() {
        return veraenderungssperredatum;
    }
    public void setVeraenderungssperredatum(LocalDate veraenderungssperredatum) {
        this.veraenderungssperredatum = veraenderungssperredatum;
    }
    
    public LocalDate getVeraenderungssperreenddatum() {
        return veraenderungssperreenddatum;
    }
    public void setVeraenderungssperreenddatum(LocalDate veraenderungssperreenddatum) {
        this.veraenderungssperreenddatum = veraenderungssperreenddatum;
    }    
    
    public String getVerlaengerungveraenderungssperre() {
        return verlaengerungveraenderungssperre;
    }
    public void setVerlaengerungveraenderungssperre(String verlaengerungveraenderungssperre) {
        this.verlaengerungveraenderungssperre = verlaengerungveraenderungssperre;
    }    
    public LocalDate getSatzungsbeschlussdatum() {
        return satzungsbeschlussdatum;
    }
    public void setSatzungsbeschlussdatum(LocalDate satzungsbeschlussdatum) {
        this.satzungsbeschlussdatum = satzungsbeschlussdatum;
    }
    public LocalDate getAufstellungsbeschlussdatum() {
        return aufstellungsbeschlussdatum;
    }
    public void setAufstellungsbeschlussdatum(LocalDate aufstellungsbeschlussdatum) {
        this.aufstellungsbeschlussdatum = aufstellungsbeschlussdatum;
    }
    public LocalDate getGenehmigungsdatum() {
        return genehmigungsdatum;
    }
    public void setGenehmigungsdatum(LocalDate genehmigungsdatum) {
        this.genehmigungsdatum = genehmigungsdatum;
    }    
    public LocalDate getUntergangsdatum() {
        return untergangsdatum;
    }
    public void setUntergangsdatum(LocalDate untergangsdatum) {
        this.untergangsdatum = untergangsdatum;
    }
    
    public LocalDate[] getTraegerbeteiligungsstartdatum() {
        return traegerbeteiligungsstartdatum;
    }
    public void setTraegerbeteiligungsstartdatum(LocalDate traegerbeteiligungsstartdatum) {
        this.traegerbeteiligungsstartdatum = new LocalDate[] {traegerbeteiligungsstartdatum};
    }
    public void setTraegerbeteiligungsstartdatum(LocalDate[] traegerbeteiligungsstartdatum) {
        this.traegerbeteiligungsstartdatum = traegerbeteiligungsstartdatum;
    }
    
    
    public LocalDate[] getTraegerbeteiligungsenddatum() {
        return traegerbeteiligungsenddatum;
    }
    public void setTraegerbeteiligungsenddatum(LocalDate traegerbeteiligungsenddatum) {
        this.traegerbeteiligungsenddatum = new LocalDate[] {traegerbeteiligungsenddatum};
    }
    public void setTraegerbeteiligungsenddatum(LocalDate[] traegerbeteiligungsenddatum) {
        this.traegerbeteiligungsenddatum = traegerbeteiligungsenddatum;
    }
    
    public LocalDate getVeroeffentlichungsdatum() {
        return veroeffentlichungsdatum;
    }
    public void setVeroeffentlichungsdatum(LocalDate veroeffentlichungsdatum) {
        this.veroeffentlichungsdatum = veroeffentlichungsdatum;
    }
    
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(externeReferenzes);
        result = prime * result + Arrays.hashCode(gemeinde);
        result = prime * result + ((geom == null) ? 0 : geom.hashCode());
        result = prime * result + ((gml_id == null) ? 0 : gml_id.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((inkrafttretensdatum == null) ? 0 : inkrafttretensdatum.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((nummer == null) ? 0 : nummer.hashCode());
        result = prime * result + Arrays.hashCode(planart);
        result = prime * result + ((rechtsstand == null) ? 0 : rechtsstand.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BPlan other = (BPlan) obj;
        if (!Arrays.equals(externeReferenzes, other.externeReferenzes)) {      
            if (externeReferenzes!=null && other.externeReferenzes!=null) {
                if (externeReferenzes.length!=other.externeReferenzes.length) {
                    System.err.println("false02.1");
                    return false;
                }
                for (int i=0; i<externeReferenzes.length; i++) {
                    if (!externeReferenzes[i].equals(other.externeReferenzes[i])) {
                        System.err.println("false02");
                        return false;
                    }
                }
            }
        }
        if (!Arrays.equals(gemeinde, other.gemeinde)) {
            System.err.println("false02");
            return false;
        }
        if (geom == null) {
            if (other.geom != null)
                return false;
        } else if (!geom.equals(other.geom)) {
            System.err.println("geom false");
            return false;
        }
        if (gml_id == null) {
            if (other.gml_id != null)
                return false;
        } else if (!gml_id.equals(other.gml_id))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (inkrafttretensdatum == null) {
            if (other.inkrafttretensdatum != null)
                return false;
        } else if (!inkrafttretensdatum.equals(other.inkrafttretensdatum))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (nummer == null) {
            if (other.nummer != null)
                return false;
        } else if (!nummer.equals(other.nummer))
            return false;
        if (!Arrays.equals(planart, other.planart))
            return false;
        if (rechtsstand == null) {
            if (other.rechtsstand != null)
                return false;
        } else if (!rechtsstand.equals(other.rechtsstand))
            return false;
        return true;
    }
    
        
    

    @Override
    public String toString() {
        return "BPlan [id=" + id + ", name=\"" + name + "\", gml_id=" + gml_id + ", nummer=" + nummer + ", planart="
                + Arrays.toString(planart) + ", rechtsstand=" + rechtsstand + ", inkrafttretensdatum=" + inkrafttretensdatum
                + ", gemeinde=" + Arrays.toString(gemeinde) + ", externereferenz=" + Arrays.toString(externeReferenzes) + ", geom=" + geom + "]";
    }
    


    static void print(BPlan bplan) {
        /*



//    Date veroeffentlichungsdatum;    
//    Boolean erschliessungsvertrag = null;
//    Boolean durchfuehrungsvertrag = null;
//    Boolean gruenordnungsplan = null;
//    Boolean staedtebaulichervertrag = null;

//    Gemeinde[] gemeinde; // "{"ags" : "13072072", "rs" : "130725260072", "gemeindename" : "Mönchhagen", "ortsteilname" : "Mönchhagen"}"
//    PGExterneReferenz[] externeReferenzes; // ="[{"georefurl" : null, "georefmimetype" : null, "art" : "Dokument", "informationssystemurl" : "https://www.amt-rostocker-heide.de/amt-rostocker-heide/Geo-Daten-Amt-Rostocker-Heide/", "referenzname" : "amt_rostocker_heide_moenchhagen_bplan_3_1_1_1.pdf", "referenzurl" : "https://service.btfietz.de/wmsdata/amt_rostocker_heide/amt_rostocker_heide_moenchhagen_bplan_3_1_1_1.pdf", "referenzmimetype" : {"codespace" : "https://bauleitplaene-mv.de/codelist/XP_MimeTypes.xml", "id" : "application/pdf", "value" : "application/pdf"}, "beschreibung" : "Satzung über 1. Änderung des Bebauungsplans (438,49 KB)", "datum" : "2003-10-07", "typ" : "1060"}, {"georefurl" : null, "georefmimetype" : null, "art" : "Dokument", "informationssystemurl" : "https://www.amt-rostocker-heide.de/amt-rostocker-heide/Geo-Daten-Amt-Rostocker-Heide/", "referenzname" : "amt_rostocker_heide_moenchhagen_bplan_3_1_2_1.pdf", "referenzurl" : "https://service.btfietz.de/wmsdata/amt_rostocker_heide/amt_rostocker_heide_moenchhagen_bplan_3_1_2_1.pdf", "referenzmimetype" : {"codespace" : "https://bauleitplaene-mv.de/codelist/XP_MimeTypes.xml", "id" : "application/pdf", "value" : "application/pdf"}, "beschreibung" : "Satzung über 2. Änderung des Bebauungsplans (2,47 MB)", "datum" : "2012-04-03", "typ" : "1060"}, {"georefurl" : null, "georefmimetype" : null, "art" : "Dokument", "informationssystemurl" : "https://www.amt-rostocker-heide.de/amt-rostocker-heide/Geo-Daten-Amt-Rostocker-Heide/", "referenzname" : "amt_rostocker_heide_moenchhagen_bplan_3_1_3_1.pdf", "referenzurl" : "https://service.btfietz.de/wmsdata/amt_rostocker_heide/amt_rostocker_heide_moenchhagen_bplan_3_1_3_1.pdf", "referenzmimetype" : {"codespace" : "https://bauleitplaene-mv.de/codelist/XP_MimeTypes.xml", "id" : "application/pdf", "value" : "application/pdf"}, "beschreibung" : "Satzung über 3. Änderung des Bebauungsplans (2,3 MB)", "datum" : "2017-12-01", "typ" : "1060"}, {"georefurl" : null, "georefmimetype" : null, "art" : "Dokument", "informationssystemurl" : "https://www.amt-rostocker-heide.de/amt-rostocker-heide/Geo-Daten-Amt-Rostocker-Heide/", "referenzname" : "amt_rostocker_heide_moenchhagen_bplan_3_1_3_2.pdf", "referenzurl" : "https://service.btfietz.de/wmsdata/amt_rostocker_heide/amt_rostocker_heide_moenchhagen_bplan_3_1_3_2.pdf", "referenzmimetype" : {"codespace" : "https://bauleitplaene-mv.de/codelist/XP_MimeTypes.xml", "id" : "application/pdf", "value" : "application/pdf"}, "beschreibung" : "Begründung zur 3. Änderung des Bebauungsplans (186,38 KB)", "datum" : "2017-12-01", "typ" : "1010"}]"
//    Geometry geom; // f.getDefaultGeometry()="POLYGON ((318577.09 6003723.31, 318578.06 6003724.87, 318586.83 6003734.74, 318592.79 6003742.57, 318597.54 6003751.46, 318607.22 6003771.9, 318616.56 6003791.61, 318625.9 6003811.31, 318630.57 6003821.16, 318635.24 6003831.01, 318644.58 6003850.71, 318651.44 6003865.19, 318662.59 6003888.51, 318672.32 6003909.05, 318717.18 6004003.81, 318720.7 6004009.51, 318725.38 6004007.67, 318726.88 6004007.11, 318729.52 6004006.12, 318752.86 6003997.38, 318742.62 6003975.75, 318733.81 6003957.13, 318724.99 6003938.53, 318716.19 6003919.91, 318706.08 6003898.58, 318717.52 6003893.16, 318739.18 6003882.9, 318747.08 6003879.15, 318767.53 6003869.45, 318787.41 6003860.03, 318788.7 6003859.42, 318792.47 6003857.44, 318797.27 6003854.39, 318801.8 6003850.97, 318806.04 6003847.18, 318809.95 6003843.07, 318813.51 6003838.64, 318817.31 6003832.99, 318820.51 6003826.98, 318823.11 6003820.7, 318915.34 6003877.92, 318954.47 6003863.19, 318907.6 6003737.12, 318884.84 6003675.88, 318893.12 6003672.84, 318939 6003655.64, 318940.76 6003654.98, 318953.36 6003689.18, 318956.33 6003688.16, 318958.46 6003687.43, 318955.87 6003680.41, 318940.77 6003639.54, 318937.65 6003639.65, 318936.16 6003639.7, 318934.56 6003638.84, 318929.99 6003627.13, 318930.52 6003625.22, 318931.61 6003624.47, 318934.51 6003622.46, 318933.55 6003619.89, 318925.87 6003599.47, 318924.44 6003595.68, 318921.07 6003586.42, 318920.25 6003586.73, 318868.17 6003606.23, 318816.09 6003623.51, 318815.2 6003621.05, 318814.82 6003620.02, 318809.06 6003604.16, 318807.02 6003602.11, 318805.72 6003598.63, 318800.68 6003585.23, 318804.47 6003583.66, 318801.22 6003574.74, 318790.99 6003546.13, 318789.61 6003542.25, 318785.12 6003543.85, 318765.16 6003554.22, 318744.37 6003565.02, 318723.05 6003576.1, 318703.51 6003587.27, 318691.67 6003594.22, 318671.82 6003605.86, 318653.24 6003616.76, 318635.67 6003637.94, 318626.96 6003648.43, 318620.57 6003658, 318607.66 6003677.31, 318583.05 6003714.13, 318577.09 6003723.31))"
//    String internalid;
//    PGVerbundenerPlan[] aendert;
//    PGVerbundenerPlan[] wurdegeaendertvon;
//    CodeList status;   
    PlanaufstellendeGemeinde[] planaufstellendegemeinde;
        Date[] auslegungsstartdatum;
    Date[] auslegungsenddatum;
    Date[] traegerbeteiligungsStartDatum;    
    Date veraenderungssperrebeschlussdatum;
    Date veraenderungssperredatum;
    Date veraenderungssperreenddatum;
    String verlaengerungveraenderungssperre;
    Date satzungsbeschlussdatum;    
    Date aufstellungsbeschlussdatum;
    Date traegerbeteiligungsstartdatum;
    Date traegerbeteiligungsenddatum;
    Date genehmigungsdatum;
    Date untergangsdatum;
         */
        
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        System.err.println("id="+bplan.id);
        System.err.println("name="+bplan.name);
        System.err.println("gml_id="+bplan.gml_id);
        System.err.println("nummer="+bplan.nummer);
        System.err.println("status="+bplan.status);
        
        System.err.println("aendert="+Arrays.toString(bplan.aendert));
        System.err.println("planart="+Arrays.toString(bplan.planart));
        System.err.println("gemeinde="+Arrays.toString(bplan.gemeinde));
        System.err.println("verfahren="+bplan.verfahren);
        System.err.println("internalid="+bplan.internalid);       
        
        System.err.println("rechtsstand="+bplan.rechtsstand);
        if (bplan.externeReferenzes != null && bplan.externeReferenzes.length>0) {
            for (int i=0; i<bplan.externeReferenzes.length; i++) {
                System.err.println("externeReferenzes"+i+"="+bplan.externeReferenzes[i]);
            }
        } else {
            System.err.println("externeReferenzes=\"\"");
        }
        
        
        System.err.println("untergangsdatum="+bplan.untergangsdatum);        
        System.err.println("genehmigungsdatum="+bplan.genehmigungsdatum);
        System.err.println("gruenordnungsplan="+bplan.gruenordnungsplan);
        System.err.println("wurdegeaendertvon="+Arrays.toString(bplan.wurdegeaendertvon)); 
        
        System.err.println("ausfertigungsdatum="+bplan.ausfertigungsdatum);
        System.err.println("auslegungsenddatum="+Arrays.toString(bplan.auslegungsenddatum));
        System.err.println("inkrafttretensdatum="+bplan.inkrafttretensdatum);
        System.err.println("auslegungsstartdatum="+Arrays.toString(bplan.auslegungsstartdatum));

        System.err.println("durchfuehrungsvertrag="+bplan.durchfuehrungsvertrag);
        System.err.println("erschliessungsvertrag="+bplan.erschliessungsvertrag);
        System.err.println("rechtsverordnungsdatum="+bplan.rechtsverordnungsdatum);
        System.err.println("satzungsbeschlussdatum="+bplan.satzungsbeschlussdatum);
        System.err.println("staedtebaulichervertrag="+bplan.staedtebaulichervertrag);
        System.err.println("veroeffentlichungsdatum="+bplan.veroeffentlichungsdatum);
        
        System.err.println("planaufstellendegemeinde="+Arrays.toString(bplan.planaufstellendegemeinde));
        System.err.println("veraenderungssperredatum="+bplan.veraenderungssperredatum);
        System.err.println("aufstellungsbeschlussdatum="+bplan.aufstellungsbeschlussdatum);
        System.err.println("traegerbeteiligungsenddatum="+Arrays.toString(bplan.traegerbeteiligungsenddatum));
        System.err.println("veraenderungssperreenddatum="+bplan.veraenderungssperreenddatum);
        // rechtsver

        System.err.println("geom="+bplan.geom.getGeometryType());
        
        System.err.println("traegerbeteiligungsstartdatum="+Arrays.toString(bplan.traegerbeteiligungsstartdatum));
        System.err.println("verlaengerungveraenderungssperre="+bplan.verlaengerungveraenderungssperre);
        System.err.println("veraenderungssperrebeschlussdatum="+bplan.veraenderungssperrebeschlussdatum);
        
        System.err.println("traegerbeteiligungsStartDatum="+Arrays.toString(bplan.traegerbeteiligungsStartDatum));    

        System.err.println("konvertierung_id="+bplan.konvertierung_id);
    }
    
    
    static String print(SimpleDateFormat sdf, Date date) {
        if (date==null) {
            return "";
        }
        return sdf.format(date);
    }

    static String toString(Date[] dates) {
        if (dates == null || dates.length==0) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        StringBuilder sb = new StringBuilder(sdf.format(dates[0]));
        for (int i=1; i<dates.length; i++) {
            sb.append(", ").append(sdf.format(dates[i]));
        }
        return sb.toString();
    }


}
