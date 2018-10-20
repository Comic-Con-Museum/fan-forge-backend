package org.comic_con.museum.fcb.models;

public class Support {
    private int id;
    private int exhibit;
    private int user;
    // TODO: Survey data
    
    public Support(int sid, int exhibit, int user) {
        this.id = sid;
        this.exhibit = exhibit;
        this.user = user;
    }
    
    public int getId() { return id; }
    public int getExhibit() { return exhibit; }
    public int getUser() { return user; }
    
    public void setExhibit(int exhibit) { this.exhibit = exhibit; }
    public void setUser(int user) { this.user = user; }
}
