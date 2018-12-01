package integration.then;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.integration.spring.JGivenStage;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.Assert.*;

@JGivenStage
public class ThenJsonResponse extends Stage<ThenJsonResponse> {
    @ExpectedScenarioState
    MockHttpServletResponse response;
    
    private static ObjectReader reader = new ObjectMapper().reader();
    
    public ThenJsonResponse statusIs(int status) {
        assertEquals(status, response.getStatus());
        return this;
    }
    
    public ThenJsonResponse bodyIs(JsonNode root) throws IOException, JSONException {
        JSONAssert.assertEquals(root.toString(), response.getContentAsString(), false);
        return this;
    }
}
