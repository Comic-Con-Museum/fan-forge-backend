package org.comic_con.museum.fcb;

import java.time.LocalDateTime;

public class Exhibit {
    private final String title;
    private final String description;
    private final LocalDateTime created;

    public Exhibit(String title, String description, LocalDateTime created) {
        this.title = title;
        this.description = description;
        this.created = created;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public LocalDateTime getCreated() {
        return this.created;
    }
}
