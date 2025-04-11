package de.gdiservice.bplan.poi;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import org.locationtech.jts.geom.Geometry;

import de.gdiservice.dao.KeyHolder;

public class XPBereich<P extends XPPlan> implements KeyHolder<UUID>{
    
    public UUID gml_id;
    public Integer nummer;
    public String name;
    public String bedeutung; // Integer 
    public String detailliertebedeutung;
    public Integer erstellungsmassstab;
    public Geometry geltungsbereich;
    public Integer user_id;
    public LocalDate created_at;
    public LocalDate updated_at;
    public Integer konvertierung_id;
    public String planinhalt;
    public String praesentationsobjekt;
    public String rasterbasis;
    public PGExterneReferenz[] refscan;
    
    UUID gehoertzuplan;
    
    public UUID getGml_id() {
        return gml_id;
    }
    public void setGml_id(UUID gml_id) {
        this.gml_id = gml_id;
    }
    public Integer getNummer() {
        return nummer;
    }
    public void setNummer(Integer nummer) {
        this.nummer = nummer;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getBedeutung() {
        return bedeutung;
    }
    public void setBedeutung(String bedeutung) {
        this.bedeutung = bedeutung;
    }
    public String getDetailliertebedeutung() {
        return detailliertebedeutung;
    }
    public void setDetailliertebedeutung(String detailliertebedeutung) {
        this.detailliertebedeutung = detailliertebedeutung;
    }
    public Integer getErstellungsmassstab() {
        return erstellungsmassstab;
    }
    public void setErstellungsmassstab(Integer erstellungsmassstab) {
        this.erstellungsmassstab = erstellungsmassstab;
    }
    public Geometry getGeltungsbereich() {
        return geltungsbereich;
    }
    public void setGeltungsbereich(Geometry geltungsbereich) {
        this.geltungsbereich = geltungsbereich;
    }
    public Integer getUser_id() {
        return user_id;
    }
    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }
    public LocalDate getCreated_at() {
        return created_at;
    }
    public void setCreated_at(LocalDate created_at) {
        this.created_at = created_at;
    }
    public LocalDate getUpdated_at() {
        return updated_at;
    }
    public void setUpdated_at(LocalDate updated_at) {
        this.updated_at = updated_at;
    }
    public Integer getKonvertierung_id() {
        return konvertierung_id;
    }
    public void setKonvertierung_id(Integer konvertierung_id) {
        this.konvertierung_id = konvertierung_id;
    }
    public String getPlaninhalt() {
        return planinhalt;
    }
    public void setPlaninhalt(String planinhalt) {
        this.planinhalt = planinhalt;
    }
    public String getPraesentationsobjekt() {
        return praesentationsobjekt;
    }
    public void setPraesentationsobjekt(String praesentationsobjekt) {
        this.praesentationsobjekt = praesentationsobjekt;
    }
    public String getRasterbasis() {
        return rasterbasis;
    }
    public void setRasterbasis(String rasterbasis) {
        this.rasterbasis = rasterbasis;
    }
    public PGExterneReferenz[] getRefscan() {
        return refscan;
    }
    public void setRefscan(PGExterneReferenz[] refscan) {
        this.refscan = refscan;
    }
    
    public UUID getGehoertzuplan() {
        return gehoertzuplan;
    }

    public void setGehoertzuplan(UUID gehoertzuplan) {
        this.gehoertzuplan = gehoertzuplan;
    }
    @Override
    public UUID getKey() {
        return gml_id;
    }
    @Override
    public String toString() {
        return "XPBereich [gml_id=" + gml_id + ", nummer=" + nummer + ", name=" + name + ", bedeutung=" + bedeutung
                + ", detailliertebedeutung=" + detailliertebedeutung + ", erstellungsmassstab=" + erstellungsmassstab
                + ", geltungsbereich=" + geltungsbereich + ", user_id=" + user_id + ", created_at=" + created_at
                + ", updated_at=" + updated_at + ", konvertierung_id=" + konvertierung_id + ", planinhalt=" + planinhalt
                + ", praesentationsobjekt=" + praesentationsobjekt + ", rasterbasis=" + rasterbasis + ", refscan="
                + Arrays.toString(refscan) + ", gehoertzuplan=" + gehoertzuplan + "]";
    }
    
//    LocalDate versionbaunvodatum;
//    String versionbaugbtext;
//    String versionsonstrechtsgrundlagetext;
//    String versionbaunvotext;
//    LocalDate versionsonstrechtsgrundlagedatum;
//    LocalDate versionbaugbdatum;
//    UUID gehoertzuplan;
    
    


}
