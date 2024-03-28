package de.gdiservice.bplan;

import java.sql.SQLException;
import java.text.ParseException;

import org.postgresql.util.PGtokenizer;

public class PGExterneReferenzAuslegung extends PGExterneReferenz {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public PGExterneReferenzAuslegung() {
        
    }
    public PGExterneReferenzAuslegung(ExterneRef bFitzExterneRef) {
        super(bFitzExterneRef);
      }

    @Override
    public void setValue(String pgValue) throws SQLException {
        this.value = pgValue;

        String sqlString = PGtokenizer.removePara(PGtokenizer.removePara(pgValue));
        PGtokenizer t = new PGtokenizer(sqlString, ',');

        int i = 0;

        ExterneRefAuslegung externeRefAuslegung = new ExterneRefAuslegung();
        this.object = externeRefAuslegung;
        this.object.georefurl = PGUtil.getString(t.getToken(i++));
        this.object.georefmimetype = PGUtil.getString(t.getToken(i++));

        this.object.art = PGUtil.getString(t.getToken(i++)); // xplan_gml.xp_externereferenzart,

        this.object.informationssystemurl = PGUtil.getString(t.getToken(i++));
        this.object.referenzname = PGUtil.getString(t.getToken(i++));
        this.object.referenzurl = PGUtil.getString(t.getToken(i++));

        String referenzmimetypeToken = t.getToken(i++);
        this.object.referenzmimetype = referenzmimetypeToken.length() == 0 ? null
                : new Referenzmimetype(referenzmimetypeToken);

        this.object.beschreibung = PGUtil.getString(t.getToken(i++));
        String sDatum = t.getToken(i++);
        try {
            this.object.datum = PGUtil.getDate(sDatum);
        } catch (ParseException e) {
            throw new SQLException("String \"" + sDatum + "\" konnte nicht als Datum geparst werden");
        }
        this.object.typ = PGUtil.getString(t.getToken(i++));
        String nurzurauslegung = t.getToken(i);
        externeRefAuslegung.nurzurauslegung = nurzurauslegung.startsWith("t");

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
        sb.append('"').append(PGUtil.getStringValue(this.object.beschreibung)).append("\",");
        sb.append(PGUtil.getDateValue(this.object.datum)).append(',');
        sb.append(PGUtil.getStringValue(this.object.typ)).append(',');
        if (this.object instanceof ExterneRefAuslegung) {
            ExterneRefAuslegung externeRefAuslegung = (ExterneRefAuslegung)this.object;
            if (externeRefAuslegung.nurzurauslegung != null && externeRefAuslegung.nurzurauslegung.booleanValue()) {
                sb.append("t");
            } else {
                sb.append("f");
            }
        } else {            
            sb.append("f");
        }
        sb.append(")");
      return sb.toString();
    }
    
    
}
