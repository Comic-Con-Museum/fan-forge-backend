package org.comic_con.museum.fcb.dal;

import org.comic_con.museum.fcb.models.Exhibit;
import org.comic_con.museum.fcb.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExhibitQueryBean {
    public static final int PAGE_SIZE = 10;
    
    public enum FeedType {
        NEW("created DESC"),
        ALPHABETICAL("title DESC");
        
        private final String orderBy;
        
        FeedType(String orderBy) { this.orderBy = orderBy; }
        
        private String getOrderBy() { return this.orderBy; }
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

    private static class ExhibitMapper implements RowMapper<Exhibit> {
        @Override
        public Exhibit mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Exhibit(
                    rs.getInt("eid"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getInt("author"),
                    // TODO: getting a java.util.Date and converting to Instant may have issues
                    rs.getTimestamp("created").toInstant(),
                    (String[]) rs.getArray("tags").getArray()
            );
        }
    }
    
    public void setupExhibitTable(boolean reset) {
        if (reset) {
            sql.execute("DROP TABLE IF EXISTS exhibits");
        }
        sql.execute(
                "CREATE TABLE IF NOT EXISTS exhibits ( " +
                "    eid SERIAL PRIMARY KEY, " +
                "    title VARCHAR(255) NOT NULL, " +
                "    description TEXT NOT NULL, " +
                "    author SERIAL ,"+//TODO REFERENCES users(uid) ON DELETE SET NULL ON UPDATE CASCADE, " +
                "    created TIMESTAMP WITH TIME ZONE NOT NULL, " +
                // TODO Once we figure out how we want tags to work, we can make this better
                "    tags TEXT ARRAY " +
                ")"
        );
    }
    
    public Exhibit getById(long id) {
        return sql.queryForObject(
                "SELECT * FROM exhibits WHERE eid = ?",
                new Object[] { id },
                new ExhibitMapper()
        );
    }

    public long create(Exhibit ex, User by) throws SQLException {
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
        int count = sql.update(
                "UPDATE exhibits " +
                "SET title = ?, " +
                "    description = ?, " +
                "    tags = ? " +
                "WHERE eid = ? " +
                "  AND author = ?",
                ex.getTitle(),
                ex.getDescription(),
                ex.getTags(),
                ex.getId(),
                by.getId()
        );
        if (count != 1) {
            throw new IncorrectUpdateSemanticsDataAccessException("More than one exhibit matched ID " + ex.getId());
        }
    }
    
    public void delete(long eid, User by) {
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
        return sql.query(
                "SELECT * FROM exhibits " +
                // this concatenation isn't a SQL injection vulnerability because it's
                // not user input; the value is hard-coded into the FeedType enum.
                "ORDER BY " + type.getOrderBy() + " " +
                "LIMIT " + PAGE_SIZE + " " +
                "OFFSET ?",
                new ExhibitMapper(),
                startIdx
        );
    }
    
    public long getCount() throws DataAccessException {
        Long count = sql.queryForObject("SELECT COUNT(*) FROM exhibits;", Long.class);
        if (count == null) {
            throw new EmptyResultDataAccessException("Somehow no count returned", 1);
        }
        return count;
    }
}
