package org.comic_con.museum.fcb.controllers.responses;

import com.fasterxml.jackson.annotation.JsonView;
import org.comic_con.museum.fcb.dal.ExhibitQueryBean;
import org.comic_con.museum.fcb.models.Exhibit;

import java.util.List;

public class Feed {
    public static class Entry {
        @JsonView(Views.Unauthed.class)
        public final long id;
        @JsonView(Views.Unauthed.class)
        public final String title;
        @JsonView(Views.Unauthed.class)
        public final String description;
        @JsonView(Views.Unauthed.class)
        public final int supporters;
        @JsonView(Views.Authed.class)
        public final Boolean supported;
        
        public Entry(Exhibit of, int supporters, Boolean supported) {
            this.id = of.getId();
            this.title = of.getTitle();
            this.description = of.getDescription();
            this.supporters = supporters;
            this.supported = supported;
        }
    }
    
    @JsonView(Views.Unauthed.class)
    public final long startIdx;
    @JsonView(Views.Unauthed.class)
    public final long count;
    @JsonView(Views.Unauthed.class)
    public final int pageSize;
    @JsonView(Views.Unauthed.class)
    public final List<Entry> exhibits;
    
    public Feed(long startIdx, long count, List<Entry> exhibits) {
        this.startIdx = startIdx;
        this.count = count;
        this.pageSize = ExhibitQueryBean.PAGE_SIZE;
        this.exhibits = exhibits;
    }
}
