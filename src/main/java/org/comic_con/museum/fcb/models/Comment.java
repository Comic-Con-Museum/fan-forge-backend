package org.comic_con.museum.fcb.models;

import java.time.Instant;

public class Comment {
    private long id;
    private String text;
    private String author;
    private Long reply;
    private Instant created;
    
    public Comment(long id, String text, String author, Long reply, Instant created) {
        this.id = id;
        this.text = text;
        this.author = author;
        this.reply = reply;
        this.created = created;
    }
    
    public long getId() { return id; }
    public String getText() { return text; }
    public String getAuthor() { return author; }
    public Long getReply() { return reply; }
    public Instant getCreated() { return created; }
    
    public void setId(long id) { this.id = id; }
    public void setText(String text) { this.text = text; }
    public void setAuthor(String author) { this.author = author; }
    public void setReply(Long reply) { this.reply = reply; }
    public void setCreated(Instant created) { this.created = created; }
}
