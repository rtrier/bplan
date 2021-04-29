package de.gdiservice.util;

public class DBConnectionParameter {

    String url;      
    String user;
    String password;

    public DBConnectionParameter(String url, String user, String password) {
        super();
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "DBConnectionParameter [url=" + url + ", user=" + user + ", password=" + password + "]";
    }



}
