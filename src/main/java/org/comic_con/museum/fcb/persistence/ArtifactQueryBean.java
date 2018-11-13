package org.comic_con.museum.fcb.persistence;

import org.comic_con.museum.fcb.models.Artifact;
import org.comic_con.museum.fcb.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ArtifactQueryBean {
    private static final Logger LOG = LoggerFactory.getLogger("persist.artifacts");
    
    private final JdbcTemplate sql;
    private final SimpleJdbcInsert insert;
    
    private static class Mapper implements RowMapper<Artifact> {
        @Override
        public Artifact mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Artifact(
                    rs.getLong("aid"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getBoolean("cover"),
                    rs.getLong("image_id"),
                    rs.getString("creator"),
                    rs.getTimestamp("created").toInstant()
            );
        }
    }
    
    @Autowired
    public ArtifactQueryBean(JdbcTemplate jdbcTemplate) {
        this.sql = jdbcTemplate;
        this.insert = new SimpleJdbcInsert(sql)
                .withTableName("artifacts")
                .usingGeneratedKeyColumns("aid");
    }
    
    public void setupTable(boolean reset) {
        LOG.info("Creating tables; resetting: {}", reset);
        if (reset) {
            sql.execute("DROP TABLE IF EXISTS artifacts CASCADE");
        }
        sql.execute(
                "CREATE TABLE IF NOT EXISTS artifacts ( " +
                "   aid SERIAL PRIMARY KEY, " +
                "   title VARCHAR(255) NOT NULL, " +
                "   description TEXT NOT NULL, " +
                "   cover BOOLEAN NOT NULL, " +
                "   image_id INTEGER NOT NULL, " +
                "   creator TEXT ,"+//TODO INTEGER REFERENCES users(uid) ON DELETE SET NULL ON UPDATE CASCADE, " +
                "   exhibit SERIAL REFERENCES exhibits(eid) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "   created TIMESTAMP WITH TIME ZONE NOT NULL " +
                ")"
        );
    }
    
    public List<Artifact> artifactsOfExhibit(long id) {
        LOG.info("Getting artifact with ID {}", id);
        return sql.query(
                "SELECT * FROM artifacts " +
                "WHERE exhibit = ?",
                new Object[] { id },
                new Mapper()
        );
    }
    
    public long create(Artifact ar, long ex, User by) throws SQLException {
        LOG.info("{} creating artifact '{}'", by.getUsername(), ar.getTitle());
        Instant now = Instant.now();
        Map<String, Object> args = new HashMap<>();
        args.put("title", ar.getTitle());
        args.put("description", ar.getDescription());
        args.put("cover", ar.isCover());
        args.put("image_id", ar.getImage());
        args.put("creator", by.getId());
        args.put("created", new java.sql.Date(ar.getCreated().toEpochMilli()));
        args.put("exhibit", ex);
        Number key = insert.executeAndReturnKey(args);
        if (key == null) {
            throw new SQLException("Failed to insert rows (no key generated)");
        }
        long id = key.longValue();
        ar.setId(id);
        ar.setCreated(now);
        return id;
    }
}