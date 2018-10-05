package org.comic_con.museum.fcb.controllers.responses;

import org.comic_con.museum.fcb.models.Exhibit;

import java.time.Instant;

public class ExhibitFull {
    private Exhibit exhibit;
    private String user;

    public ExhibitFull(Exhibit showing, String toUser) {
        this.exhibit = showing;
        this.user = toUser;
    }

    public int getId() { return this.exhibit.getId(); }
    public String getTitle() { return this.exhibit.getTitle(); }
    public String getDescription() { return this.exhibit.getDescription(); }
    public int getSupporterCount() { return this.exhibit.getSupporters().size(); }
    public boolean isSupported() { return this.exhibit.getSupporters().contains(this.user); }
    public String getAuthor() { return this.exhibit.getAuthor(); }
    public Instant getCreated() { return this.exhibit.getCreated(); }
}
