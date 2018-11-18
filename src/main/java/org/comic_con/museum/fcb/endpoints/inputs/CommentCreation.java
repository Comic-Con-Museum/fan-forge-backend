package org.comic_con.museum.fcb.endpoints.inputs;

import org.comic_con.museum.fcb.models.Comment;
import org.comic_con.museum.fcb.models.User;

import java.time.Instant;

public class CommentCreation {
    private String text;
    private Long reply;
    private Long parent;
    
    public String getText() { return text; }
    public Long getReply() { return reply; }
    public Long getParent() { return parent; }
    
    public void setText(String text) { this.text = text; }
    public void setReply(Long reply) { this.reply = reply; }
    public void setParent(Long parent) { this.parent = parent; }
    
    public Comment build(User author) {
        return new Comment(0, text, author.getId(), reply, Instant.now());
    }
}
