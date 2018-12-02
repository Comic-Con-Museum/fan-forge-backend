package integration.when;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.AfterStage;
import com.tngtech.jgiven.annotation.As;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import com.tngtech.jgiven.integration.spring.JGivenStage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;

@JGivenStage
public class WhenEndpointHit extends Stage<WhenEndpointHit> {
    @ProvidedScenarioState
    MockHttpServletResponse response;
    
    private HttpMethod method;
    private String url;
    @As("$")
    public WhenEndpointHit request(HttpMethod method, String url) {
        this.method = method;
        this.url = url;
        return this;
    }
    
    @As("GET")
    public WhenEndpointHit get(String url) {
        return request(HttpMethod.GET, url);
    }
    
    @As("POST")
    public WhenEndpointHit post(String url) {
        return request(HttpMethod.POST, url);
    }
    
    @As("PUT")
    public WhenEndpointHit put(String url) {
        return request(HttpMethod.PUT, url);
    }
    
    private String authToken;
    public WhenEndpointHit withAuthToken(@Quoted String auth) {
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
