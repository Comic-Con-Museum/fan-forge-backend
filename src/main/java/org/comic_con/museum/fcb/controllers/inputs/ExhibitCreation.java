package org.comic_con.museum.fcb.controllers.inputs;

import org.comic_con.museum.fcb.models.Exhibit;
import org.comic_con.museum.fcb.models.User;

public class ExhibitCreation {
    private String title;
    private String description;
    private String[] tags;

    ExhibitCreation() {}

    public ExhibitCreation(String title, String description, String[] tags) {
        this.title = title;
        this.description = description;
        this.tags = tags;
    }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }

    public Exhibit build(User author) {
        return new Exhibit(-1, this.title, this.description, author.getId(), null, this.tags);
    }
}
