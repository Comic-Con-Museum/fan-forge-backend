package org.comic_con.museum.fcb.persistence;

import org.comic_con.museum.fcb.models.Comment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
                rs.getLong("id"),
                rs.getString("text"),
                rs.getString("author"),
                rs.getObject("reply") == null ? null : rs.getLong("reply"),
                rs.getTimestamp("created").toInstant()
        );
    }
    
    public void setupTable(boolean resetOnStart) {
        if (resetOnStart) {
            sql.execute("DROP TABLE IF EXISTS comments CASCADE", PreparedStatement::execute);
        }
        sql.execute(
                "CREATE TABLE IF NOT EXISTS comments (" +
                "    cid SERIAL PRIMARY KEY, " +
                "    text TEXT NOT NULL, " +
                "    author TEXT ,"+//TODO INTEGER REFERENCES users(uid) ON DELETE SET NULL ON UPDATE CASCADE, " +
                "    exhibit INTEGER NOT NULL REFERENCES exhibits(eid) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "    reply INTEGER REFERENCES comments(cid) ON DELETE SET NULL ON UPDATE CASCADE " +
                ")",
                PreparedStatement::execute
        );
    }
    
    
}
