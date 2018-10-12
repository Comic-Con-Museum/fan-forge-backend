package org.comic_con.museum.fcb.models;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class Exhibit {
    private int id;
    private String title;
    private String description;
    private String author;
    private Instant created;
    private Set<Integer> supporters;

    public Exhibit(int id, String title, String description, String author) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.author = author;
        this.created = Instant.now();
        this.supporters = new HashSet<>();
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getAuthor() { return author; }
    public Instant getCreated() { return created; }
    public Set<Integer> getSupporters() { return supporters; }

    public void setId(int id) { this.id = id; }
    public void setTitle(String value) { this.title = value; }
    public void setDescription(String value) { this.description = value; }
    public void setAuthor(String value) { this.author = value; }
    public void setCreated(Instant value) { this.created = value; }
    public void setSupporters(Set<Integer> value) { this.supporters = value; }

    @Override
    public String toString() {
        return String.format("Exhibit %d: '%s' by %s}", getId(), getTitle(), getAuthor());
    }
}
