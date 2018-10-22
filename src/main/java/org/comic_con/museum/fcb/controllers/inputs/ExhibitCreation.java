package org.comic_con.museum.fcb.controllers.inputs;

import org.comic_con.museum.fcb.models.Exhibit;
import org.comic_con.museum.fcb.models.User;

import java.time.Instant;

public class ExhibitCreation {
    private String title;
    private String description;
    private String[] tags;

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String[] getTags() { return tags; }
    
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setTags(String[] tags) { this.tags = tags; }
    
    public Exhibit build(User author) {
        return new Exhibit(-1, this.title, this.description, author.getId(), Instant.now(), this.tags);
    }
}
