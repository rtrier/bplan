package de.gdiservice.bplan.poi;

import org.postgresql.util.PGobject;
import org.postgresql.util.PGtokenizer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.gdiservice.bplan.PGUtil;

public class Referenzmimetype extends PGobject {
	
	private static final long serialVersionUID = 1L;
	
	public String codespace;
	public String id;
	public String value;
	
	Referenzmimetype() {}
	
	
	Referenzmimetype(String token) {
		this.setValue(token);
	}
	
	@JsonProperty
	public String getCodespace() {
		return codespace;
	}
	
	@JsonProperty
	public String getId() {
		return id;
	}
	
	@JsonProperty(value = "value")
	public String getMimeTypeValue() {		
		return value;
	}
	@JsonProperty(value = "value")
	public void setMimeTypeValue(String value) {      
	    this.value = value;
	}
	
	@Override
	public String toString() {
		return "Referenzmimetype [codespace=" + codespace + ", id=" + id + ", value=" + value + "]";
	}
	
	@Override
	@JsonIgnore
	public void setValue(String pgValue) {
		String sValue = PGtokenizer.remove(pgValue, "\"", "\"");
		String sqlString = PGtokenizer.removePara(PGtokenizer.removePara(sValue));		
		PGtokenizer t = new PGtokenizer( sqlString, ',');
		codespace = PGUtil.getString(t.getToken(0));
		id = PGUtil.getString(t.getToken(1));
		this.value = PGUtil.getString(t.getToken(2));		
	}
	
	@Override
	public String getValue() {	
		StringBuilder sb = new StringBuilder();
		sb.append("\"(");
		if (codespace!=null) { sb.append(codespace); }
		sb.append(',');
		if (id!=null) { sb.append(id); }
		sb.append(',');
		if (this.value!=null) { sb.append(this.value); }		
		sb.append(")\"");
		return sb.toString();
	}


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((codespace == null) ? 0 : codespace.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }


  @Override
  public boolean equals(Object obj) {
//    System.out.println("Referenzmimetype.equals");
    if (this == obj)
      return true;
    
//    System.err.println(((PGobject) obj).getValue());
//    System.err.println(this.getValue());
    
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    Referenzmimetype other = (Referenzmimetype) obj;
    if (codespace == null) {
      if (other.codespace != null)
        return false;
    } else if (!codespace.equals(other.codespace))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }
	
	
}