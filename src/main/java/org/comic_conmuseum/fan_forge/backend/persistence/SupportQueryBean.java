package org.comic_conmuseum.fan_forge.backend.persistence;

import org.comic_conmuseum.fan_forge.backend.models.Exhibit;
import org.comic_conmuseum.fan_forge.backend.models.Survey;
import org.comic_conmuseum.fan_forge.backend.models.User;
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
    private static final Logger LOG = LoggerFactory.getLogger("persist.support");
    
    private final NamedParameterJdbcTemplate sql;

    public SupportQueryBean(NamedParameterJdbcTemplate sql) {
        this.sql = sql;
    }
    
    public void setupTable(boolean reset) {
        LOG.info("Creating tables; resetting: {}", reset);
        if (reset) {
            this.sql.execute("DROP TABLE IF EXISTS supports CASCADE", PreparedStatement::execute);
        }
        this.sql.execute(
                "CREATE TABLE IF NOT EXISTS supports ( " +
                "    sid SERIAL PRIMARY KEY, " +
                "    exhibit SERIAL REFERENCES exhibits(eid) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "    supporter TEXT ,"+//TODO SERIAL REFERENCES users(uid) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "    visits INTEGER NOT NULL CHECK (0 <= visits AND visits <= 10), " +
                "    pop_male BOOLEAN NOT NULL, " +
                "    pop_female BOOLEAN NOT NULL, " +
                "    pop_kids BOOLEAN NOT NULL, " +
                "    pop_teenagers BOOLEAN NOT NULL, " +
                "    pop_adults BOOLEAN NOT NULL, " +
                "    nps INTEGER NOT NULL CHECK (0 <= nps AND nps <= 10), " +
        // we shouldn't have the same person supporting the same exhibit more than once
                "    UNIQUE (exhibit, supporter)" +
                ")",
                PreparedStatement::execute
        );
    }
    
    public Boolean isSupportingExhibit(User user, Exhibit exhibit) {
        return isSupportingExhibit(user, exhibit.getId());
    }
    
    private Boolean isSupportingExhibit(User user, long exhibit) {
        if (user.isAnonymous()) {
            LOG.info("Checked for anon support");
            return null;
        }
        LOG.info("Checking if {} supports {}", user.getUsername(), exhibit);


        Integer countSupported = sql.queryForObject(
                "SELECT COUNT(*) FROM supports " +
                        "WHERE exhibit = :eid AND supporter = :supporter",
                new MapSqlParameterSource("eid" ,exhibit)
                        .addValue("supporter", user.getId()),
                Integer.class
        );
        if (countSupported == null) {
            throw new EmptyResultDataAccessException("Somehow no COUNT(*) returned", 1);
        }
        return countSupported == 1;
    }
    
    public long getSupporterCount(Exhibit exhibit) {
        return getSupporterCount(exhibit.getId());
    }
    
    private long getSupporterCount(long exhibit) {
        LOG.info("Getting supporter count for {}", exhibit);
        Long supporterCount = sql.queryForObject(
                "SELECT COUNT(*) FROM supports WHERE exhibit = :eid",
                new MapSqlParameterSource("eid", exhibit),
                Long.class
        );
        if (supporterCount == null) {
            throw new EmptyResultDataAccessException("Somehow no COUNT(*) returned", 1);
        }
        return supporterCount;
    }

    public boolean createSupport(long eid, Survey survey) {
        LOG.info("{} supporting {}; survey: {}", by.getUsername(), eid, survey);
        try {
            sql.update(
                    "INSERT INTO supports (" +
                    "    exhibit, supporter, visits, pop_male, pop_female, pop_kids, pop_teenagers, pop_adults, nps" +
                    ") " +
                    "VALUES (" +
                    "    :exhibit, :supporter, :visits, " +
                    "    :pop_male, :pop_female, :pop_kids, :pop_teenagers, :pop_adults, " +
                    "    :nps " +
                    ")",
                    new MapSqlParameterSource("exhibit", eid)
                            .addValue("supporter", survey.supporter)
                            .addValue("visits", survey.visits)
                            .addValue("pop_male", survey.populations.get("male"))
                            .addValue("pop_female", survey.populations.get("female"))
                            .addValue("pop_kids", survey.populations.get("kids"))
                            .addValue("pop_teenagers", survey.populations.get("teenagers"))
                            .addValue("pop_adults", survey.populations.get("adults"))
                            .addValue("nps", survey.nps)
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
                "WHERE exhibit = :eid AND (supporter = :supporter OR :isAdmin)",
                new MapSqlParameterSource("eid", eid)
                        .addValue("supporter", by.getId())
                        .addValue("isAdmin", by.isAdmin())
        );
        return removed == 1;
    }

    public List<Survey> getSurveys(long eid) {
        LOG.info("Getting surveys for exhibit {}", eid);

        return sql.query(
                "SELECT supporter, visits, pop_male, pop_female, pop_kids, pop_teenagers, pop_adults FROM supports " +
                "WHERE exhibit = :eid",
                new MapSqlParameterSource("eid", eid),
                Survey::new
        );
    }
}
