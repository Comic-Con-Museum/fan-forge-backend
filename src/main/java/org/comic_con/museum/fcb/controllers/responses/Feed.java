package org.comic_con.museum.fcb.controllers.responses;

import com.fasterxml.jackson.annotation.JsonView;
import org.comic_con.museum.fcb.dal.ExhibitQueryBean;

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
        
        public Entry(long id, String title, String description, int supporters) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.supporters = supporters;
            this.supported = null;
        }
        
        public Entry(long id, String title, String shortDescription, int supporters, boolean supported) {
            this.id = id;
            this.title = title;
            this.description = shortDescription;
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
    public final Entry[] exhibits;
    
    public Feed(long startIdx, long count, Entry... exhibits) {
        this.startIdx = startIdx;
        this.count = count;
        this.pageSize = ExhibitQueryBean.PAGE_SIZE;
        this.exhibits = exhibits;
    }
}
