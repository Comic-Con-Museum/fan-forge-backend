package org.comic_con.museum.fcb;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class Exhibit {
    public class Abbreviated {
        protected String user;

        private Abbreviated(String user) {
            this.user = user;
        }

        public int getId() { return Exhibit.this.id; }
        public String getTitle() { return Exhibit.this.title; }
        public String getDescription() { return Exhibit.this.description; }
        public int getSupporterCount() { return Exhibit.this.supporters.size(); }
        public boolean isSupported() { return Exhibit.this.supporters.contains(this.user); }
    }

    public class Full extends  Abbreviated {
        private Full(String user) { super(user); }

        public String getAuthor() { return Exhibit.this.author; }
        public Instant getCreated() { return Exhibit.this.created; }
    }

    static class Input {
        public void setTitle(String title) { this.title = title; }
        public void setDescription(String description) { this.description = description; }

        String title;
        String description;
        Input() {}
        Input(String title, String description) {
            this.title = title;
            this.description = description;
        }
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

    public Abbreviated getAbbreviated(String user) { return this.new Abbreviated(user); }
    public Full getFull(String user) { return this.new Full(user); }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getAuthor() { return author; }
    public Instant getCreated() { return created; }

    public void setId(int id) { this.id = id; }
    public void setTitle(String value) { this.title = value; }
    public void setDescription(String value) { this.description = value; }
    public void setAuthor(String value) { this.author = value; }
    public void setCreated(Instant value) { this.created = value; }

    public boolean addSupporter(String by) { return this.supporters.add(by); }
    public boolean removeSupporter(String by) { return this.supporters.remove(by); }
}
