package integration.given;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.integration.spring.JGivenStage;
import org.comic_conmuseum.fan_forge.backend.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@JGivenStage
public class GivenDB extends Stage<GivenDB> {
    @Autowired
    NamedParameterJdbcTemplate sql;
    
    public GivenDB exhibitDoesntExist(long id) {
        sql.update(
                "DELETE FROM exhibits WHERE eid = :cid",
                new MapSqlParameterSource("eid", id)
        );
        return this;
    }
    
    public GivenDB authExists(String token, User user) {
        // TODO add token to DB when that's a thing
        return this;
    }
}
