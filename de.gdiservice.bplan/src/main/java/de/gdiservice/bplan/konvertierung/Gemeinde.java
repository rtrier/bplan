package de.gdiservice.bplan.konvertierung;

public class Gemeinde {
    
	public Integer id_amt;
	public String amt_name; 
	public Integer id_gmd;
	public String rs;
	public String ags;
	public String gmd_name;
	public Integer id_ot;
	public String ot_name;
	public Integer stelle_id;
		
    @Override
    public String toString() {
        return "Gemeinde [id_amt=" + id_amt + ", amt_name=" + amt_name + ", id_gmd=" + id_gmd + ", rs=" + rs + ", ags="
                + ags + ", gmd_name=" + gmd_name + ", id_ot=" + id_ot + ", ot_name=" + ot_name + ", stelle_id="
                + stelle_id + "]";
    }  
	
}

