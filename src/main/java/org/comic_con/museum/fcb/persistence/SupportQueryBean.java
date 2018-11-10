package org.comic_con.museum.fcb.persistence;

import org.comic_con.museum.fcb.models.Exhibit;
import org.comic_con.museum.fcb.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.*;

@Repository
public class SupportQueryBean {
    private static final Logger LOG = LoggerFactory.getLogger("query.support");
    
    private final NamedParameterJdbcTemplate sql;
    
    private List<Long> getIds(List<Exhibit> exhibits) {
        List<Long> ids = new ArrayList<>(exhibits.size());
        for (Exhibit ex : exhibits) {
            ids.add(ex.getId());
        }
        return ids;
    }
    
    public SupportQueryBean(NamedParameterJdbcTemplate jdbcTemplate) {
        this.sql = jdbcTemplate;
    }
    
    public void setupTable(boolean reset) {
        LOG.info("Creating tables; resetting: {}", reset);
        if (reset) {
            this.sql.execute("DROP TABLE IF EXISTS supports CASCADE", new HashMap<>(), PreparedStatement::execute);
        }
        this.sql.execute(
                "CREATE TABLE IF NOT EXISTS supports ( " +
                "    sid SERIAL PRIMARY KEY, " +
                "    exhibit SERIAL REFERENCES exhibits(eid) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "    supporter TEXT ,"+//TODO SERIAL REFERENCES users(uid) ON DELETE CASCADE ON UPDATE CASCADE, " +
                // TODO Get actual survey data fields to use and use them
                "    survey_data TEXT, " +
                // we shouldn't have the same person supporting the same exhibit more than once
                "    UNIQUE (exhibit, supporter)" +
                ")", new HashMap<>(), PreparedStatement::execute
        );
    }
    
    public Boolean isSupporting(User user, Exhibit exhibit) {
        return isSupportingById(user, exhibit.getId());
    }
    
    public Boolean isSupportingById(User user, long exhibit) {
        if (user == null) {
            LOG.info("Checked for anon support");
            return null;
        }
        LOG.info("Checking if {} supports {}", user.getUsername(), exhibit);
        Boolean supportCount = sql.queryForObject(
                "SELECT COUNT(*) = 1 " +
                "FROM supports " +
                "WHERE exhibit = :eid " +
                "  AND supporter = :uid",
                new MapSqlParameterSource()
                        .addValue("eid", exhibit)
                        .addValue("uid", user.getId()),
                Boolean.class
        );
        if (supportCount == null) {
            throw new EmptyResultDataAccessException("Somehow no COUNT(*)=1 returned", 1);
        }
        return supportCount;
    }
    
    public Map<Long, Boolean> isSupporting(User user, List<Exhibit> exhibits) {
        return isSupportingByIds(user, getIds(exhibits));
    }
    
    public Map<Long, Boolean> isSupportingByIds(User user, List<Long> exhibits) {
        if (user == null) {
            LOG.info("Getting {} supports for anon user", exhibits.size());
            return new HashMap<>();
        }
        LOG.info("Getting if {} is supporting {} exhibits", user.getUsername(), exhibits.size());
        return sql.query(
                "SELECT eid, COUNT(sid) = 1 AS supporting " +
                "FROM exhibits " +
                "LEFT JOIN supports " +
                "       ON eid = exhibit " +
                "      AND supporter = :uid " +
                "WHERE eid IN (:eids) " +
                "GROUP BY eid;",
                new MapSqlParameterSource()
                        .addValue("eids", exhibits)
                        .addValue("uid", user.getId()),
                rs -> {
                    Map<Long, Boolean> results = new HashMap<>();
                    while (rs.next()) {
                        results.put(rs.getLong("eid"), rs.getBoolean("supporting"));
                    }
                    return results;
                }
        );
    }
    
    public long supporterCount(Exhibit exhibit) {
        return supporterCountById(exhibit.getId());
    }
    
    public long supporterCountById(long exhibit) {
        LOG.info("Getting supporter count for {}", exhibit);
        Long supporterCount = sql.queryForObject(
                "SELECT COUNT(*) FROM supports WHERE exhibit = :eid",
                new MapSqlParameterSource("eid", exhibit),
                Long.class
        );
        if (supporterCount == null) {
            throw new EmptyResultDataAccessException("Somehow no COUNT(*)=1 returned", 1);
        }
        return supporterCount;
    }
    
    public Map<Long, Integer> supporterCounts(List<Exhibit> exhibits) {
        return supporterCountsByIds(getIds(exhibits));
    }
    
    public Map<Long, Integer> supporterCountsByIds(List<Long> exhibits) {
        LOG.info("Getting supporter counts for {} exhibits", exhibits.size());
        return sql.query(
                "SELECT eid, COUNT(sid) AS supporters " +
                "FROM exhibits " +
                "LEFT JOIN supports ON eid = exhibit " +
                "WHERE eid IN (:eids)" +
                "GROUP BY eid;",
                new MapSqlParameterSource()
                        .addValue("eids", exhibits),
                rs -> {
                    Map<Long, Integer> results = new HashMap<>();
                    while (rs.next()) {
                        results.put(rs.getLong("eid"), rs.getInt("supporters"));
                    }
                    return results;
                }
        );
    }
    
    public boolean support(long eid, User by, String survey) {
        LOG.info("{} supporting {}; survey: {}", by.getUsername(), eid, survey);
        try {
            sql.update(
                    "INSERT INTO supports ( " +
                    "    exhibit, supporter, survey_data " +
                    ") VALUES ( " +
                    "    :eid, :uid, :survey " +
                    ")",
                    new MapSqlParameterSource()
                            .addValue("eid", eid)
                            .addValue("uid", by.getId())
                            .addValue("survey", survey)
            );
            return true;
        } catch (DuplicateKeyException e) {
            LOG.info("Already supporting that exhibit");
            return false;
        }
    }
    
    public boolean unsupport(long eid, User by) {
        LOG.info("User {} no longer supports {}", by.getUsername(), eid);
        int removed = sql.update(
                "DELETE FROM supports " +
                "WHERE exhibit = :eid " +
                "  AND supporter = :uid",
                new MapSqlParameterSource()
                        .addValue("eid", eid)
                        .addValue("uid", by.getId())
        );
        return removed == 1;
    }
}
