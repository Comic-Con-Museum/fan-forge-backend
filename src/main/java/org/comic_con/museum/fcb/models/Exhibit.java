package org.comic_con.museum.fcb.models;

import java.time.Instant;

public class Exhibit {
    private long id;
    private String title;
    private String description;
    private long author;
    private Instant created;
    private String[] tags;

    public Exhibit(long eid, String title, String description, long author, Instant created, String[] tags) {
        this.id = eid;
        this.title = title;
        this.description = description;
        this.author = author;
        this.created = created;
        this.tags = tags;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public long getAuthor() { return author; }
    public Instant getCreated() { return created; }
    public String[] getTags() { return tags; }
    
    public void setId(long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setAuthor(long author) { this.author = author; }
    public void setCreated(Instant created) { this.created = created; }
    public void setTags(String[] tags) { this.tags = tags; }
}
