package org.comic_conmuseum.fan_forge.backend.persistence;

import org.comic_conmuseum.fan_forge.backend.models.Exhibit;
import org.comic_conmuseum.fan_forge.backend.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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
        recent("created", true),
        popular("(SELECT COUNT(*) FROM supports WHERE exhibit = eid)", true);
        // comments will be, roughly:
        //comments("(SELECT COUNT(*) FROM comments WHERE exhibit = eid)", true)
        
        private final String orderBy;
        private final boolean defaultDesc;
        
        FeedType(String orderBy, boolean defaultDesc) {
            this.orderBy = orderBy + " ";
            this.defaultDesc = defaultDesc;
        }
        
        public static FeedType parse(String name) {
            for (FeedType feed : FeedType.values()) {
                if (feed.name().equals(name)) {
                    return feed;
                }
            }
            return null;
        }
        
        private String getSql(boolean inverted) {
            // XOR is magic, don't question it
            return this.orderBy + (defaultDesc ^ inverted ? "DESC" : "ASC");
        }
    }

    private final NamedParameterJdbcTemplate sql;
    private final SimpleJdbcInsert insert;
    
    @Autowired
    public ExhibitQueryBean(NamedParameterJdbcTemplate sql) {
        this.sql = sql;
        this.insert = new SimpleJdbcInsert(sql.getJdbcTemplate())
                .withTableName("exhibits")
                .usingGeneratedKeyColumns("eid");
    }

    public void setupTable(boolean reset) {
        LOG.info("Creating tables; resetting: {}", reset);
        if (reset) {
            sql.execute("DROP TABLE IF EXISTS exhibits CASCADE", PreparedStatement::execute);
        }
        sql.execute(
                "CREATE TABLE IF NOT EXISTS exhibits ( " +
                "   eid SERIAL PRIMARY KEY, " +
                "   featured BOOLEAN NOT NULL, " +
                "   title VARCHAR(255) NOT NULL, " +
                "   description TEXT NOT NULL, " +
                "   author TEXT ,"+//TODO INTEGER REFERENCES users(uid) ON DELETE SET NULL ON UPDATE CASCADE, " +
                // TODO Once we figure out how we want tags to work, we can make this better
                "   tags TEXT ARRAY, " +
                "   created TIMESTAMP WITH TIME ZONE NOT NULL " +
                ")", PreparedStatement::execute
        );
    }
    
    public Exhibit get(long exhibitId) {
        LOG.info("Getting exhibit with ID {}", exhibitId);
        return sql.queryForObject(
                "SELECT e.*, " +
                "       a.aid aid, a.title atitle, a.description adesc, " +
                "       a.creator acreator, a.created acreated " +
                "FROM exhibits e " +
                "LEFT JOIN artifacts a " +
                "       ON a.exhibit = e.eid " +
                "      AND a.cover " +
                "WHERE eid = :id",
                new MapSqlParameterSource("id", exhibitId),
                Exhibit::new
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
        args.put("featured", false);
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

        final int count = sql.update(
                "UPDATE exhibits " +
                "SET title = COALESCE(:title, title), " +
                "    description = COALESCE(:description, description), " +
                "    tags = COALESCE(:tags, tags) " +
                "WHERE eid = :exhibit AND (author = :user OR :isAdmin)",
                new MapSqlParameterSource()
                        .addValue("title", ex.getTitle())
                        .addValue("description", ex.getDescription())
                        .addValue("tags", ex.getTags())
                        .addValue("exhibit", ex.getId())
                        .addValue("user", by.getId())
                        .addValue("isAdmin", by.isAdmin())
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
        
        final int count = sql.update(
                "DELETE FROM exhibits " +
                "WHERE eid = :exhibit " +
                "  AND (author = :user OR :isAdmin)",
                new MapSqlParameterSource()
                        .addValue("exhibit", eid)
                        .addValue("user", by.getId())
                        .addValue("isAdmin", by.isAdmin())
        );
        
        if (count > 1) {
            throw new IncorrectUpdateSemanticsDataAccessException("More than one exhibit matched ID " + eid);
        }
        if (count == 0) {
            throw new EmptyResultDataAccessException("No exhibits with ID " + eid + " by " + by.getUsername(), 1);
        }
    }
    
    private static void addFilters(Map<String, String> filters, StringBuilder query, MapSqlParameterSource params) {
        // 1=1 so we can start with `AND` and forget about it)
        query.append(" WHERE 1=1 ");
        
        if (filters.containsKey("tag")) {
            query.append("AND :tag = ANY(e.tags) ");
            params.addValue("tag", filters.get("tag"));
        }
    
        if (filters.containsKey("author")) {
            query.append("AND author = :author ");
            params.addValue("author", filters.get("author"));
        }
    }
    
    public List<Exhibit> getFeed(FeedType type, int startIdx, Map<String, String> filters) {
        LOG.info("Getting {} feed", type);

        StringBuilder query = new StringBuilder(
                "SELECT e.*, a.aid aid, a.title atitle, a.description adesc, " +
                "       a.creator acreator, a.created acreated " +
                "FROM exhibits e " +
                "LEFT JOIN artifacts a " +
                "       ON a.exhibit = e.eid " +
                "      AND a.cover "
        );
        MapSqlParameterSource params = new MapSqlParameterSource();
        
        addFilters(filters, query, params);
        
        // TODO Add support for reversing
        query.append("ORDER BY ").append(type.getSql(false)).append(" LIMIT :limit OFFSET :offset");
        params.addValue("limit", PAGE_SIZE);
        params.addValue("offset", startIdx);
        
        return sql.query(query.toString(), params, Exhibit::new);
    }

    public long getCount(Map<String, String> filters) throws DataAccessException {
        LOG.info("Getting total exhibit count");
        
        StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM exhibits e ");
        MapSqlParameterSource params = new MapSqlParameterSource();
        
        addFilters(filters, query, params);
        
        Long count = sql.queryForObject(query.toString(), params, Long.class);
        if (count == null) {
            throw new EmptyResultDataAccessException("Somehow no count returned", 1);
        }
        return count;
    }

    public List<String> getAllTags() {
        LOG.info("Getting all tags");
        return sql.queryForList(
                "SELECT DISTINCT UNNEST(tags) t FROM exhibits ORDER BY t ASC",
                new MapSqlParameterSource(),
                String.class
        );
    }

    /** Marks the exhibit by id as featured. If it is already, there is no effect */
    public boolean markFeatured(long eid) {
        LOG.info("Marking exhibit {} as featured", eid);
        int count = sql.update("UPDATE exhibits " +
                "SET featured = TRUE " +
                "WHERE eid = :exhibit",
                new MapSqlParameterSource("exhibit", eid));
        return count == 1;
    }

    /** Removes the featured status of an exhibit by id. If it is not featured, there is no effect */
    public boolean deleteFeatured(long eid) {
        LOG.info("Marking exhibit {} as featured", eid);
        int count = sql.update("UPDATE exhibits " +
                        "SET featured = FALSE " +
                        "WHERE eid = :exhibit",
                new MapSqlParameterSource("exhibit", eid));
        return count == 1;
    }
}
