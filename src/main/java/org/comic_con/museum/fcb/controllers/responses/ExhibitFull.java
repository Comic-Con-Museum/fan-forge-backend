package org.comic_con.museum.fcb.controllers.responses;

import org.comic_con.museum.fcb.models.Exhibit;
import org.comic_con.museum.fcb.models.User;

import java.time.Instant;

public class ExhibitFull extends ExhibitAbbreviated {
    public ExhibitFull(Exhibit showing, User toUser) {
        super(showing, toUser);
    }
    
    public String getAuthor() { return this.exhibit.getAuthor().getUsername(); }
    public Instant getCreated() { return this.exhibit.getCreated(); }
}
