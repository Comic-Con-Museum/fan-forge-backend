package integration.test;

import com.tngtech.jgiven.integration.spring.SpringScenarioTest;
import integration.IntegrationTestContext;
import integration.then.ThenJsonResponse;
import integration.when.WhenEndpointHit;
import integration.given.GivenDB;
import org.comic_conmuseum.fan_forge.backend.config.BearerTokenAuthenticationProvider;
import org.comic_conmuseum.fan_forge.backend.config.WebSecurityConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(classes = {WebApplicationContext.class, IntegrationTestContext.class})
@WebAppConfiguration
@ContextConfiguration(classes = {WebSecurityConfig.class, BearerTokenAuthenticationProvider.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class ExhibitEndpointTests extends SpringScenarioTest<GivenDB, WhenEndpointHit, ThenJsonResponse> {
    @Test
    public void nonexistentExhibitGives404() throws Exception {
        given().exhibitDoesntExist(0);
        when().request(HttpMethod.GET, "/exhibit/0");
        then().statusIs(404);
    }
}
