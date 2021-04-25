package de.gdiservice.bplan;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class ImportLogger {
    
    List<String> strings = new ArrayList<>();
    List<String> errors = new ArrayList<>();
    OffsetDateTime time;
    
    public ImportLogger() {
        time = OffsetDateTime.now();
    }

    public void addLine(String s) {
        strings.add(s);
    }
    
    public void addError(String s) {
        strings.add(s);
        errors.add(s);
    }
    
    public OffsetDateTime getTime() {
        return time;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public String getText() {
        StringBuilder sb = new StringBuilder();
        for (String s : strings) {
            sb.append(s).append("\n");
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String s : strings) {
            sb.append(s).append("\n");
        }
        return sb.toString();
    }
}
