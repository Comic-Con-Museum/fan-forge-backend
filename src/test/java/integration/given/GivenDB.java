package integration.given;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.BeforeStage;
import com.tngtech.jgiven.integration.spring.JGivenStage;
import com.zaxxer.hikari.util.DriverDataSource;
import org.comic_conmuseum.fan_forge.backend.models.Exhibit;
import org.comic_conmuseum.fan_forge.backend.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.util.Properties;

@JGivenStage
public class GivenDB extends Stage<GivenDB> {
    @Value("${spring.datasource.url}") String sqlUrl;
    @Value("${spring.datasource.driver}") String sqlDriver;
    @Value("${spring.datasource.username}") String sqlUsername;
    @Value("${spring.datasource.password}") String sqlPassword;
    
    private NamedParameterJdbcTemplate makeSql() {
        return new NamedParameterJdbcTemplate(
                new JdbcTemplate(
                        new DriverDataSource(
                                sqlUrl, sqlDriver,
                                new Properties(),
                                sqlUsername, sqlPassword
                        )
                )
        );
    }
    
    private NamedParameterJdbcTemplate sql;
    
    @BeforeStage
    public void setUpSql() {
        this.sql = makeSql();
    }
    
    @Autowired
    WebApplicationContext wac;
    
    public GivenDB exhibitDoesntExist(long id) {
        sql.update(
                "DELETE FROM exhibits WHERE eid = :eid",
                new MapSqlParameterSource("eid", id)
        );
        return this;
    }
    
    public GivenDB exhibitExists(Exhibit ex) {
        sql.update(
                "INSERT INTO exhibits (" +
                "    eid, title, description, author, tags, created, featured" +
                ") VALUES (" +
                "    :id, :title, :description, :author, :tags, :created, :featured" +
                ")",
                new MapSqlParameterSource()
                        .addValue("id", ex.getId())
                        .addValue("title", ex.getTitle())
                        .addValue("description", ex.getDescription())
                        .addValue("author", ex.getAuthor())
                        .addValue("created", new java.sql.Date(ex.getCreated().toEpochMilli()))
                        .addValue("tags", ex.getTags())
                        .addValue("featured", false)

        );
        return this;
    }

    public GivenDB authExists(String token, User user) {
        // TODO add token to DB when that's a thing
        return this;
    }
    
    public GivenDB noSupportersFor(long id) {
        sql.update(
                "DELETE FROM supports WHERE exhibit = :id",
                new MapSqlParameterSource("id", id)
        );
        return this;
    }
    
    public GivenDB noCommentsFor(long id) {
        sql.update(
                "DELETE FROM comments WHERE exhibit = :id",
                new MapSqlParameterSource("id", id)
        );
        return this;
    }
    
    public GivenDB noArtifactsFor(long id) {
        sql.update(
                "DELETE FROM artifacts WHERE exhibit = :id",
                new MapSqlParameterSource("id", id)
        );
        return this;
    }
}
