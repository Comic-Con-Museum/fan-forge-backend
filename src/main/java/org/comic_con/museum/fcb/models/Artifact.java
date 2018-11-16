package org.comic_con.museum.fcb.models;

import java.time.Instant;

public class Artifact {
    private long id;
    private String title;
    private String description;
    private boolean cover;
    private String creator;
    private Instant created;
    
    public Artifact(long id, String title, String description, boolean cover,
                    String creator, Instant created) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.cover = cover;
        this.creator = creator;
        this.created = created;
    }
    
    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public boolean isCover() { return cover; }
    public long getImage() { return id; }
    public String getCreator() { return creator; }
    public Instant getCreated() { return created; }
    
    public void setId(long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setCover(boolean cover) { this.cover = cover; }
    public void setCreator(String creator) { this.creator = creator; }
    public void setCreated(Instant created) { this.created = created; }
}
