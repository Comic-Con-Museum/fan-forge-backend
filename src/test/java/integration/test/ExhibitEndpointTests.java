package integration.test;

import com.tngtech.jgiven.integration.spring.SpringScenarioTest;
import integration.IntegrationTestContext;
import integration.then.ThenJsonResponse;
import integration.when.WhenEndpointHit;
import integration.given.GivenDB;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(classes = {WebApplicationContext.class, IntegrationTestContext.class})
@RunWith(SpringRunner.class)
@WebAppConfiguration
public class ExhibitEndpointTests extends SpringScenarioTest<GivenDB, WhenEndpointHit, ThenJsonResponse> {
    @Test
    public void nonexistentExhibitGives404() {
        given().exhibitDoesntExist(0);
        when().request(HttpMethod.GET, "/exhibit/0");
        then().statusIs(404);
    }
}
