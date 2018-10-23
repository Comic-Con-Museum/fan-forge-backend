package org.comic_con.museum.fcb.controllers.responses;

import org.comic_con.museum.fcb.models.Exhibit;

import java.time.Instant;

public class ExhibitFull extends Feed.Entry {
    public final long author;
    public final Instant created;
    public final String[] tags;
    
    public ExhibitFull(Exhibit of, long supporters, Boolean supported) {
        super(of, supporters, supported);
        this.author = of.getAuthor();
        this.created = of.getCreated();
        this.tags = of.getTags();
    }
}
