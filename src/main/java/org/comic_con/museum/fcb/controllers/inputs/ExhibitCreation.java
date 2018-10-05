package org.comic_con.museum.fcb.controllers.inputs;

import org.comic_con.museum.fcb.models.Exhibit;

public class ExhibitCreation {
    public String title;
    public String description;

    ExhibitCreation() {}

    public ExhibitCreation(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }

    public Exhibit build(String author) {
        // it's not inserted yet, so its ID can't be trusted
        return new Exhibit(-1, this.title, this.description, author);
    }
}
