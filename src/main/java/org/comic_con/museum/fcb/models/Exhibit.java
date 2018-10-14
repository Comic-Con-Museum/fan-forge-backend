package org.comic_con.museum.fcb.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

//@Entity
public class Exhibit {
    private static final int MAX_TAGS = 5;
    private static final Logger LOG = LoggerFactory.getLogger("dao.exhibit");
    
    private int id;
    private String title;
    private String description;
    @ManyToOne
    @JoinColumn(name = "author_uid")
    private User author;
    private Instant created;
    @OneToMany
    private List<Support> supports;

    /*
    TODO: Denormalize?
    
    We intentionally have a limited number of tags, so we COULD denormalize
    this (make N fields, tag1 through tagN), but for now we're going to
    reference another table instead. It's more customizable that way, and then
    once we have the requirements set in stone, we can optimize.
    */
    @ManyToMany
    @JoinTable(name = "exhibit_tags")
    private List<Tag> tags;

    public Exhibit(String title, String description, User author) {
        LOG.info("Creating exhibit with paramaterized ctor");
        this.id = 0;
        this.title = title;
        this.description = description;
        this.author = author;
        this.created = Instant.now();
        this.supports = new ArrayList<>();
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public User getAuthor() { return author; }
    public Instant getCreated() { return created; }
    public List<Support> getSupports() { return supports; }
    public List<Tag> getTags() { return tags; }

    public void setId(int id) { this.id = id; }
    public void setTitle(String value) { this.title = value; }
    public void setDescription(String value) { this.description = value; }
    public void setAuthor(User value) { this.author = value; }
    public void setCreated(Instant value) { this.created = value; }
    public void setSupports(List<Support> value) { this.supports = value; }
    public void setTags(List<Tag> tags) {
        if (tags.size() > MAX_TAGS) {
            throw new IllegalArgumentException("Exhibits cannot have more than " + MAX_TAGS + " tags");
        }
        this.tags = tags;
    }

    @Override
    public String toString() {
        return String.format("Exhibit %d: '%s' by %s}", getId(), getTitle(), getAuthor());
    }

    public void addSupporter(User user) {
        this.supports.add(new Support(this, user));
    }
    
    public void removeSupporter(User user) {
        this.supports.removeIf(s -> s.user.getUsername().equals(user.getUsername()));
    }
}
