package org.comic_con.museum.fcb.endpoints.responses;

import org.comic_con.museum.fcb.models.Artifact;
import org.comic_con.museum.fcb.models.Exhibit;

import java.time.Instant;
import java.util.List;

public class ExhibitFull extends Feed.Entry {
    public final String author;
    public final Instant created;
    public final String[] tags;
    public final List<Artifact> artifacts;
    
    public ExhibitFull(Exhibit of, long supporters, Boolean supported, List<Artifact> artifacts) {
        super(of, supporters, supported);
        this.author = of.getAuthor();
        this.created = of.getCreated();
        this.tags = of.getTags();
        this.artifacts = artifacts;
    }
}
