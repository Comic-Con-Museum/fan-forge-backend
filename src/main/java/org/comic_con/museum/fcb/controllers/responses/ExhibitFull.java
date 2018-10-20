package org.comic_con.museum.fcb.controllers.responses;

import com.fasterxml.jackson.annotation.JsonView;

import java.time.Instant;

public class ExhibitFull extends Feed.Entry {
    @JsonView(Views.Unauthed.class)
    public final long author;
    @JsonView(Views.Unauthed.class)
    public final Instant created;
    
    public ExhibitFull(long id, String title, String description, int supporters, long author, Instant created) {
        super(id, title, description, supporters);
        this.author = author;
        this.created = created;
    }
    
    public ExhibitFull(long id, String title, String description, int supporters, boolean supported, long author,
                       Instant created) {
        super(id, title, description, supporters, supported);
        this.author = author;
        this.created = created;
    }
}
