package org.comic_conmuseum.fan_forge.backend.persistence;

import org.comic_conmuseum.fan_forge.backend.models.Comment;
import org.comic_conmuseum.fan_forge.backend.models.Exhibit;
import org.comic_conmuseum.fan_forge.backend.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public class CommentQueryBean {
    private static final Logger LOG = LoggerFactory.getLogger("persist.comments");
    
    private final JdbcTemplate sql;
    private final SimpleJdbcInsert insert;
    
    @Autowired
    public CommentQueryBean(JdbcTemplate sql) {
        this.sql = sql;
        this.insert = new SimpleJdbcInsert(sql)
                .withTableName("comments")
                .usingGeneratedKeyColumns("cid");
    }
    
    public void setupTable(boolean resetOnStart) {
        if (resetOnStart) {
            sql.execute("DROP TABLE IF EXISTS comments CASCADE");
        }
        sql.execute(
                "CREATE TABLE IF NOT EXISTS comments (" +
                "    cid SERIAL PRIMARY KEY, " +
                "    text TEXT NOT NULL, " +
                "    author TEXT ,"+//TODO INTEGER REFERENCES users(uid) ON DELETE SET NULL ON UPDATE CASCADE, " +
                "    exhibit INTEGER NOT NULL REFERENCES exhibits(eid) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "    reply INTEGER REFERENCES comments(cid) ON DELETE SET NULL ON UPDATE CASCADE," +
                "    created TIMESTAMP WITH TIME ZONE NOT NULL " +
                ")"
        );
    }
    
    public long getCommentCount(Exhibit exhibit) {
        LOG.info("Getting comment count of {}", exhibit.getId());
        Long count = sql.queryForObject(
                "SELECT COUNT(*) FROM comments WHERE exhibit = ?",
                Long.class,
                exhibit.getId()
        );
        if (count == null) {
            throw new IllegalStateException("No count returned somehow");
        }
        return count;
    }
    
    public List<Comment> getComments(long exhibitId) {
        return sql.query(
                "SELECT * FROM comments WHERE exhibit = ?",
                Comment::new,
                exhibitId
        );
    }
    
    public Comment get(long exhibitId) {
        LOG.info("Getting comment with ID {}", exhibitId);
        return sql.queryForObject(
                "SELECT * FROM comments WHERE cid = ?",
                Comment::new,
                exhibitId
        );
    }
    
    public long create(Comment co, long ex, User by) throws SQLException {
        LOG.info("{} creating comment on {}", by.getUsername(), ex);
        Number key = insert.executeAndReturnKey(new MapSqlParameterSource()
                .addValue("text", co.getText())
                .addValue("author", by.getId())
                .addValue("exhibit", ex)
                .addValue("reply", co.getReply())
                .addValue("created", new java.sql.Date(co.getCreated().toEpochMilli()))
        );
        if (key == null) {
            throw new SQLException("Failed to insert rows (no key generated)");
        }
        long id = key.longValue();
        co.setId(id);
        return id;
    }
    
    public void update(Comment co, User by) {
        LOG.info("{} updating comment {}", by.getUsername(), co.getId());
        int count = sql.update(
                "UPDATE comments SET text = ? " +
                "WHERE cid = ? AND (author = ? OR ?)",
                co.getText(),
                co.getId(),
                by.getId(),
                by.isAdmin()
        );
        if (count == 0) {
            throw new EmptyResultDataAccessException("No comments updated. Does the creator own the artifact?", 1);
        }
        if (count > 1) {
            throw new IncorrectUpdateSemanticsDataAccessException("More than one comment matched ID " + co.getId());
        }
    }
    
    public void delete(long id, User by) {
        LOG.info("{} deleting comment {}", by.getUsername(), id);
        
        final int count = sql.update(
                "DELETE FROM comments " +
                "WHERE cid = ? AND (author = ? OR ?)",
                id,
                by.getId(),
                by.isAdmin()
        );
        
        if (count > 1) {
            throw new IncorrectUpdateSemanticsDataAccessException("More than one comment matched ID " + id);
        }
        if (count == 0) {
            throw new EmptyResultDataAccessException("No comments with ID " + id + " by " + by.getUsername(), 1);
        }
    }
}
