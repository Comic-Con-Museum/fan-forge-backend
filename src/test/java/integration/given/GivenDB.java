package integration.given;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.integration.spring.JGivenStage;
import org.comic_conmuseum.fan_forge.backend.models.User;
import org.comic_conmuseum.fan_forge.backend.persistence.ExhibitQueryBean;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@JGivenStage
public class GivenDB extends Stage<GivenDB> {
    @Autowired
    WebApplicationContext wac;
    
    public GivenDB exhibitDoesntExist(long id) throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.request(HttpMethod.DELETE, "/exhibit/0");
            builder.header("Authorization", "Bearer admin");

        MockMvc mvc = MockMvcBuilders.webAppContextSetup(wac).build();
        mvc.perform(builder).andReturn().getResponse();
        return this;
    }

    public GivenDB authExists(String token, User user) {
        // TODO add token to DB when that's a thing
        return this;
    }
}
