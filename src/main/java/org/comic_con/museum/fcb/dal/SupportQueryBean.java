package org.comic_con.museum.fcb.dal;

import org.comic_con.museum.fcb.models.Exhibit;
import org.comic_con.museum.fcb.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SupportQueryBean {
    private static final Logger LOG = LoggerFactory.getLogger("query.support");
    
    private final JdbcTemplate sql;
    
    public SupportQueryBean(JdbcTemplate jdbcTemplate) {
        this.sql = jdbcTemplate;
    }
    
    public void setupSupportTable(boolean reset) {
        if (reset) {
            this.sql.execute("DROP TABLE IF EXISTS supports");
        }
        this.sql.execute(
                "CREATE TABLE IF NOT EXISTS supports ( " +
                "    exhibit SERIAL REFERENCES exhibits(eid) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "    supporter SERIAL ,"+//TODO REFERENCES users(uid) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "    survey_data TEXT" + // TODO Get actual survey data to use and use that
                ")"
        );
    }
    
    public boolean isSupporting(User user, Exhibit exhibit) {
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
        Long supporterCount = sql.queryForObject("SELECT COUNT(*) WHERE exhibit = ?", Long.class, exhibit.getId());
        if (supporterCount == null) {
            throw new EmptyResultDataAccessException("Somehow no count returned", 1);
        }
        return supporterCount;
    }
    
    public boolean support(Exhibit exhibit, User by, String survey) {
        int added = sql.update(
                "INSERT INTO supports (" +
                "    exhibit, supporter, survey_data" +
                ") VALUES (" +
                "    ?, ?, ?" +
                ")",
                exhibit.getId(), by.getId(), survey
        );
        return added == 1;
    }
}
