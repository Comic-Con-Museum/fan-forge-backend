package org.comic_con.museum.fcb.models;

public class Artifact {
    public final long aid;
    public final String title;
    public final String description;
    public final long image;
    
    public Artifact(long aid, String title, String description, long image) {
        this.aid = aid;
        this.title = title;
        this.description = description;
        this.image = image;
    }
}
