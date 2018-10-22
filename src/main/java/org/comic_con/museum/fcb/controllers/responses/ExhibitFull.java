package org.comic_con.museum.fcb.controllers.responses;

import com.fasterxml.jackson.annotation.JsonView;
import org.comic_con.museum.fcb.models.Exhibit;

import java.time.Instant;

public class ExhibitFull extends Feed.Entry {
    @JsonView(Views.Unauthed.class)
    public final long author;
    @JsonView(Views.Unauthed.class)
    public final Instant created;
    @JsonView(Views.Unauthed.class)
    public final String[] tags;
    
    public ExhibitFull(Exhibit of, int supporters, Boolean supported) {
        super(of, supporters, supported);
        this.author = of.getAuthor();
        this.created = of.getCreated();
        this.tags = of.getTags();
    }
}
