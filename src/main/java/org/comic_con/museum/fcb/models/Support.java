package org.comic_con.museum.fcb.models;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

//@Entity
public class Support {
    @ManyToOne
    @JoinColumn(name = "supported_eid")
    public Exhibit exhibit;
    @ManyToOne
    @JoinColumn(name = "supporter_uid")
    public User user;
    
    // TODO: Survey data
    
    public Support(Exhibit of, User by) {
        this.exhibit = of;
        this.user = by;
    }
}
