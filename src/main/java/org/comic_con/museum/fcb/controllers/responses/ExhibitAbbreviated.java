package org.comic_con.museum.fcb.controllers.responses;

import org.comic_con.museum.fcb.models.Exhibit;
import org.comic_con.museum.fcb.models.User;

public class ExhibitAbbreviated {
    protected final User user;
    protected final Exhibit exhibit;

    public ExhibitAbbreviated(Exhibit showing, User toUser) {
        this.exhibit = showing;
        this.user = toUser;
    }

    public int getId() { return this.exhibit.getId(); }
    public String getTitle() { return this.exhibit.getTitle(); }
    public String getDescription() { return this.exhibit.getDescription(); }
    public int getSupporterCount() { return this.exhibit.getSupports().size(); }
    public boolean isSupported() {
        return this.user != null &&
                this.exhibit.getSupports().stream().anyMatch(
                        s -> s.user.getUsername().equals(user.getUsername())
                );
    }
}
