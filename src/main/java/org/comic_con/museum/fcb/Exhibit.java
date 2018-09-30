package org.comic_con.museum.fcb;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class Exhibit {
    private String title;
    private String description;
    private Instant created;
    private final Set<String> supporters;

    public Exhibit(String title, String description) {
        this.title = title;
        this.description = description;
        this.created = Instant.now();
        this.supporters = new HashSet<>();
    }

    public String getTitle() { return this.title; }
    public String getDescription() { return this.description; }
    public Instant getCreated() { return this.created; }

    public void setTitle(String value) { this.title = value; }
    public void setDescription(String value) { this.description = value; }
    public void setCreated(Instant value) { this.created = value; }

    public int getSupporterCount() { return this.supporters.size(); }
    public boolean addSupporter(String by) { return this.supporters.add(by); }
    public boolean removeSupporter(String by) { return this.supporters.remove(by); }
}
