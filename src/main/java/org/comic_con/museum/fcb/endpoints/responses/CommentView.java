package org.comic_con.museum.fcb.endpoints.responses;

public class CommentView {
    public final long id;
    public final String text;
    public final String author;
    public final Long reply;
    
    public CommentView(long id, String text, String author, Long reply) {
        this.id = id;
        this.text = text;
        this.author = author;
        this.reply = reply;
    }
}
