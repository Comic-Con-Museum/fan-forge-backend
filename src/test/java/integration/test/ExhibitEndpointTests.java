package integration.test;

import integration.IntegrationTestContext;
import integration.then.ThenJsonResponse;
import integration.when.WhenEndpointHit;
import integration.given.GivenDB;
import org.comic_conmuseum.fan_forge.backend.Application;
import org.comic_conmuseum.fan_forge.backend.models.Artifact;
import org.comic_conmuseum.fan_forge.backend.models.Exhibit;
import org.comic_conmuseum.fan_forge.backend.models.Survey;
import org.comic_conmuseum.fan_forge.backend.models.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;
import states.FanForgeTest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static util.JsonGenerator.*;

@SpringBootTest(
        classes = {WebApplicationContext.class, IntegrationTestContext.class},
        properties = {
                "ff.add-test-data=false", "ff.reset-on-start=true",
                "ff.close-on-init-fail=false", "ff.require-https=false"
        }
)
@EnableAutoConfiguration
@EnableWebSecurity
@ComponentScan(basePackageClasses = Application.class)
@RunWith(SpringRunner.class)
public class ExhibitEndpointTests extends FanForgeTest<GivenDB, WhenEndpointHit, ThenJsonResponse> {
    public ExhibitEndpointTests() throws ClassNotFoundException { }
    
    @Test
    public void nonexistentExhibitGivesA404() throws Exception {
        given()
                .exhibitDoesntExist(0);
        
        when()
                .get("/exhibit/0");
        
        then()
                .statusIs(404);
    }
    
    @Test
    public void existingExhibitResultIsValid() throws Exception {
        Exhibit val = new Exhibit(
                0, "a title", "and a description", "me!",
                Instant.ofEpochSecond(200), new String[] { "a", "b" },
                null, false
        );
        
        given()
                .exhibitExists(val).and()
                .noSupportsFor(val.getId()).and()
                .noCommentsFor(val.getId()).and()
                .noArtifactsFor(val.getId());
        
        when()
                .get("/exhibit/0");
        
        then()
                .statusIs(200).and()
                .bodyIsExactly(o(
                        // all the data is correct
                        p("id", v(0)),
                        p("title", v("a title")),
                        p("description", v("and a description")),
                        p("featured", v(false)),
                        p("author", v("me!")),
                        p("created", v("1970-01-01T00:03:20Z")),
                        p("tags", a(v("a"), v("b"))),
                        // and we don't have any mysterious extra stuff
                        p("supporters", v(0)),
                        p("artifacts", a()),
                        p("comments", a())
                )).and()
                // request isn't authorized, so we shouldn't have this
                .bodyDoesntContain("isSupported");
    }
    
    @Test
    public void authedWithoutSupport() throws Exception {
        Exhibit val = new Exhibit(
                0, "a title", "and a description", "me!",
                Instant.ofEpochSecond(200), new String[] { "a", "b" },
                null, false
        );
        
        given()
                .authTokenExists("auth", new User("auth", "auth", "auth", false)).and()
                .exhibitExists(val);
        
        when()
                .get("/exhibit/0").withAuthToken("auth");
        
        then()  .statusIs(200).and()
                .bodyMatches(o(
                        p("supported", v(false))
                ));
    }
    
    @Test
    public void withLoginAndSupportShowsSupported() throws Exception {
        Exhibit val = new Exhibit(
                0, "a title", "and a description", "me!",
                Instant.ofEpochSecond(200), new String[] { "a", "b" },
                null, false
        );
        Map<String, Boolean> pops = new HashMap<>();
        for (Survey.Population pop : Survey.Population.values()) {
            pops.put(pop.display(), false);
        }
        
        given()
                .authTokenExists("auth", new User("auth", "auth", "auth", false)).and()
                .exhibitExists(val).and()
                .supportExists(val.getId(), new Survey(4, pops, 8, "auth"));
        
        when()
                .get("/exhibit/0").withAuthToken("auth");
        
        then()
                .statusIs(200).and()
                .bodyMatches(o(
                        p("supported", v(true)),
                        p("supporters", v(1))
                ));
    }
    
    @Test
    public void withLoginAndOtherSupportShowsNotSupported() throws Exception {
        Exhibit val = new Exhibit(
                0, "a title", "and a description", "me!",
                Instant.ofEpochSecond(200), new String[] { "a", "b" },
                null, false
        );
        Map<String, Boolean> pops = new HashMap<>();
        for (Survey.Population pop : Survey.Population.values()) {
            pops.put(pop.display(), false);
        }
        
        given()
                .authTokenExists("auth", new User("auth", "auth", "auth", false)).and()
                .exhibitExists(val).and()
                .supportExists(val.getId(), new Survey(4, pops, 8, "someone else"));
        
        when()
                .get("/exhibit/0").withAuthToken("auth");
        
        then()
                .statusIs(200).and()
                .bodyMatches(o(
                        p("supported", v(false)),
                        p("supporters", v(1))
                ));
    }
    
    @Test
    public void artifactListCorrect() throws Exception {
        Exhibit val = new Exhibit(
                0, "a title", "and a description", "me!",
                Instant.ofEpochSecond(200), new String[] { "a", "b" },
                null, false
        );
        
        given()
                .authTokenExists("auth", new User("auth", "auth", "auth", false)).and()
                .exhibitExists(val).and()
                .artifactExists(new Artifact(
                        0, "this is one artifact", "it has a description",
                        false, "non-cover author", 0, Instant.ofEpochSecond(300)
                )).and()
                .artifactExists(new Artifact(
                        1, "this is another artifact", "it has a different description",
                        true, "cover author", 0, Instant.ofEpochSecond(400)
                ));
        
        when()
                .get("/exhibit/0").withAuthToken("auth");
        
        then()
                .statusIs(200).and()
                .bodyMatches(o(p("artifacts", a(
                        o(
                                p("title", v("this is one artifact")),
                                p("description", v("it has a description")),
                                p("image", v(0)), // for now at least, same as ID -- this may change!
                                p("creator", v("non-cover author")),
                                p("id", v(0)),
                                p("cover", v(false)),
                                p("created", v("1970-01-01T00:05:00Z"))
                        ),
                        o(
                                p("title", v("this is another artifact")),
                                p("description", v("it has a different description")),
                                p("image", v(1)),
                                p("creator", v("cover author")),
                                p("id", v(1)),
                                p("cover", v(true)),
                                p("created", v("1970-01-01T00:06:40Z"))
                        )
                )))).and()
                .bodyDoesntContain("artifacts", "3");
    }
}
