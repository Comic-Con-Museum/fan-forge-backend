package org.comic_con.museum.fcb.dal;

import org.comic_con.museum.fcb.models.Exhibit;
import org.comic_con.museum.fcb.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SupportQueryBean {
    private static final Logger LOG = LoggerFactory.getLogger("query.support");
    
    private final JdbcTemplate sql;
    
    public SupportQueryBean(JdbcTemplate jdbcTemplate) {
        this.sql = jdbcTemplate;
    }
    
    public void setupTable(boolean reset) {
        LOG.info("Creating tables; resetting: {}", reset);
        if (reset) {
            this.sql.execute("DROP TABLE IF EXISTS supports CASCADE");
        }
        this.sql.execute(
                "CREATE TABLE IF NOT EXISTS supports ( " +
                "    exhibit SERIAL REFERENCES exhibits(eid) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "    supporter SERIAL ,"+//TODO REFERENCES users(uid) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "    survey_data TEXT, " + // TODO Get actual survey data fields to use and use them
                "    UNIQUE (exhibit, supporter)" +
                ")"
        );
    }
    
    public boolean isSupporting(User user, Exhibit exhibit) {
        LOG.info("Checking if {} supports {}", user.getUsername(), exhibit.getId());
        Integer supportCount = sql.queryForObject(
                "SELECT COUNT(*) FROM supports WHERE exhibit = ? AND supporter = ?",
                Integer.class,
                exhibit.getId(), user.getId()
        );
        if (supportCount == null) {
            throw new EmptyResultDataAccessException("Somehow no count returned", 1);
        }
        return supportCount == 1;
    }
    
    public long supporterCount(Exhibit exhibit) {
        LOG.info("Getting supporter count for {}", exhibit.getId());
        Long supporterCount = sql.queryForObject("SELECT COUNT(*) WHERE exhibit = ?", Long.class, exhibit.getId());
        if (supporterCount == null) {
            throw new EmptyResultDataAccessException("Somehow no count returned", 1);
        }
        return supporterCount;
    }
    
    public boolean support(long eid, User by, String survey) {
        LOG.info("{} supporting {}; survey: {}", by.getUsername(), eid, survey);
        try {
            sql.update(
                    "INSERT INTO supports (" +
                    "    exhibit, supporter, survey_data" +
                    ") VALUES (" +
                    "    ?, ?, ?" +
                    ")",
                    eid, by.getId(), survey
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
                "DELETE FROM supports" +
                "WHERE exhibit = ?" +
                "  AND supporter = ?",
                eid, by.getId()
        );
        return removed == 1;
    }
}
