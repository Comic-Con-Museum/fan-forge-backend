package org.comic_conmuseum.fan_forge.backend.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

public class Artifact {
    private long id;
    private String title;
    private String description;
    private boolean cover;
    private String creator;
    private long parent;
    private Instant created;

    public Artifact(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
        this(
                rs.getLong("aid"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getBoolean("cover"),
                rs.getString("creator"),
                rs.getLong("exhibit"),
                rs.getTimestamp("created").toInstant()
        );
    }

    public Artifact(long id, String title, String description, boolean cover,
                    String creator, long parent, Instant created) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.cover = cover;
        this.creator = creator;
        this.parent = parent;
        this.created = created;
    }

    /** Creates an artifact from the result set joined with the exhibit.
     * Here the names for fields are slightly different */
    static Artifact coverFromJoined(ResultSet rs, long eid) throws SQLException {
        return new Artifact(
                rs.getLong("aid"),
                rs.getString("atitle"),
                rs.getString("adesc"),
                true, // always true; we only get covers with this query
                rs.getString("acreator"),
                eid,
                rs.getTimestamp("acreated").toInstant()
        );
    }
    
    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public boolean isCover() { return cover; }
    public long getImage() { return id; }
    public String getCreator() { return creator; }
    public long getParent() { return parent; }
    public Instant getCreated() { return created; }
    
    public void setId(long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setCover(boolean cover) { this.cover = cover; }
    public void setCreator(String creator) { this.creator = creator; }
    public void setParent(long parent) { this.parent = parent; }
    public void setCreated(Instant created) { this.created = created; }

    @Override
    public String toString() {
        return String.format("Artifact(%d, '%s', '%s', %b, %s, %d, %s)",
                getId(),
                getTitle(),
                getDescription(),
                isCover(),
                getCreator(),
                getParent(),
                getCreated()
        );
    }
}
