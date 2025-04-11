package de.gdiservice.bplan.poi;

import java.sql.SQLException;


import org.postgresql.util.PGobject;
import org.postgresql.util.PGtokenizer;

import de.gdiservice.bplan.PGUtil;


public class PGVerbundenerPlan extends PGobject {

    private static final long serialVersionUID = 1L;
    
    private VerbundenerPlan object;
    
    public PGVerbundenerPlan() {}
    
    public PGVerbundenerPlan(VerbundenerPlan verbundenerplan) {
      this.object = verbundenerplan;
    }
    
    public VerbundenerPlan getVerbundenerPlan() {
        return object;
    }

    
    @Override
    public void setValue(String pgValue) throws SQLException {
      super.setValue(pgValue);
      
        String sqlString = PGtokenizer.removePara(PGtokenizer.removePara(pgValue));
        PGtokenizer t = new PGtokenizer( sqlString, ',');
    
        int i=0;
        
        this.object = new VerbundenerPlan();         
        
        this.object.planname = PGUtil.getString(t.getToken(i++));
        
        String rechtscharakterToken = t.getToken(i++);
        this.object.rechtscharakter = (rechtscharakterToken.length()==0) ? null : VerbundenerPlan.RechtscharakterPlanaenderung.get(rechtscharakterToken);
        this.object.nummer = PGUtil.getString(t.getToken(i++));
        this.object.verbundenerplan = PGUtil.getString(t.getToken(i++));
    
    }   
    
    @Override
    public String getValue() {  
        StringBuilder sb = new StringBuilder();
//        sb.append("\"(\\\"");    
//        sb.append("(\"\"\"").append("Am Dpo4").append("\"\"\"").append(",");;
        
//        if (object == null) {
//            return null
//        }
        
        sb.append("(");
        if (this.object.planname != null) {
            sb.append("\"").append(PGUtil.getStringValue(this.object.planname)).append("\"");
        }
        sb.append(",");
        
        if (this.object.rechtscharakter != null) {
            sb.append("\"").append(PGUtil.getStringValue(this.object.rechtscharakter.getArt().toString())).append("\"");
        };
        sb.append(',');
        if (this.object.nummer != null) {
            sb.append("\"").append(PGUtil.getStringValue(this.object.nummer)).append("\"");
        }
        sb.append(',');
        sb.append("\"").append(PGUtil.getStringValue(this.object.verbundenerplan)).append("\"");
        sb.append(")");
        // sb.append("\\\")");
      return sb.toString();
    }


    
    
    
}
