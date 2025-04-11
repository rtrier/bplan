package de.gdiservice.bplan.poi;

import java.sql.SQLException;

import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BPStatus extends CodeList {

    final static Logger logger = LoggerFactory.getLogger(BPStatus.class);
    
    public BPStatus()  {}

    public BPStatus(Object object) throws SQLException {
        if (object instanceof PGobject) {
            this.setValue( ((PGobject)object).getValue() );
        }
    }

    private static final long serialVersionUID = 1L;

    String codespace; //"13072072", "
    String id; // " : "130725260072", ""
    String statusValue;
    		

    @Override
    public String toString() {
        return getValue();
    }



}


