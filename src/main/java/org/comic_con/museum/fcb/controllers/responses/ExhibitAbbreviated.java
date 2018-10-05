package org.comic_con.museum.fcb.controllers.responses;

import org.comic_con.museum.fcb.models.Exhibit;

public class ExhibitAbbreviated {
    private final String user;
    private Exhibit exhibit;

    public ExhibitAbbreviated(Exhibit showing, String toUser) {
        this.exhibit = showing;
        this.user = toUser;
    }
    public int getId() { return this.exhibit.getId(); }
    public String getTitle() { return this.exhibit.getTitle(); }
    public String getDescription() { return this.exhibit.getDescription(); }
    public int getSupporterCount() { return this.exhibit.getSupporters().size(); }
    public boolean isSupported() { return this.exhibit.getSupporters().contains(this.user); }
}
