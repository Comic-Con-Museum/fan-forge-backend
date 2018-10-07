package org.comic_con.museum.fcb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class Application implements CommandLineRunner {
    private Logger LOG = LoggerFactory.getLogger(Application.class);

    @Autowired
    JdbcTemplate jdbcTemplate;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        LOG.info("Demoing DB connection:");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS demo (id INTEGER PRIMARY KEY)");
        jdbcTemplate.execute("INSERT INTO demo VALUES (1), (2), (3)");
        jdbcTemplate.query(
                "SELECT * FROM demo",
                (rs, rowNum) -> String.valueOf(rs.getInt("id"))
        ).forEach(LOG::info);
    }
}
