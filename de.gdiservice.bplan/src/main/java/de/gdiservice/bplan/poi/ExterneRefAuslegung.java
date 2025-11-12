package de.gdiservice.bplan.poi;

public class ExterneRefAuslegung extends SpezExterneRef {
    
    public Boolean nurzurauslegung;

    public Boolean isNurzurauslegung() {
        return nurzurauslegung;
    }

    public void setNurzurauslegung(Boolean nurzurauslegung) {
        this.nurzurauslegung = nurzurauslegung;
    }
    
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExterneRefAuslegung other = (ExterneRefAuslegung) obj;
        if (art == null) {
            if (other.art != null)
                return false;
        } else if (!art.equals(other.art))
            return false;
        if (beschreibung == null) {
            if (other.beschreibung != null)
                return false;
        } else if (!beschreibung.equals(other.beschreibung))
            return false;
        if (datum == null) {
            if (other.datum != null)
                return false;        
        } else if (!datum.equals(other.datum)) {
            logger.debug(datum.getClass()+"  "+datum+"  "+datum);
            logger.debug(other.datum.getClass()+"  "+other.datum+"  "+other.datum);
            
            return false;
        }
        if (georefmimetype == null) {
            if (other.georefmimetype != null)
                return false;
        } else if (!georefmimetype.equals(other.georefmimetype))
            return false;
        if (georefurl == null) {
            if (other.georefurl != null)
                return false;
        } else if (!georefurl.equals(other.georefurl))
            return false;
        if (informationssystemurl == null) {
            if (other.informationssystemurl != null)
                return false;
        } else if (!informationssystemurl.equals(other.informationssystemurl))
            return false;
        if (referenzname == null) {
            if (other.referenzname != null)
                return false;
        } else if (!referenzname.equals(other.referenzname))
            return false;
        if (referenzurl == null) {
            if (other.referenzurl != null)
                return false;
        } else if (!referenzurl.equals(other.referenzurl))
            return false;
        if (typ == null) {
            if (other.typ != null)
                return false;
        } else if (!typ.equals(other.typ))
            return false;
        
        if (nurzurauslegung == null) {
            if (other.nurzurauslegung != null)
                return false;
        } else if (!nurzurauslegung.equals(other.nurzurauslegung))
            return false;
        return true;
    }
    @Override
    public String toString() {
        return "ExterneRef [georefurl=" + georefurl + ", georefmimetype=" + georefmimetype + ", art=" + art
                + ", informationssystemurl=" + informationssystemurl + ", referenzname=" + referenzname + ", referenzurl="
                + referenzurl + ", referenzmimetype=" + referenzmimetype + ", beschreibung=" + beschreibung + ", datum="
                + datum + ", typ=" + typ  + ", nurzurauslegung=" + nurzurauslegung + "]";
    }

}
