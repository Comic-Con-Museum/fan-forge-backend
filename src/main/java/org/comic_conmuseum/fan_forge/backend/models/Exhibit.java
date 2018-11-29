package org.comic_conmuseum.fan_forge.backend.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

public class Exhibit {
    private long id;
    private String title;
    private String description;
    private String author;
    private Instant created;
    private String[] tags;
    private Artifact cover;
    private boolean featured;

    public Exhibit(long eid, String title, String description, String author,
                   Instant created, String[] tags, Artifact cover, boolean featured) {
        this.id = eid;
        this.title = title;
        this.description = description;
        this.author = author;
        this.created = created;
        this.tags = tags;
        this.cover = cover;
        this.featured = featured;
    }

    public Exhibit (ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
        this (
                rs.getInt("eid"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("author"),
                rs.getTimestamp("created").toInstant(),
                (String[]) rs.getArray("tags").getArray(),
                rs.getString("atitle") != null ? Artifact.coverFromJoined(rs) : null,
                rs.getBoolean("featured")
        );
    }


    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getAuthor() { return author; }
    public Instant getCreated() { return created; }
    public String[] getTags() { return tags; }
    public Artifact getCover() { return cover; }
    public boolean getFeatured() { return featured; }

    public void setId(long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setAuthor(String author) { this.author = author; }
    public void setCreated(Instant created) { this.created = created; }
    public void setTags(String[] tags) { this.tags = tags; }
    public void setCover(Artifact cover) { this.cover = cover; }
}
