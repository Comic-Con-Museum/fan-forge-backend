package org.comic_con.museum.fcb.endpoints.responses;

import org.comic_con.museum.fcb.models.Comment;

public class CommentView {
    public final long id;
    public final String text;
    public final String author;
    public final Long reply;
    
    public CommentView(Comment from) {
        this.id = from.getId();
        this.text = from.getText();
        this.author = from.getAuthor();
        this.reply = from.getReply();
    }
}
