package org.comic_conmuseum.fan_forge.backend.persistence;

import org.comic_conmuseum.fan_forge.backend.endpoints.responses.SurveyAggregate;
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
import java.util.stream.Collectors;

@Repository
public class SupportQueryBean {
    private static final Logger LOG = LoggerFactory.getLogger("persist.support");
    
    private final NamedParameterJdbcTemplate sql;

    public SupportQueryBean(NamedParameterJdbcTemplate sql) {
        this.sql = sql;
    }
    
    private static final String POPULATIONS_COLUMN_DEFS =
            Arrays.stream(Survey.Population.values())
                    .map(pop -> pop.column() + " BOOLEAN NOT NULL")
                    .collect(Collectors.joining(", "));
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
                "    visits INTEGER NOT NULL CHECK (1 <= visits AND visits <= 10), " +
                "    rating INTEGER NOT NULL CHECK (0 <= rating AND rating <= 10), " +
                "    " + POPULATIONS_COLUMN_DEFS + ", " +
                // we shouldn't have the same person supporting the same exhibit more than once
                "    CONSTRAINT support_once_per_exhibit UNIQUE (exhibit, supporter)" +
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
    
    private static final String POPULATIONS_COLUMN_NAMES =
            Arrays.stream(Survey.Population.values())
                    .map(Survey.Population::column)
                    .collect(Collectors.joining(", "));
    private static final String POPULATIONS_PARAMS =
            Arrays.stream(Survey.Population.values())
                    .map(Survey.Population::sqlParam)
                    .collect(Collectors.joining(", "));
    private static final String POPULATIONS_SETTERS =
            Arrays.stream(Survey.Population.values())
                    // pop_foo = COALESCE(:pop_foo, pop_foo);
                    .map(p -> p.column() + " = COALESCE(" + p.sqlParam() + ", s." + p.column() + ")")
                    .collect(Collectors.joining(", "));
    public boolean createSupport(long eid, Survey survey) {
        LOG.info("{} supporting {}", survey.supporter, eid);
        try {
            MapSqlParameterSource params =
                    new MapSqlParameterSource("exhibit", eid)
                            .addValue("supporter", survey.supporter)
                            .addValue("visits", survey.visits)
                            .addValue("rating", survey.rating);
            for (Survey.Population pop : Survey.Population.values()) {
                params.addValue(pop.column(), survey.populations.get(pop.display()));
            }
            sql.update(
                    "INSERT INTO supports AS s (" +
                    "    exhibit, supporter, visits, rating, " + POPULATIONS_COLUMN_NAMES +
                    ") " +
                    "VALUES (" +
                    "    :exhibit, :supporter, :visits, :rating, " +
                    "    " + POPULATIONS_PARAMS +
                    ") " +
                    "ON CONFLICT ON CONSTRAINT support_once_per_exhibit DO UPDATE SET " +
                    "  visits = COALESCE(:visits, s.visits), " +
                    "  rating = COALESCE(:rating, s.rating), " +
                    POPULATIONS_SETTERS,
                    params
            );
            return true;
        } catch (DuplicateKeyException e) {
            LOG.info("Already supporting that exhibit");
            return false;
        }
    }
    
    public Survey getSupportSurvey(long eid, User by) {
        LOG.info("Getting survey on {} by {}", eid, by.getUsername());
        return sql.queryForObject(
                "SELECT * FROM supports " +
                "WHERE exhibit = :eid AND supporter = :user",
                new MapSqlParameterSource()
                        .addValue("eid", eid)
                        .addValue("user", by.getId()),
                Survey::new
        );
    }
    
    public boolean deleteSupport(long eid, User by) {
        LOG.info("User {} no longer supports {}", by.getUsername(), eid);
        int removed = sql.update(
                "DELETE FROM supports " +
                "WHERE exhibit = :eid AND supporter = :supporter",
                new MapSqlParameterSource("eid", eid)
                        .addValue("supporter", by.getId())
                        .addValue("isAdmin", by.isAdmin())
        );
        return removed == 1;
    }

    public List<Survey> getSurveys(long eid) {
        LOG.info("Getting surveys for exhibit {}", eid);

        return sql.query(
                "SELECT * FROM supports " +
                "WHERE exhibit = :eid",
                new MapSqlParameterSource("eid", eid),
                Survey::new
        );
    }
    
    private int getNPS(long eid, long total) {
        Long npsCount = sql.queryForObject(
                "SELECT SUM(CASE " +
                "         WHEN rating >= 9 THEN 1 " +
                "         WHEN rating <= 6 THEN -1 " +
                "         ELSE 0 " +
                "       END) " +
                "FROM supports " +
                "WHERE exhibit = :eid",
                new MapSqlParameterSource("eid", eid),
                Long.class
        );
        if (npsCount == null) {
            throw new EmptyResultDataAccessException("Somehow no COUNT(*) returned", 1);
        }
        return Math.round(100 * (float) npsCount / total);
    }
    
    public int getNPS(long eid) {
        return getNPS(eid, this.getSupporterCount(eid));
    }
    
    private static final String POPULATION_COUNT_QUERIES =
            Arrays.stream(Survey.Population.values())
                    .map(p -> "COUNT(sid) FILTER (WHERE " + p.column() + ") AS " + p.column() + "_count")
                    .collect(Collectors.joining(", "));
    public SurveyAggregate getAggregateData(long eid) {
        LOG.info("Getting survey aggregate data for {}", eid);
        
        MapSqlParameterSource params = new MapSqlParameterSource("eid", eid);
        
        long total = this.getSupporterCount(eid);
        
        int nps = this.getNPS(eid, total);
        
        Long[] expVisitCounts = sql.queryForList(
                "SELECT COUNT(sid) AS count " +
                "FROM GENERATE_SERIES(0, 9) r " +
                "LEFT JOIN supports ON visits = r " +
                "      AND exhibit = :eid " +
                "GROUP BY r " +
                "ORDER BY r ",
                params,
                Long.class
        ).toArray(new Long[0]);
        double[] expVisitPercents = Arrays.stream(expVisitCounts)
                .mapToDouble(l -> (double) l / total)
                .toArray();
        
        Map<String, Float> expPopulations = sql.queryForObject(
                "SELECT " + POPULATION_COUNT_QUERIES + " " +
                "FROM supports WHERE exhibit = :eid",
                params,
                (rs, rn) -> {
                    Map<String, Float> ret = new HashMap<>();
                    for (Survey.Population pop : Survey.Population.values()) {
                        ret.put(pop.display(), (float) rs.getLong(pop.column() + "_count") / total);
                    }
                    return ret;
                }
        );
        
        return new SurveyAggregate(total, nps, expVisitPercents, expPopulations);
    }
}
