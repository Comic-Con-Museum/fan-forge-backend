package org.comic_con.museum.fcb.persistence;

import org.comic_con.museum.fcb.models.Comment;
import org.comic_con.museum.fcb.models.Exhibit;
import org.comic_con.museum.fcb.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class CommentQueryBean {
    private static final Logger LOG = LoggerFactory.getLogger("persist.comments");
    
    private final NamedParameterJdbcTemplate sql;
    private final SimpleJdbcInsert insert;
    
    @Autowired
    public CommentQueryBean(NamedParameterJdbcTemplate sql) {
        this.sql = sql;
        this.insert = new SimpleJdbcInsert(sql.getJdbcTemplate())
                .withTableName("comments")
                .usingGeneratedKeyColumns("cid");
    }
    
    private static Comment mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Comment(
                rs.getLong("cid"),
                rs.getString("text"),
                rs.getString("author"),
                rs.getObject("reply") == null ? null : rs.getLong("reply"),
                rs.getTimestamp("created").toInstant()
        );
    }
    
    public void setupTable(boolean resetOnStart) {
        if (resetOnStart) {
            LOG.info("Creating table; resetting {}", resetOnStart);
            sql.execute("DROP TABLE IF EXISTS comments CASCADE", PreparedStatement::execute);
        }
        sql.execute(
                "CREATE TABLE IF NOT EXISTS comments (" +
                "    cid SERIAL PRIMARY KEY, " +
                "    text TEXT NOT NULL, " +
                "    author TEXT ,"+//TODO INTEGER REFERENCES users(uid) ON DELETE SET NULL ON UPDATE CASCADE, " +
                "    exhibit INTEGER NOT NULL REFERENCES exhibits(eid) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "    reply INTEGER REFERENCES comments(cid) ON DELETE SET NULL ON UPDATE CASCADE," +
                "    created TIMESTAMP WITH TIME ZONE NOT NULL " +
                ")",
                PreparedStatement::execute
        );
    }
    
    public long commentCount(Exhibit exhibit) {
        Long count = sql.queryForObject(
                "SELECT COUNT(*) FROM comments " +
                "WHERE exhibit = :eid",
                new MapSqlParameterSource("eid", exhibit.getId()),
                Long.class
        );
        if (count == null) {
            throw new IllegalStateException("No count returned somehow");
        }
        return count;
    }
    
    public List<Comment> commentsOfExhibit(long id) {
        return sql.query(
                "SELECT * FROM comments " +
                "WHERE exhibit = :eid",
                new MapSqlParameterSource("eid", id),
                CommentQueryBean::mapRow
        );
    }
}
