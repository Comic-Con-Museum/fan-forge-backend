package org.comic_conmuseum.fan_forge.backend.endpoints.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.comic_conmuseum.fan_forge.backend.models.Artifact;
import org.comic_conmuseum.fan_forge.backend.models.Comment;
import org.comic_conmuseum.fan_forge.backend.models.Exhibit;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class ExhibitFull extends Feed.Entry {
    public static class Image extends Feed.Cover {
        public final long id;
        public final boolean cover;
        public final String creator;
        public final Instant created;
        
        public Image(Artifact ar) {
            super(ar);
            this.id = ar.getId();
            this.cover = ar.isCover();
            this.creator = ar.getCreator();
            this.created = ar.getCreated();
        }
    }
    public final String author;
    public final Instant created;
    public final String[] tags;
    public final List<Image> artifacts;
    public final List<CommentView> comments;

    // hide the parent's cover
    @SuppressWarnings("unused") @JsonIgnore public final Object cover = null;
    
    public ExhibitFull(Exhibit of, long supporters, Boolean supported, List<Artifact> artifacts,
                       List<Comment> comments) {
        super(of, supporters, 0, supported);
        this.author = of.getAuthor();
        this.created = of.getCreated();
        this.tags = of.getTags();
        this.artifacts = artifacts.stream().map(Image::new).collect(Collectors.toList());
        this.comments = comments.stream().map(CommentView::new).collect(Collectors.toList());
    }
}
