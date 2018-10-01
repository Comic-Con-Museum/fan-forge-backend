package org.comic_con.museum.fcb;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class Exhibit {
    public class Abbreviated {
        public int getId() { return Exhibit.this.getId(); }
        public String getTitle() { return Exhibit.this.getTitle(); }
        public String getDescription() { return Exhibit.this.getDescription(); }
    }

    private int id;
    private String title;
    private String description;
    private String author;
    private Instant created;
    private final Set<String> supporters;

    public Exhibit(int id, String title, String description, String author) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.author = author;
        this.created = Instant.now();
        this.supporters = new HashSet<>();
    }

    @JsonIgnore
    public Abbreviated getAbbreviated() { return this.new Abbreviated(); }

    public int getId() { return this.id; }
    public String getTitle() { return this.title; }
    public String getDescription() { return this.description; }
    public String getAuthor() { return this.author; }
    public Instant getCreated() { return this.created; }

    public void setId(int id) { this.id = id; }
    public void setTitle(String value) { this.title = value; }
    public void setDescription(String value) { this.description = value; }
    public void setAuthor(String value) { this.author = value; }
    public void setCreated(Instant value) { this.created = value; }

    @JsonIgnore
    public int getSupporterCount() { return this.supporters.size(); }
    public Set<String> getSupporters() { return this.supporters; }
    public boolean addSupporter(String by) { return this.supporters.add(by); }
    public boolean removeSupporter(String by) { return this.supporters.remove(by); }
}
