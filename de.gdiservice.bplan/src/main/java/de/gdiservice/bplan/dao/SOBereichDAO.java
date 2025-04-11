package de.gdiservice.bplan.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.gdiservice.bplan.poi.SOBereich;
import de.gdiservice.bplan.poi.SOPlan;
import de.gdiservice.bplan.poi.XPBereich;

public class SOBereichDAO extends XPBereichDAO<SOBereich> {
    

    final static String KEY_COLUMN = "gml_id";
    
    public SOBereichDAO(Connection conWrite, Connection conRead, String tableName) {
        super(conWrite, conRead, tableName);
    }

    @Override
    SOBereich createBereich() {
        return new SOBereich();
    }

    @Override
    public String[] getColumns() {
        return XPBereichDAO.COLLUMN_NAMES; 
    }

    @Override
    public String getKeyColumn() {
        return SOPlanDAO.KEY_COLUMN;
    }

//    protected SOBereich createObject(ResultSet rs) throws SQLException {
//        return super.createBereich(rs);
//    }
//
    @Override
    protected SOBereich createObject(ResultSet rs) throws SQLException {        
        return super.createBereich(rs);        
    }    
    
    @Override
    protected int setSQLParameter(Connection con, PreparedStatement stmt, SOBereich bereich, int i) throws SQLException {
        return super.setSQLParameter(con, stmt, (XPBereich<SOPlan>)bereich, i);
    }

}
