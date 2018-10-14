package org.comic_con.museum.fcb.controllers.responses;

import org.comic_con.museum.fcb.models.Exhibit;
import org.comic_con.museum.fcb.models.Support;
import org.comic_con.museum.fcb.models.User;

import java.time.Instant;

public class ExhibitFull extends ExhibitAbbreviated {
    private final Exhibit exhibit;
    private final User user;

    public ExhibitFull(Exhibit showing, User toUser) {
        super(showing, toUser);
        this.exhibit = showing;
        this.user = toUser;
    }
    
    public String getAuthor() { return this.exhibit.getAuthor().getUsername(); }
    public Instant getCreated() { return this.exhibit.getCreated(); }
}
