package org.comic_con.museum.fcb.models.dal;

import org.comic_con.museum.fcb.models.Exhibit;
import org.comic_con.museum.fcb.models.User;
import org.postgresql.jdbc.PgArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.Instant;

@Component
public class ExhibitQueryBean {
    public static final int PAGE_SIZE = 10;
    
    private JdbcTemplate sql;
    
    @Autowired
    public ExhibitQueryBean(JdbcTemplate jdbcTemplate) {
        this.sql = jdbcTemplate;
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
                    rs.getDate("created").toInstant(),
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
                // TODO Add REFERENCES users(uid) once the users table is built
                "    author SERIAL ,"+//REFERENCES users(uid) ON DELETE SET NULL ON UPDATE CASCADE, " +
                "    created TIMESTAMP WITH TIME ZONE NOT NULL, " +
                // TODO Once we figure out how we want tags to work, we can make this better
                "    tags VARCHAR(16) ARRAY " +
                ")"
        );
    }
    
    public Exhibit getById(long id) {
        return sql.queryForObject(
                "SELECT * FROM exhibits WHERE id = ?",
                new Object[] { id },
                new ExhibitMapper()
        );
    }

    public long create(Exhibit ex, User by) throws SQLException {
        Instant now = Instant.now();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        sql.update(
                conn -> {
                    PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO exhibits ( " +
                            "    title, description, author, created, tags " +
                            ") VALUES ( " +
                            "    ?, ?, ?, ?, ? " +
                            ")"
                    );
                    ps.setString(1, ex.getTitle());
                    ps.setString(2, ex.getDescription());
                    ps.setLong(3, by.getId());
                    ps.setTimestamp(4, Timestamp.from(now));
                    ps.setArray(5, conn.createArrayOf("VARCHAR(16)", ex.getTags()));
                    return ps;
                }, keyHolder
        );
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new SQLException("Failed to insert rows (no key generated)");
        }
        long id = key.longValue();
        ex.setId(id);
        ex.setCreated(now);
        return id;
    }
    
    public void update(Exhibit ex) {
        int count = sql.update(
                "UPDATE exhibits " +
                "SET title = :title, " +
                "    description = :description, " +
                "    author = :author, " +
                "    tags = :tags " +
                "WHERE id = :id",
                new BeanPropertySqlParameterSource(ex));
        if (count != 1) {
            throw new IncorrectUpdateSemanticsDataAccessException("More than one exhibit matched ID " + ex.getId());
        }
    }
}
