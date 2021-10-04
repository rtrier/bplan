package de.gdiservice.bplan;

public class ImportConfigEntry {
  
  public static final String[] ATTNAMES = new String[] {
      "id", "bezeichnung", "onlineresource", "featuretype", "email", "stelle_id", "created_at", "created_from", "updated_at", "updated_from", "version"
  };
  
  public Object id;
  public Object bezeichnung;
  public String onlineresource;
  public String featuretype;
  public String email; 
  public Integer stelle_id;
  public Object created_at;
  public Object created_from;
  public Object updated_at;
  public Object updated_from;
  public Object version;
  
  @Override
  public String toString() {
    return "ImportConfigEntry [id=" + id + ", bezeichnung=" + bezeichnung + ", onlineresource=" + onlineresource
        + ", featuretype=" + featuretype + ", stelle_id=" + stelle_id + ", created_at=" + created_at + ", created_from="
        + created_from + ", updated_at=" + updated_at + ", updated_from=" + updated_from + ", version=" + version + "]";
  }
  
  

}
