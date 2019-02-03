package integration.when;

import org.junit.After;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import states.AfterStage;
import states.ProvidedScenarioState;
import states.Stage;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;

@Component
public class WhenEndpointHit extends Stage<WhenEndpointHit> {
    @Override
    public void reset() {
        this.method = null;
        this.url = null;
        this.authToken = null;
    }
    
    @ProvidedScenarioState
    private MockHttpServletResponse response;
    
    private HttpMethod method;
    private String url;
    private String authToken;
    
    public WhenEndpointHit request(HttpMethod method, String url) {
        this.method = method;
        this.url = url;
        return this;
    }
    
    public WhenEndpointHit get(String url) {
        return request(HttpMethod.GET, url);
    }
    
    public WhenEndpointHit post(String url) {
        return request(HttpMethod.POST, url);
    }
    
    public WhenEndpointHit put(String url) {
        return request(HttpMethod.PUT, url);
    }
    
    public WhenEndpointHit withAuthToken(String auth) {
        this.authToken = auth;
        return this;
    }
    
    @Autowired
    WebApplicationContext wac;
    
    @AfterStage
    public void doRequest() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.request(method, url);
        if (authToken != null) {
            builder.header("Authorization", "Bearer " + authToken);
        }
        
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
        response = mvc.perform(builder).andReturn().getResponse();
    }
}
