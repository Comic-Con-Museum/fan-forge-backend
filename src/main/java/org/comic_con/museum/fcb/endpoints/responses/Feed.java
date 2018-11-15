package org.comic_con.museum.fcb.endpoints.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.comic_con.museum.fcb.models.Artifact;
import org.comic_con.museum.fcb.persistence.ExhibitQueryBean;
import org.comic_con.museum.fcb.models.Exhibit;

import java.util.List;

public class Feed {
    public static class Cover {
        public final String title;
        public final String description;
        public final long image;
        public final String creator;
        
        public Cover(Artifact ar) {
            this.title = ar.getTitle();
            this.description = ar.getDescription();
            this.image = ar.getImage();
            this.creator = ar.getCreator();
        }
    }
    
    public static class Entry {
        public final long id;
        public final String title;
        public final String description;
        public final Cover cover;
        public final long supporters;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public final Boolean supported;
        
        public Entry(Exhibit of, long supporters, Boolean supported) {
            this.id = of.getId();
            this.title = of.getTitle();
            this.description = of.getDescription();
            this.cover = of.getCover() == null ? null : new Cover(of.getCover());
            this.supporters = supporters;
            this.supported = supported;
        }
    }
    
    public final long startIdx;
    public final long count;
    public final int pageSize;
    public final List<Entry> exhibits;
    
    public Feed(long startIdx, long count, List<Entry> exhibits) {
        this.startIdx = startIdx;
        this.count = count;
        this.pageSize = ExhibitQueryBean.PAGE_SIZE;
        this.exhibits = exhibits;
    }
}
