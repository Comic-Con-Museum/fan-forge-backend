package org.comic_con.museum.fcb.endpoints.responses;

import org.comic_con.museum.fcb.models.Artifact;
import org.comic_con.museum.fcb.models.Exhibit;

import java.time.Instant;

public class ExhibitFull extends Feed.Entry {
    public final String author;
    public final Instant created;
    public final String[] tags;
    public final Artifact[] artifacts;
    
    public ExhibitFull(Exhibit of, long supporters, Boolean supported, Artifact[] artifacts) {
        super(of, supporters, supported);
        this.author = of.getAuthor();
        this.created = of.getCreated();
        this.tags = of.getTags();
        this.artifacts = artifacts;
    }
}
