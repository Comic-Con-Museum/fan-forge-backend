package org.comic_conmuseum.fan_forge.backend.persistence;

import org.comic_conmuseum.fan_forge.backend.models.Exhibit;
import org.comic_conmuseum.fan_forge.backend.models.Survey;
import org.comic_conmuseum.fan_forge.backend.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class SupportQueryBean {
    private static final Logger LOG = LoggerFactory.getLogger("persist.support");
    
    private final JdbcTemplate sql;

    public SupportQueryBean(JdbcTemplate sql) {
        this.sql = sql;
    }
    
    public void setupTable(boolean reset) {
        LOG.info("Creating tables; resetting: {}", reset);
        if (reset) {
            this.sql.execute("DROP TABLE IF EXISTS supports CASCADE");
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
                ")"
        );
    }
    
    public Boolean isSupportingExhibit(User user, Exhibit exhibit) {
        return isSupportingExhibit(user, exhibit.getId());
    }
    
    private Boolean isSupportingExhibit(User user, long exhibit) {
        if (user == null) {
            LOG.info("Checked for anon support");
            return null;
        }
        LOG.info("Checking if {} supports {}", user.getUsername(), exhibit);


        Integer countSupported = sql.queryForObject(
                "SELECT COUNT(*) FROM supports " +
                        "WHERE exhibit = ? AND supporter = ?",
                Integer.class,
                exhibit,
                user.getId()
        );
        if (countSupported == null) {
            throw new EmptyResultDataAccessException("Somehow no COUNT(*)=1 returned", 1);
        }
        return countSupported == 1;
    }
    
    public long getSupporterCount(Exhibit exhibit) {
        return getSupporterCount(exhibit.getId());
    }
    
    private long getSupporterCount(long exhibit) {
        LOG.info("Getting supporter count for {}", exhibit);
        Long supporterCount = sql.queryForObject(
                "SELECT COUNT(*) FROM supports WHERE exhibit = ?",
                Long.class,
                exhibit
        );
        if (supporterCount == null) {
            throw new EmptyResultDataAccessException("Somehow no COUNT(*)=1 returned", 1);
        }
        return supporterCount;
    }

    public boolean createSupport(long eid, User by, String survey) {
        LOG.info("{} supporting {}; survey: {}", by.getUsername(), eid, survey);
        try {
            sql.update(
                    "INSERT INTO supports (exhibit, supporter, survey_data) " +
                    "VALUES (?, ?, ?)",
                    eid,
                    by.getId(),
                    survey
            );
            return true;
        } catch (DuplicateKeyException e) {
            LOG.info("Already supporting that exhibit");
            return false;
        }
    }
    
    public boolean deleteSupport(long eid, User by) {
        LOG.info("User {} no longer supports {}", by.getUsername(), eid);
        int removed = sql.update(
                "DELETE FROM supports " +
                "WHERE exhibit = ? " +
                "  AND supporter = ?",
                eid,
                by.getId()
        );
        return removed == 1;
    }

    public List<Survey> getSurveys(long eid) {
        LOG.info("Getting surveys for exhibit {}", eid);

        return sql.query(
                "SELECT supporter, survey_data FROM supports WHERE exhibit = ?",
                Survey::new,
                eid
        );
    }
}
