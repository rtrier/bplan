package de.gdiservice.bplan;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExterneRef {
    
    final static Logger logger = LoggerFactory.getLogger(ExterneRef.class);
  
  private static final long serialVersionUID = 1L;
  
  public String georefurl;
  public String georefmimetype;
  public String art;
  public String informationssystemurl;
  public String referenzname;
  public String referenzurl;
  public Referenzmimetype referenzmimetype;
  public String beschreibung;
  public Date datum;
  public String typ;
  public Boolean nurzurauslegung;
  
  
  public String getGeorefurl() {
    return georefurl;
  }
  public void setGeorefurl(String georefurl) {
    this.georefurl = georefurl;
  }
  public String getGeorefmimetype() {
    return georefmimetype;
  }
  public void setGeorefmimetype(String georefmimetype) {
    this.georefmimetype = georefmimetype;
  }
  public String getArt() {
    return art;
  }
  public void setArt(String art) {
    this.art = art;
  }
  public String getInformationssystemurl() {
    return informationssystemurl;
  }
  public void setInformationssystemurl(String informationssystemurl) {
    this.informationssystemurl = informationssystemurl;
  }
  public String getReferenzname() {
    return referenzname;
  }
  public void setReferenzname(String referenzname) {
    this.referenzname = referenzname;
  }
  public String getReferenzurl() {
    return referenzurl;
  }
  public void setReferenzurl(String referenzurl) {
    this.referenzurl = referenzurl;
  }
  public Referenzmimetype getReferenzmimetype() {
    return referenzmimetype;
  }
  public void setReferenzmimetype(Referenzmimetype referenzmimetype) {
    this.referenzmimetype = referenzmimetype;
  }
  public String getBeschreibung() {
    return beschreibung;
  }
  public void setBeschreibung(String beschreibung) {
    this.beschreibung = beschreibung;
  }
  public Date getDatum() {
    return datum;
  }
  public void setDatum(Date datum) {
    this.datum = datum;
  }
  public String getTyp() {
    return typ;
  }
  public void setTyp(String typ) {
    this.typ = typ;
  }
  public Boolean isNurzurauslegung() {
      return nurzurauslegung;
  }
  public void setNurzurauslegung(Boolean nurzurauslegung) {
      this.nurzurauslegung = nurzurauslegung;
  }  
  
  
  
  public static long getSerialversionuid() {
    return serialVersionUID;
  }
@Override
public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((art == null) ? 0 : art.hashCode());
    result = prime * result + ((beschreibung == null) ? 0 : beschreibung.hashCode());
    result = prime * result + ((datum == null) ? 0 : datum.hashCode());
    result = prime * result + ((georefmimetype == null) ? 0 : georefmimetype.hashCode());
    result = prime * result + ((georefurl == null) ? 0 : georefurl.hashCode());
    result = prime * result + ((informationssystemurl == null) ? 0 : informationssystemurl.hashCode());
    result = prime * result + ((referenzmimetype == null) ? 0 : referenzmimetype.hashCode());
    result = prime * result + ((referenzname == null) ? 0 : referenzname.hashCode());
    result = prime * result + ((referenzurl == null) ? 0 : referenzurl.hashCode());
    result = prime * result + ((typ == null) ? 0 : typ.hashCode());
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
    ExterneRef other = (ExterneRef) obj;
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
        logger.debug(datum.getClass()+"  "+datum.getTime()+"  "+datum);
        logger.debug(other.datum.getClass()+"  "+other.datum.getTime()+"  "+other.datum);
        
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
    return true;
}
@Override
public String toString() {
    return "ExterneRef [georefurl=" + georefurl + ", georefmimetype=" + georefmimetype + ", art=" + art
            + ", informationssystemurl=" + informationssystemurl + ", referenzname=" + referenzname + ", referenzurl="
            + referenzurl + ", referenzmimetype=" + referenzmimetype + ", beschreibung=" + beschreibung + ", datum="
            + datum + ", typ=" + typ + "]";
}
  
  



}


