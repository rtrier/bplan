package de.gdiservice.bplan;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Objects;

import org.postgresql.util.PGobject;
import org.postgresql.util.PGtokenizer;


public class PG_SO_Planart extends PGobject implements Serializable, Cloneable {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  String codespace = "http://bauleitplaene-mv.de/codelist/SO_PlanArt/SO_PlanArt.xml"; 
  String value;
  
  
  public PG_SO_Planart() {
      this.setType("\"xplan_gml\".\"so_planart\"");      
  }
  
  public PG_SO_Planart(String value) {
      this.setType("\"xplan_gml\".\"so_planart\"");
      this.value = value;
  }
  
    
	
	
	@Override
	public void setValue(String pgValue) throws SQLException {
	  super.setValue(pgValue);
		
	  String sqlString = PGtokenizer.removePara(PGtokenizer.removePara(pgValue));
	  PGtokenizer t = new PGtokenizer( sqlString, ',');
	
	  int i=0;
	  this.codespace = PGUtil.getString(t.getToken(i++));				
	  this.value = PGUtil.getString(t.getToken(i++));
	
	}	
	
	
	
	@Override
	public String getValue() {
	    // dreei-teiliges
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(PGUtil.getStringValue(this.codespace)).append(',');
		sb.append(PGUtil.getStringValue(this.value)).append(',');
		sb.append(")");
	  return sb.toString();
	}
	


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(codespace, value);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        PG_SO_Planart other = (PG_SO_Planart) obj;
        return Objects.equals(codespace, other.codespace) && Objects.equals(value, other.value);
    }

    @Override
	public String toString() {
	  return getValue();
	}
	
	
}


