package de.gdiservice.bplan;

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
    Date inkrafttretensdatum; //="2018-01-05"
    Gemeinde[] gemeinde; // "{"ags" : "13072072", "rs" : "130725260072", "gemeindename" : "Mönchhagen", "ortsteilname" : "Mönchhagen"}"
    PGExterneReferenz[] externeReferenzes; // ="[{"georefurl" : null, "georefmimetype" : null, "art" : "Dokument", "informationssystemurl" : "https://www.amt-rostocker-heide.de/amt-rostocker-heide/Geo-Daten-Amt-Rostocker-Heide/", "referenzname" : "amt_rostocker_heide_moenchhagen_bplan_3_1_1_1.pdf", "referenzurl" : "https://service.btfietz.de/wmsdata/amt_rostocker_heide/amt_rostocker_heide_moenchhagen_bplan_3_1_1_1.pdf", "referenzmimetype" : {"codespace" : "https://bauleitplaene-mv.de/codelist/XP_MimeTypes.xml", "id" : "application/pdf", "value" : "application/pdf"}, "beschreibung" : "Satzung über 1. Änderung des Bebauungsplans (438,49 KB)", "datum" : "2003-10-07", "typ" : "1060"}, {"georefurl" : null, "georefmimetype" : null, "art" : "Dokument", "informationssystemurl" : "https://www.amt-rostocker-heide.de/amt-rostocker-heide/Geo-Daten-Amt-Rostocker-Heide/", "referenzname" : "amt_rostocker_heide_moenchhagen_bplan_3_1_2_1.pdf", "referenzurl" : "https://service.btfietz.de/wmsdata/amt_rostocker_heide/amt_rostocker_heide_moenchhagen_bplan_3_1_2_1.pdf", "referenzmimetype" : {"codespace" : "https://bauleitplaene-mv.de/codelist/XP_MimeTypes.xml", "id" : "application/pdf", "value" : "application/pdf"}, "beschreibung" : "Satzung über 2. Änderung des Bebauungsplans (2,47 MB)", "datum" : "2012-04-03", "typ" : "1060"}, {"georefurl" : null, "georefmimetype" : null, "art" : "Dokument", "informationssystemurl" : "https://www.amt-rostocker-heide.de/amt-rostocker-heide/Geo-Daten-Amt-Rostocker-Heide/", "referenzname" : "amt_rostocker_heide_moenchhagen_bplan_3_1_3_1.pdf", "referenzurl" : "https://service.btfietz.de/wmsdata/amt_rostocker_heide/amt_rostocker_heide_moenchhagen_bplan_3_1_3_1.pdf", "referenzmimetype" : {"codespace" : "https://bauleitplaene-mv.de/codelist/XP_MimeTypes.xml", "id" : "application/pdf", "value" : "application/pdf"}, "beschreibung" : "Satzung über 3. Änderung des Bebauungsplans (2,3 MB)", "datum" : "2017-12-01", "typ" : "1060"}, {"georefurl" : null, "georefmimetype" : null, "art" : "Dokument", "informationssystemurl" : "https://www.amt-rostocker-heide.de/amt-rostocker-heide/Geo-Daten-Amt-Rostocker-Heide/", "referenzname" : "amt_rostocker_heide_moenchhagen_bplan_3_1_3_2.pdf", "referenzurl" : "https://service.btfietz.de/wmsdata/amt_rostocker_heide/amt_rostocker_heide_moenchhagen_bplan_3_1_3_2.pdf", "referenzmimetype" : {"codespace" : "https://bauleitplaene-mv.de/codelist/XP_MimeTypes.xml", "id" : "application/pdf", "value" : "application/pdf"}, "beschreibung" : "Begründung zur 3. Änderung des Bebauungsplans (186,38 KB)", "datum" : "2017-12-01", "typ" : "1010"}]"
    Geometry geom; // f.getDefaultGeometry()="POLYGON ((318577.09 6003723.31, 318578.06 6003724.87, 318586.83 6003734.74, 318592.79 6003742.57, 318597.54 6003751.46, 318607.22 6003771.9, 318616.56 6003791.61, 318625.9 6003811.31, 318630.57 6003821.16, 318635.24 6003831.01, 318644.58 6003850.71, 318651.44 6003865.19, 318662.59 6003888.51, 318672.32 6003909.05, 318717.18 6004003.81, 318720.7 6004009.51, 318725.38 6004007.67, 318726.88 6004007.11, 318729.52 6004006.12, 318752.86 6003997.38, 318742.62 6003975.75, 318733.81 6003957.13, 318724.99 6003938.53, 318716.19 6003919.91, 318706.08 6003898.58, 318717.52 6003893.16, 318739.18 6003882.9, 318747.08 6003879.15, 318767.53 6003869.45, 318787.41 6003860.03, 318788.7 6003859.42, 318792.47 6003857.44, 318797.27 6003854.39, 318801.8 6003850.97, 318806.04 6003847.18, 318809.95 6003843.07, 318813.51 6003838.64, 318817.31 6003832.99, 318820.51 6003826.98, 318823.11 6003820.7, 318915.34 6003877.92, 318954.47 6003863.19, 318907.6 6003737.12, 318884.84 6003675.88, 318893.12 6003672.84, 318939 6003655.64, 318940.76 6003654.98, 318953.36 6003689.18, 318956.33 6003688.16, 318958.46 6003687.43, 318955.87 6003680.41, 318940.77 6003639.54, 318937.65 6003639.65, 318936.16 6003639.7, 318934.56 6003638.84, 318929.99 6003627.13, 318930.52 6003625.22, 318931.61 6003624.47, 318934.51 6003622.46, 318933.55 6003619.89, 318925.87 6003599.47, 318924.44 6003595.68, 318921.07 6003586.42, 318920.25 6003586.73, 318868.17 6003606.23, 318816.09 6003623.51, 318815.2 6003621.05, 318814.82 6003620.02, 318809.06 6003604.16, 318807.02 6003602.11, 318805.72 6003598.63, 318800.68 6003585.23, 318804.47 6003583.66, 318801.22 6003574.74, 318790.99 6003546.13, 318789.61 6003542.25, 318785.12 6003543.85, 318765.16 6003554.22, 318744.37 6003565.02, 318723.05 6003576.1, 318703.51 6003587.27, 318691.67 6003594.22, 318671.82 6003605.86, 318653.24 6003616.76, 318635.67 6003637.94, 318626.96 6003648.43, 318620.57 6003658, 318607.66 6003677.31, 318583.05 6003714.13, 318577.09 6003723.31))"

    String internalid;
    PGVerbundenerPlan[] aendert;
    PGVerbundenerPlan[] wurdegeaendertvon;    
    
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
    public Date getInkrafttretensdatum() {
        return inkrafttretensdatum;
    }
    public void setInkrafttretensdatum(Date inkrafttretensdatum) {
        this.inkrafttretensdatum = inkrafttretensdatum;
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
            this.externeReferenzes = new PGExterneReferenz[] {pgExterneReferenz};
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
        return "BPlan [id=" + id + ", name=" + name + ", gml_id=" + gml_id + ", nummer=" + nummer + ", planart="
                + planart + ", rechtsstand=" + rechtsstand + ", inkrafttretensdatum=" + inkrafttretensdatum
                + ", gemeinde=" + gemeinde + ", externereferenz=" + Arrays.toString(externeReferenzes) + ", geom=" + geom + "]";
    }
    








}
