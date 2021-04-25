package de.gdiservice.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class DBUtil {
    
    /*
     * 
     */
    public static String getUpdateSQLString(String tableName, String[] columnNames, String keyColumn) {
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ").append(tableName);
        sb.append(" SET ");
        boolean notFirst = false;
        for (int i=0, count=columnNames.length; i<count; i++) {
            if (!columnNames[i].equals(keyColumn)) {
                if (notFirst) {
                    sb.append(",");
                } else {
                    notFirst = true;
                }
                sb.append(columnNames[i]).append("=?");
            }
        }        
        sb.append(" where ").append(keyColumn).append("=?");
        return sb.toString();
    }
    

    public static String getInsertSQLString(String tableName, String[] columnNames) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(tableName).append("(");
        sb.append(columnNames[0]);
        for (int i=1, count=columnNames.length; i<count; i++) {
            sb.append(",").append(columnNames[i]);
        }
        sb.append(") values (?");
        for (int i=1, count=columnNames.length; i<count; i++) {
            sb.append(",?");
        }
        sb.append(")");
        return sb.toString();
    }

    public static String getSelectSQLString(String tableName, String[] columnNames) {
        return getSelectSQLString(tableName, columnNames, null, null);
    }

    public static String getSelectSQLString(String tableName, String[] columnNames, Integer maxCount) {
        return getSelectSQLString(tableName, columnNames, null, maxCount);
    }

    public static String getSelectSQLString(String tableName, String[] columnNames, String[] whereClauses) {
        return getSelectSQLString(tableName, columnNames, whereClauses, null);
    }

    public static String getSelectSQLString(String tableName, String[] columnNames, String[] whereClauses, Integer maxCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("Select ");
        sb.append(columnNames[0]);
        for (int i=1, count=columnNames.length; i<count; i++) {
            sb.append(",").append(columnNames[i]);
        }
        sb.append(" from ").append(tableName);
        if (whereClauses!=null && whereClauses.length>0) {
            sb.append(" where ").append(whereClauses[0]);
            for (int i=1, count=whereClauses.length; i<count; i++) {
                sb.append(" and ").append(whereClauses[i]);
            }      
        }


        if (maxCount!=null) {
            sb.append(" limit ").append(maxCount);
        }
        return sb.toString();
    }  

    /**
     * @param f
     * @param url username@host:port/dbname
     * @return
     */
    public static String findPwd(File f, String url) {
        if (url == null) {
            throw new IllegalArgumentException("Es wurd keine URL angegeben");
        }

        int idxU = url.indexOf("@");
        if (idxU<=1) {
            throw new IllegalArgumentException("URL enthält keinen Usernamen");
        }
        String username = url.substring(0, idxU);
        int idxSlash = url.indexOf('/', idxU);
        if (idxSlash<0) {
            throw new IllegalArgumentException("URL nicht korrekt angegeben (username@host:port/dbname)");
        }
        String host = url.substring(idxU+1, idxSlash);
        int port = 5432;

        int idxC = host.indexOf(':');
        if (idxC>0) {
            port = Integer.parseInt(host.substring(idxC+1));
            host = host.substring(0, idxC);
        }

        String dbname = url.substring(idxSlash+1);


        if (f.exists() && f.canRead()) {

            BufferedReader r = null;

            try {
                String line;
                r = new BufferedReader(new FileReader(f));
                // pgsql-server2:5433:*:kvwmap:secretpwd
                while ( (line = r.readLine()) != null) {          
                    int idx1 = line.indexOf(':');
                    //          System.out.println("01 "+line.substring(0, idx1));
                    if (host.equals(line.substring(0, idx1))) {
                        int idx2 = line.indexOf(':', idx1+1);
                        if (String.valueOf(port).equals(line.substring(idx1+1, idx2))) {
                            int idx3 = line.indexOf(':', idx2+1);
                            String db = line.substring(idx2+1, idx3);
                            if (db.equals("*") || db.equals(dbname)) {
                                int idx4 = line.indexOf(':', idx3+1);
                                if (username.equals(line.substring(idx3+1, idx4))) {                    
                                    return line.substring(idx4+1);
                                }
                            }
                        }
                    }
                }
                throw new IllegalArgumentException("konnte Passwort für \""+url+"\" nicht in der PasswortDatei \""+f.getAbsolutePath()+ "\" finden.");
            }
            catch (FileNotFoundException ex) {
                throw new IllegalArgumentException("angegebene PasswortDatei \""+f.getAbsolutePath()+ "\" existiert nicht oder kann nicht gelesen werden.");
            }
            catch (IOException ex) {
                throw new IllegalArgumentException("Fehler beim Lesen der PasswortDatei \""+f.getAbsolutePath()+ "\".", ex);
            }
            finally {
                if (r!=null) {
                    try {
                        r.close();
                    } catch (IOException e) {}
                }
            }
        }
        else {
            throw new IllegalArgumentException("angegebene PasswortDatei \""+f.getAbsolutePath()+ "\" existiert nicht oder kann nicht gelesen werden.");
        }


    }

    public static DBConnectionParameter getConnectionParameter(File f, String url) {
        return getConnectionParameter(f, url, false);
    }

    /**
     * @param url String like username@host:port/dbname
     * @return
     */
    public static DBConnectionParameter getConnectionParameter(File f, String url, boolean debug) {
        if (f == null) {
            throw new IllegalArgumentException("Es wurd keine pgpass angegeben");
        }

        if (url == null) {
            throw new IllegalArgumentException("Es wurd keine URL angegeben");
        }

        int idxU = url.indexOf("@");
        if (idxU<=1) {
            throw new IllegalArgumentException("URL enthält keinen Usernamen");
        }
        String username = url.substring(0, idxU);
        int idxSlash = url.indexOf('/', idxU);
        if (idxSlash<0) {
            throw new IllegalArgumentException("URL nicht korrekt angegeben (username@host:port/dbname)");
        }
        String host = url.substring(idxU+1, idxSlash);
        int port = 5432;

        int idxC = host.indexOf(':');
        if (idxC>0) {
            port = Integer.parseInt(host.substring(idxC+1));
            host = host.substring(0, idxC);
        }
        String dbname = url.substring(idxSlash+1);

        String pwd = findPwd(f, url); 

        String jdbcurl;
        if (debug) {
            jdbcurl = "jdbc:postgresql://" + host + ":" + port + "/" + dbname+"?loggerLevel=DEBUG";
        } else {
            jdbcurl = "jdbc:postgresql://" + host + ":" + port + "/" + dbname;
        }
        
        return new DBConnectionParameter(jdbcurl, username, pwd);
    }



    public static void main(String[] args) {
        System.out
                .println(DBUtil.getUpdateSQLString("test.test", new String[] { "id", "name", "str", "att" }, "id"));
    }

}
