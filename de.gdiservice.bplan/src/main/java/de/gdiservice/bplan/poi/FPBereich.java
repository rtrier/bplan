package de.gdiservice.bplan.poi;

import java.time.LocalDate;
import java.util.Arrays;

public class FPBereich extends XPBereich<FPlan> {
    
    public LocalDate versionbaunvodatum;
    public String versionbaugbtext;
    public String versionsonstrechtsgrundlagetext;
    public String versionbaunvotext;
    public LocalDate versionsonstrechtsgrundlagedatum;
    public LocalDate versionbaugbdatum;
    
    public LocalDate getVersionbaunvodatum() {
        return versionbaunvodatum;
    }

    public void setVersionbaunvodatum(LocalDate versionbaunvodatum) {
        this.versionbaunvodatum = versionbaunvodatum;
    }



    public String getVersionbaugbtext() {
        return versionbaugbtext;
    }



    public void setVersionbaugbtext(String versionbaugbtext) {
        this.versionbaugbtext = versionbaugbtext;
    }



    public String getVersionsonstrechtsgrundlagetext() {
        return versionsonstrechtsgrundlagetext;
    }



    public void setVersionsonstrechtsgrundlagetext(String versionsonstrechtsgrundlagetext) {
        this.versionsonstrechtsgrundlagetext = versionsonstrechtsgrundlagetext;
    }



    public String getVersionbaunvotext() {
        return versionbaunvotext;
    }



    public void setVersionbaunvotext(String versionbaunvotext) {
        this.versionbaunvotext = versionbaunvotext;
    }



    public LocalDate getVersionsonstrechtsgrundlagedatum() {
        return versionsonstrechtsgrundlagedatum;
    }



    public void setVersionsonstrechtsgrundlagedatum(LocalDate versionsonstrechtsgrundlagedatum) {
        this.versionsonstrechtsgrundlagedatum = versionsonstrechtsgrundlagedatum;
    }

    public LocalDate getVersionbaugbdatum() {
        return versionbaugbdatum;
    }

    public void setVersionbaugbdatum(LocalDate versionbaugbdatum) {
        this.versionbaugbdatum = versionbaugbdatum;
    }



    @Override    
    public String toString() {
        return "FPBereich [versionbaunvodatum=" + versionbaunvodatum + ", versionbaugbtext=" + versionbaugbtext
                + ", versionsonstrechtsgrundlagetext=" + versionsonstrechtsgrundlagetext + ", versionbaunvotext="
                + versionbaunvotext + ", versionsonstrechtsgrundlagedatum=" + versionsonstrechtsgrundlagedatum
                + ", versionbaugbdatum=" + versionbaugbdatum + ", gehoertzuplan=" + gehoertzuplan + ", gml_id=" + gml_id
                + ", nummer=" + nummer + ", name=" + name + ", bedeutung=" + bedeutung + ", detailliertebedeutung="
                + detailliertebedeutung + ", erstellungsmassstab=" + erstellungsmassstab + ", geltungsbereich="
                + geltungsbereich + ", user_id=" + user_id + ", created_at=" + created_at + ", updated_at=" + updated_at
                + ", konvertierung_id=" + konvertierung_id + ", planinhalt=" + planinhalt + ", praesentationsobjekt="
                + praesentationsobjekt + ", rasterbasis=" + rasterbasis + ", refscan=" + Arrays.toString(refscan) + "]";
    }
    
    


}
