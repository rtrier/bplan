package de.gdiservice.bplan.poi;

import java.io.Serializable;
import java.sql.SQLException;
import java.text.ParseException;

import org.postgresql.util.PGobject;
import org.postgresql.util.PGtokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gdiservice.bplan.PGUtil;


public class PGExterneReferenz extends PGobject implements Serializable, Cloneable {
    
    final static Logger logger = LoggerFactory.getLogger(PGExterneReferenz.class);
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  
  ExterneRef object;
  
  
  public PGExterneReferenz() {
    
  }
  
  public PGExterneReferenz(ExterneRef externeRef) {
    this.object = externeRef;
  }
  
    
	
	
	@Override
	public void setValue(String pgValue) throws SQLException {
	  super.setValue(pgValue);
		
	  
		String sqlString = PGtokenizer.removePara(PGtokenizer.removePara(pgValue));
		PGtokenizer t = new PGtokenizer( sqlString, ',');
	
		int i=0;
		
		this.object = new ExterneRef(); 		
		this.object.georefurl = PGUtil.getString(t.getToken(i++));				
		this.object.georefmimetype = PGUtil.getString(t.getToken(i++));

		this.object.art = PGUtil.getString(t.getToken(i++)); // xplan_gml.xp_externereferenzart,
		
		this.object.informationssystemurl = PGUtil.getString(t.getToken(i++));	
		this.object.referenzname = PGUtil.getString(t.getToken(i++));
		this.object.referenzurl = PGUtil.getString(t.getToken(i++));
		
		String referenzmimetypeToken = t.getToken(i++);
		this.object.referenzmimetype = referenzmimetypeToken.length()==0 ? null : new Referenzmimetype(referenzmimetypeToken);
		
		this.object.beschreibung = PGUtil.getString(t.getToken(i++));
		
		String sDatum = t.getToken(i++);
		try {			
		  this.object.datum = PGUtil.getDate(sDatum);		
		} catch (ParseException e) {
			throw new SQLException("String \"" + sDatum + "\" konnte nicht als Datum geparst werden");
		}		
		
	}	
	
	
	public ExterneRef getExterneRef() {
	    return object;
	}
	
	
	@Override
	public String getValue() {	

		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(PGUtil.getStringValue(this.object.georefurl)).append(',');
		sb.append(PGUtil.getStringValue(this.object.georefmimetype)).append(',');
		sb.append(PGUtil.getStringValue(this.object.art)).append(',');
		sb.append(PGUtil.getStringValue(this.object.informationssystemurl)).append(',');
		sb.append(PGUtil.getStringValue(this.object.referenzname)).append(',');
		sb.append(PGUtil.getStringValue(this.object.referenzurl)).append(',');
		sb.append(getReferenzmimetypeValue(this.object.referenzmimetype)).append(',');
		
		if (this.object.beschreibung != null) {
		    sb.append('"').append(PGUtil.getStringValue(this.object.beschreibung)).append("\"");
		}
		sb.append(",");
		sb.append(PGUtil.getDateValue(this.object.datum)).append(',');		
		sb.append(")");
	  return sb.toString();
	}
	

	
	static final String getReferenzmimetypeValue(Referenzmimetype type) {
		return type == null ? "" : type.getValue();
	}
	

	@Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((object == null) ? 0 : object.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (getClass() != obj.getClass()) {
            logger.debug("PGExterneReferenz false01");
            return false;
        }
        PGExterneReferenz other = (PGExterneReferenz) obj;
        if (object == null) {
            if (other.object != null) {
                logger.debug("PGExterneReferenz false02");
                return false;
            }
        } else if (!object.equals(other.object)) {
            logger.debug("PGExterneReferenz false03");
            return false;
        }
        return true;
    }

    @Override
	public String toString() {
	  return getValue();
	}
	
	
}


