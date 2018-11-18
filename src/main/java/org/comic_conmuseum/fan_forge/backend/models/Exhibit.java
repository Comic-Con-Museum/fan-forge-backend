package org.comic_conmuseum.fan_forge.backend.models;

import java.time.Instant;

public class Exhibit {
    private long id;
    private String title;
    private String description;
    private String author;
    private Instant created;
    private String[] tags;
    private Artifact cover;

    public Exhibit(long eid, String title, String description, String author,
                   Instant created, String[] tags, Artifact cover) {
        this.id = eid;
        this.title = title;
        this.description = description;
        this.author = author;
        this.created = created;
        this.tags = tags;
        this.cover = cover;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getAuthor() { return author; }
    public Instant getCreated() { return created; }
    public String[] getTags() { return tags; }
    public Artifact getCover() { return cover; }
    
    public void setId(long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setAuthor(String author) { this.author = author; }
    public void setCreated(Instant created) { this.created = created; }
    public void setTags(String[] tags) { this.tags = tags; }
    public void setCover(Artifact cover) { this.cover = cover; }
}
