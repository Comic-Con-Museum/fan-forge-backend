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
    private Instant created;

    public Artifact(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
        this(
                rs.getLong("aid"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getBoolean("cover"),
                rs.getString("creator"),
                rs.getTimestamp("created").toInstant()
        );
    }

    public Artifact(long id, String title, String description, boolean cover,
                    String creator, Instant created) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.cover = cover;
        this.creator = creator;
        this.created = created;
    }

    /** Creates an artifact from the result set joined with the exhibit.
     * Here the names for fields are slightly different */
    static Artifact coverFromJoined(ResultSet rs) throws SQLException {
        return new Artifact(
                rs.getLong("aid"),
                rs.getString("atitle"),
                rs.getString("adesc"),
                true, // always true; we only get covers in this bean
                rs.getString("acreator"),
                rs.getTimestamp("acreated").toInstant()
        );
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
