package org.comic_con.museum.fcb.controllers.inputs;

import org.comic_con.museum.fcb.models.Exhibit;
import org.comic_con.museum.fcb.models.User;

public class ExhibitCreation {
    private String title;
    private String description;

    ExhibitCreation() {}

    public ExhibitCreation(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }

    public Exhibit build(User author) {
        return new Exhibit(this.title, this.description, author);
    }
}
