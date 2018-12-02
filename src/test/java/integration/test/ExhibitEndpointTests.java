package integration.test;

import com.tngtech.jgiven.integration.spring.SpringScenarioTest;
import integration.IntegrationTestContext;
import integration.then.ThenJsonResponse;
import integration.when.WhenEndpointHit;
import integration.given.GivenDB;
import org.comic_conmuseum.fan_forge.backend.Application;
import org.comic_conmuseum.fan_forge.backend.models.Exhibit;
import org.comic_conmuseum.fan_forge.backend.models.Survey;
import org.comic_conmuseum.fan_forge.backend.models.User;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static util.JsonGenerator.*;

@SpringBootTest(
        classes = {WebApplicationContext.class, IntegrationTestContext.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"ff.add-test-data=false", "ff.reset-on-start=true"}
)
@EnableAutoConfiguration
@EnableWebSecurity
@ComponentScan(basePackageClasses = Application.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ExhibitEndpointTests extends SpringScenarioTest<GivenDB, WhenEndpointHit, ThenJsonResponse> {
    @Test
    public void nonexistentExhibitGives404() {
        given()
                .exhibitDoesntExist(0);
        
        when()
                .get("/exhibit/0");
        
        then()
                .statusIs(404);
    }
    
    @Test
    public void existingExhibitGivesGoodBody() throws IOException, JSONException {
        Exhibit val = new Exhibit(
                0, "a title", "and a description", "me!",
                Instant.ofEpochSecond(200), new String[] { "a", "b" },
                null, false
        );
        
        given()
                .exhibitExists(val).and()
                .noSupportersFor(val.getId()).and()
                .noCommentsFor(val.getId()).and()
                .noArtifactsFor(val.getId());
        
        when()
                .get("/exhibit/0");
        
        then()
                .statusIs(200).and()
                .bodyMatches(o(
                        // all the data is correct
                        p("id", v(0)),
                        p("title", v("a title")),
                        p("description", v("and a description")),
                        p("supporters", v(0)),
                        p("featured", v(false)),
                        p("author", v("me!")),
                        p("created", v("1970-01-01T00:03:20Z")),
                        p("tags", a(v("a"), v("b"))),
                        // and we don't have any mysterious extra stuff
                        p("artifacts", a()),
                        p("comments", a())
                )).and()
                // request isn't authorized, so we shouldn't have this
                .bodyDoesntContain("isSupported");
    }
    
    @Test
    public void withLoginButNoSupportShowsNotSupported() throws IOException, JSONException {
        Exhibit val = new Exhibit(
                0, "a title", "and a description", "me!",
                Instant.ofEpochSecond(200), new String[] { "a", "b" },
                null, false
        );
        
        given()
                .exhibitExists(val).and()
                .noSupportersFor(val.getId()).and()
                .noCommentsFor(val.getId()).and()
                .noArtifactsFor(val.getId()).and()
                .authExists("auth", new User("auth", "auth", "auth", false));
        
        when()
                .get("/exhibit/0").withAuth("auth");
        
        then()
                .statusIs(200).and()
                .bodyMatches(o(
                        p("supported", v(false))
                ));
    }
    
    @Test
    public void withLoginAndSupportShowsSupported() throws IOException, JSONException {
        Exhibit val = new Exhibit(
                0, "a title", "and a description", "me!",
                Instant.ofEpochSecond(200), new String[] { "a", "b" },
                null, false
        );
        Map<String, Boolean> pops = new HashMap<>();
        for (Survey.Population pop : Survey.Population.values()) {
            pops.put(pop.displayName(), false);
        }
        
        given()
                .authExists("auth", new User("auth", "auth", "auth", false)).and()
                .exhibitExists(val).and()
                .supportExists(val.getId(), new Survey(4, pops, 8, "auth"))
                .noCommentsFor(val.getId()).and()
                .noArtifactsFor(val.getId()).and();
        
        when()
                .get("/exhibit/0").withAuth("auth");
        
        then()
                .statusIs(200).and()
                .bodyMatches(o(
                        p("supported", v(true))
                ));
    }
}
