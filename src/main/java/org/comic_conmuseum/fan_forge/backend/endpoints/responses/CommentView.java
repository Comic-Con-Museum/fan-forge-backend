package org.comic_conmuseum.fan_forge.backend.endpoints.responses;

import org.comic_conmuseum.fan_forge.backend.models.Comment;

import java.time.Instant;

public class CommentView {
    public final long id;
    public final String text;
    public final String author;
    public final Long reply;
    public final Instant created;
    
    public CommentView(Comment from) {
        this.id = from.getId();
        this.text = from.getText();
        this.author = from.getAuthor();
        this.reply = from.getReply();
        this.created = from.getCreated();
    }
}
