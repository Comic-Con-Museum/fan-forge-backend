package integration.test;

import com.tngtech.jgiven.integration.spring.SpringScenarioTest;
import integration.IntegrationTestContext;
import integration.then.ThenJsonResponse;
import integration.when.WhenEndpointHit;
import integration.given.GivenDB;
import org.comic_conmuseum.fan_forge.backend.models.Exhibit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;

import static util.JsonGenerator.*;

@SpringBootTest(classes = {WebApplicationContext.class, IntegrationTestContext.class})
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
    public void existingExhibitGivesGoodBody() {
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
                        p("id", v(0)),
                        p("title", v("a title")),
                        p("description", v("and a description")),
                        p("supporters", v(0)),
                        p("comments", a()),
                        p("featured", v(true)),
                        p("author", v("me!")),
                        p("created", v("1970-01-01T00:03:20Z")),
                        p("tags", a(v("a"), v("b"))),
                        p("artifacts", a())
                ));
    }
}
