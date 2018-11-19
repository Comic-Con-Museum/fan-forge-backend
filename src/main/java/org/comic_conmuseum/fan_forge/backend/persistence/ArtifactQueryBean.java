package org.comic_conmuseum.fan_forge.backend.persistence;

import org.comic_conmuseum.fan_forge.backend.models.Artifact;
import org.comic_conmuseum.fan_forge.backend.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public class ArtifactQueryBean {
    private static final Logger LOG = LoggerFactory.getLogger("persist.artifacts");
    
    private final JdbcTemplate sql;
    private final SimpleJdbcInsert insert;

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
                "    aid SERIAL PRIMARY KEY, " +
                "    title VARCHAR(255) NOT NULL, " +
                "    description TEXT NOT NULL, " +
                "    cover BOOLEAN NOT NULL, " +
                "    creator TEXT ,"+//TODO INTEGER REFERENCES users(uid) ON DELETE SET NULL ON UPDATE CASCADE, " +
                "    exhibit SERIAL REFERENCES exhibits(eid) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "    created TIMESTAMP WITH TIME ZONE NOT NULL " +
                ");" +
                // Partial index to ensure no exhibits have more than one cover
                "CREATE UNIQUE INDEX one_cover_per_exhibit " +
                "ON artifacts(exhibit) " +
                "WHERE cover;"
        );
    }
    
    public List<Artifact> artifactsOfExhibit(long id) {
        LOG.info("Getting artifact with ID {}", id);
        return sql.query(
                "SELECT * FROM artifacts " +
                "WHERE exhibit = ?",
                Artifact::new,
                id
        );
    }
    
    public long create(Artifact ar, long ex, User by) throws SQLException {
        LOG.info("{} creating artifact '{}'", by.getUsername(), ar.getTitle());
        Number key = insert.executeAndReturnKey(new MapSqlParameterSource()
                .addValue("title", ar.getTitle())
                .addValue("description", ar.getDescription())
                .addValue("cover", ar.isCover())
                .addValue("creator", by.getId())
                .addValue("created", new java.sql.Date(ar.getCreated().toEpochMilli()))
                .addValue("exhibit", ex)
        );
        if (key == null) {
            throw new SQLException("Failed to insert rows (no key generated)");
        }
        long id = key.longValue();
        ar.setId(id);
        return id;
    }

    public void update(Artifact ar) {
        LOG.info("updating artifact {}", ar.getId());

        int count = sql.update(
                "UPDATE artifacts " +
                "SET title = COALESCE(?, title), " +
                "    description = COALESCE(?, description) " +
                "WHERE aid = ? ",
                ar.getTitle(),
                ar.getDescription(),
                ar.getId()
        );
        if (count == 0) {
            throw new EmptyResultDataAccessException("No artifacts updated. Does the creator own the artifact?", 1);
        }
        if (count > 1) {
            throw new IncorrectUpdateSemanticsDataAccessException("More than one exhibit matched ID " + ar.getId());
        }
    }

    public Artifact get(long id) {
        LOG.info("Getting artifact {}", id);
        return sql.queryForObject(
                "SELECT * FROM artifacts WHERE aid = ?",
                Artifact::new,
                id
        );
    }

    public void delete(long aid) {
        LOG.info("Deleting artifact {}", aid);
        int count = sql.update(
                "DELETE FROM artifacts WHERE aid = ?",
                aid
        );
        if (count > 1) {
            throw new IncorrectUpdateSemanticsDataAccessException("More than one exhibit matched ID " + aid);
        }
        if (count == 0) {
            throw new EmptyResultDataAccessException("No artifacts with ID " + aid, 1);
        }
    }
    
    public void deleteAllFromExcept(long exFrom, List<Long> except) {
        LOG.info("Deleting all artifacts of {} except {}", exFrom, except);
        if (except.isEmpty()) {
            sql.update(
                    "DELETE FROM artifacts WHERE exhibit = ? ",
                    exFrom
            );
        } else {
            sql.update(
                    "DELETE FROM artifacts WHERE exhibit = ? AND aid NOT IN (?)",
                    exFrom,
                    except
            );
        }
    }
}
