package org.comic_con.museum.fcb.models;

public class Support {
    private long id;
    private int exhibit;
    private int user;
    // TODO: Survey data
    
    public long getId() { return id; }
    public int getExhibit() { return exhibit; }
    public int getUser() { return user; }
    
    public void setId(long id) { this.id = id; }
    public void setExhibit(int exhibit) { this.exhibit = exhibit; }
    public void setUser(int user) { this.user = user; }
}
