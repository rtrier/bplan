package de.gdiservice.bplan;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Objects;

import org.postgresql.util.PGobject;
import org.postgresql.util.PGtokenizer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CodeList extends PGobject implements Serializable, Cloneable {
    
    private static final long serialVersionUID = 1L;
    
    public String codespace;
    public String id;
    public String codelistValue;
    
    
    public CodeList() {
        
    }
    
    public CodeList(String codespace, String id, String value) {
        super();
        this.codespace = codespace;
        this.id = id;
        this.value = value;
    }

    
    @Override
    @JsonIgnore
    public void setValue(String value) throws SQLException {
        
        String sqlString = PGtokenizer.removePara(PGtokenizer.removeCurlyBrace(value));
        PGtokenizer t = new PGtokenizer( sqlString, ',');

        codespace  = t.getToken(0);
        id  = t.getToken(1);
        value = PGUtil.trim(t.getToken(2));        
        this.value = value;
    }   

    @Override
    @JsonIgnore
    public String getValue() {      
        final String v = "(\"" + this.codespace + "\",\"" + this.id + "\",\"" + this.value + "\")";
        return v;
    }       
    
    
    
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(codelistValue, codespace, id);
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
        CodeList other = (CodeList) obj;
        return Objects.equals(codelistValue, other.codelistValue) && Objects.equals(codespace, other.codespace)
                && Objects.equals(id, other.id);
    }


    public String getCodespace() {
        return codespace;
    }

    public void setCodespace(String codespace) {
        this.codespace = codespace;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCodelistValue() {
        return codelistValue;
    }

    @JsonProperty("value")
    public void setCodelistValue(String codelistValue) {
        this.codelistValue = codelistValue;
    }    
    
    

    @Override
    public String toString() {
        return "CodeList[codespace=" + codespace + ", id=" + id + ", value=" + value + "]";
    }
    
    
}
