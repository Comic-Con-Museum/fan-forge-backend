package org.comic_con.museum.fcb.persistence;

import org.comic_con.museum.fcb.models.Artifact;
import org.comic_con.museum.fcb.models.Exhibit;
import org.comic_con.museum.fcb.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ExhibitQueryBean {
    private static final Logger LOG = LoggerFactory.getLogger("persist.exhibits");
    
    public static final int PAGE_SIZE = 10;
    
    public enum FeedType {
        NEW("created DESC"),
        ALPHABETICAL("title DESC");
        
        private final String orderBy;
        
        FeedType(String orderBy) { this.orderBy = orderBy; }
        
        private String getSql() { return this.orderBy; }
    }
    
    private final JdbcTemplate sql;
    private final SimpleJdbcInsert insert;
    
    @Autowired
    public ExhibitQueryBean(JdbcTemplate jdbcTemplate) {
        this.sql = jdbcTemplate;
        this.insert = new SimpleJdbcInsert(sql)
                .withTableName("exhibits")
                .usingGeneratedKeyColumns("eid");
    }

    // TODO Switch mappers to lambdas/method references?
    private static class Mapper implements RowMapper<Exhibit> {
        @Override
        public Exhibit mapRow(ResultSet rs, int rowNum) throws SQLException {
            Artifact cover;
            if (rs.getString("atitle") == null) {
                cover = null;
            } else {
                cover = new Artifact(
                        rs.getLong("aid"),
                        rs.getString("atitle"),
                        rs.getString("adesc"),
                        true, // always true; we only get covers in this bean
                        rs.getString("acreator"),
                        rs.getTimestamp("acreated").toInstant()
                );
            }
            
            return new Exhibit(
                    rs.getInt("eid"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("author"),
                    // TODO: getting a java.sql.Timestamp and converting to Instant may have issues
                    rs.getTimestamp("created").toInstant(),
                    (String[]) rs.getArray("tags").getArray(),
                    cover
            );
        }
    }
    
    public void setupTable(boolean reset) {
        LOG.info("Creating tables; resetting: {}", reset);
        if (reset) {
            sql.execute("DROP TABLE IF EXISTS exhibits CASCADE");
        }
        sql.execute(
                "CREATE TABLE IF NOT EXISTS exhibits ( " +
                "   eid SERIAL PRIMARY KEY, " +
                "   title VARCHAR(255) NOT NULL, " +
                "   description TEXT NOT NULL, " +
                "   author TEXT ,"+//TODO INTEGER REFERENCES users(uid) ON DELETE SET NULL ON UPDATE CASCADE, " +
                "   tags TEXT ARRAY, " +
                "   created TIMESTAMP WITH TIME ZONE NOT NULL " +
                // TODO Once we figure out how we want tags to work, we can make this better
                ")"
        );
    }
    
    public Exhibit getById(long id) {
        LOG.info("Getting exhibit with ID {}", id);
        return sql.queryForObject(
                "SELECT e.*, " +
                "       a.aid aid, a.title atitle, a.description adesc, " +
                "       a.creator acreator, a.created acreated " +
                "FROM exhibits e " +
                "LEFT JOIN artifacts a " +
                "       ON a.exhibit = e.eid " +
                "      AND a.cover " +
                "WHERE eid = ?",
                new Object[] { id },
                new Mapper()
        );
    }

    public long create(Exhibit ex, User by) throws SQLException {
        LOG.info("{} creating exhibit '{}'", by.getUsername(), ex.getTitle());
        Instant now = Instant.now();
        Map<String, Object> args = new HashMap<>();
        args.put("title", ex.getTitle());
        args.put("description", ex.getDescription());
        args.put("author", by.getId());
        args.put("created", new java.sql.Date(ex.getCreated().toEpochMilli()));
        args.put("tags", ex.getTags());
        Number key = insert.executeAndReturnKey(args);
        if (key == null) {
            throw new SQLException("Failed to insert rows (no key generated)");
        }
        long id = key.longValue();
        ex.setId(id);
        ex.setCreated(now);
        return id;
    }
    
    public void update(Exhibit ex, User by) {
        LOG.info("{} updating exhibit {}", by.getUsername(), ex.getId());
        
        int count = sql.update(
                "UPDATE exhibits " +
                "SET title = COALESCE(?, title), " +
                "    description = COALESCE(?, description), " +
                "    tags = COALESCE(?, tags) " +
                "WHERE eid = ? " +
                "  AND author = ?",
                ex.getTitle(),
                ex.getDescription(),
                ex.getTags(),
                ex.getId(),
                by.getId()
        );
        if (count == 0) {
            throw new EmptyResultDataAccessException("No exhibits updated. Does the author own the exhibit?", 1);
        }
        if (count > 1) {
            throw new IncorrectUpdateSemanticsDataAccessException("More than one exhibit matched ID " + ex.getId());
        }
    }
    
    public void delete(long eid, User by) {
        LOG.info("{} deleting exhibit {}", by.getUsername(), eid);
        
        int count = sql.update(
                "DELETE FROM exhibits " +
                "WHERE eid = ? " +
                "  AND author = ?",
                eid,
                by.getId()
        );
        if (count > 1) {
            throw new IncorrectUpdateSemanticsDataAccessException("More than one exhibit matched ID " + eid);
        }
        if (count == 0) {
            throw new EmptyResultDataAccessException("No exhibits with ID " + eid + " by " + by.getUsername(), 1);
        }
    }
    
    public List<Exhibit> getFeedBy(FeedType type, int startIdx) {
        LOG.info("Getting {} feed", type);
        
        return sql.query(
                "SELECT e.*, a.aid aid, a.title atitle, a.description adesc, " +
                "       a.creator acreator, a.created acreated " +
                "FROM exhibits e " +
                "LEFT JOIN artifacts a " +
                "       ON a.exhibit = e.eid " +
                "      AND a.cover " +
                "ORDER BY " + type.getSql() + " " +
                "LIMIT ? " +
                "OFFSET ?",
                new Mapper(),
                PAGE_SIZE,
                startIdx
        );
    }
    
    public long getCount() throws DataAccessException {
        LOG.info("Getting total exhibit count");
        
        Long count = sql.queryForObject("SELECT COUNT(*) FROM exhibits;", Long.class);
        if (count == null) {
            throw new EmptyResultDataAccessException("Somehow no count returned", 1);
        }
        return count;
    }
}
