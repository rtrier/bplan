package de.gdiservice.bplan.poi;

import java.io.Serializable;
import java.sql.SQLException;


import org.postgresql.util.PGobject;
import org.postgresql.util.PGtokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gdiservice.bplan.PGUtil;


public class Gemeinde extends PGobject implements Serializable, Cloneable {

    final static Logger logger = LoggerFactory.getLogger(Gemeinde.class);
    
    public Gemeinde()  {}

    public Gemeinde(Object object) throws SQLException {
        if (object instanceof PGobject) {
            this.setValue( ((PGobject)object).getValue() );
        }
    }

    private static final long serialVersionUID = 1L;

    public String ags; //"13072072", "
    public String rs; // " : "130725260072", ""
    public String gemeindename;
    public String ortsteilname;		


    @Override
    public void setValue(String value) throws SQLException {
//        logger.debug("Gemeinde.setValue(\""+value+"\")");
        String sqlString = PGtokenizer.removePara(PGtokenizer.removeCurlyBrace(value));
        PGtokenizer t = new PGtokenizer( sqlString, ',');

        ags  = PGUtil.trim(t.getToken(0));
        rs  = PGUtil.trim(t.getToken(1));
        gemeindename = PGUtil.trim(t.getToken(2));
//        if (gemeindename.startsWith("\"")) {
//            gemeindename = gemeindename.substring(1);
//        }
//        if (gemeindename.endsWith("\"")) {
//            gemeindename = gemeindename.substring(0, gemeindename.length()-1);
//        }
        // ortsteilname = t.getToken(3);
        String sOrtsteilname = PGUtil.trim(t.getToken(3)); 
        ortsteilname = sOrtsteilname !=null && sOrtsteilname.length()>0 ? sOrtsteilname : null;
        
        this.value = value;
    }	

    
    public String getValue() {         
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        if (this.ags != null) {
            sb.append(this.ags);
        }
        sb.append(",");
        if (this.rs != null) {
            sb.append(this.rs);
        }
        sb.append(",\"");
        if (this.gemeindename != null) {
            sb.append(this.gemeindename);
        }
        sb.append("\",\"");
        if (this.ortsteilname!= null && this.ortsteilname.length()>0) {
            sb.append(this.ortsteilname);
        }
        sb.append("\")");
//        final String v = "(" + this.ags  + "," + this.rs + ",\"" + this.gemeindename + "\",\"" + this.ortsteilname+ "\")";
//        return v;
        return sb.toString();
    }	

    public String getValueFail() {         
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        if (this.ags != null) {
            sb.append(this.ags);
        }
        sb.append(",");
        if (this.rs != null) {
            sb.append(this.rs);
        }
        sb.append(",");
        if (this.gemeindename != null) {
            sb.append(this.gemeindename);
        }
        sb.append(",");
        if (this.ortsteilname!= null) {
            sb.append(this.ortsteilname);
        }
        sb.append(")");
//        final String v = "(" + this.ags  + "," + this.rs + ",\"" + this.gemeindename + "\",\"" + this.ortsteilname+ "\")";
//        return v;
        return sb.toString();
    }   



    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((ags == null) ? 0 : ags.hashCode());
        result = prime * result + ((gemeindename == null) ? 0 : gemeindename.hashCode());
        result = prime * result + ((ortsteilname == null) ? 0 : ortsteilname.hashCode());
        result = prime * result + ((rs == null) ? 0 : rs.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        Gemeinde other = (Gemeinde) obj;
        if (ags == null) {
            if (other.ags != null)
                return false;
        } else if (!ags.equals(other.ags))
            return false;
        if (gemeindename == null) {
            if (other.gemeindename != null)
                return false;
        } else if (!gemeindename.equals(other.gemeindename))
            return false;
        if (ortsteilname == null) {
            if (other.ortsteilname != null)
                return false;
        } else if (!ortsteilname.equals(other.ortsteilname))
            return false;
        if (rs == null) {
            if (other.rs != null)
                return false;
        } else if (!rs.equals(other.rs))
            return false;
        return true;
    }

    public Gemeinde clone() throws CloneNotSupportedException {
        return (Gemeinde)super.clone();
    }	

    public String getAgs() {
        return ags;
    }
    public void setAgs(String ags) {
        this.ags = ags;
    }
    public String getRs() {
        return rs;
    }
    public void setRs(String rs) {
        this.rs = rs;
    }
    public String getGemeindename() {
        return gemeindename;
    }
    public void setGemeindename(String gemeindename) {
        this.gemeindename = gemeindename;
    }
    public String getOrtsteilname() {
        return ortsteilname;
    }
    public void setOrtsteilname(String ortsteilname) {
        this.ortsteilname = ortsteilname;
    }
    @Override
    public String toString() {
        return getValue();
    }



}


