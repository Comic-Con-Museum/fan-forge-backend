package org.comic_con.museum.fcb.controllers.inputs;

import org.comic_con.museum.fcb.models.Exhibit;
import org.comic_con.museum.fcb.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExhibitCreation {
    private final Logger LOG = LoggerFactory.getLogger(ExhibitCreation.class);

    public String title;
    public String description;

    ExhibitCreation() {
        LOG.info("Creating ExhibitCreation");
    }

    public ExhibitCreation(String title, String description) {
        this();
        this.title = title;
        this.description = description;
    }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }

    public Exhibit build(User author) {
        // it's not inserted yet, so its ID can't be trusted
        return new Exhibit(-1, this.title, this.description, author.getUsername());
    }
}
