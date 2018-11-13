package org.comic_con.museum.fcb.endpoints.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.comic_con.museum.fcb.models.Artifact;
import org.comic_con.museum.fcb.models.Exhibit;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ExhibitFull extends Feed.Entry {
    public static class Image extends Feed.Cover {
        public final boolean cover;
        public final String creator;
        public final Instant created;
        
        public Image(Artifact ar) {
            super(ar);
            this.cover = ar.isCover();
            this.creator = ar.getCreator();
            this.created = ar.getCreated();
        }
    }
    public final String author;
    public final Instant created;
    public final String[] tags;
    public final List<Image> artifacts;
    
    // hide the parent's cover
    @JsonIgnore public final Object cover = null;
    
    public ExhibitFull(Exhibit of, long supporters, Boolean supported, List<Artifact> artifacts) {
        super(of, supporters, supported);
        this.author = of.getAuthor();
        this.created = of.getCreated();
        this.tags = of.getTags();
        List<ExhibitFull.Image> converted = new ArrayList<>(artifacts.size());
        for (Artifact a : artifacts) {
            converted.add(new ExhibitFull.Image(a));
        }
        this.artifacts = converted;
    }
}
