package integration.then;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.json.JSONException;
import org.junit.Assert;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Component;
import states.ExpectedScenarioState;
import states.Stage;

import java.io.IOException;

import static org.junit.Assert.*;

@Component
public class ThenJsonResponse extends Stage<ThenJsonResponse> {
    @ExpectedScenarioState
    private MockHttpServletResponse response;
    
    @Override
    public void reset() { }
    
    private static ObjectReader reader = new ObjectMapper().reader();
    
    public ThenJsonResponse statusIs(int status) {
        assertEquals(status, response.getStatus());
        return this;
    }
    
    public ThenJsonResponse bodyMatches(JsonNode root) throws IOException, JSONException {
        JSONAssert.assertEquals(root.toString(), response.getContentAsString(), false);
        return this;
    }
    
    public ThenJsonResponse bodyIsExactly(JsonNode root) throws IOException, JSONException {
        JSONAssert.assertEquals(root.toString(), response.getContentAsString(), true);
        return this;
    }
    
    public ThenJsonResponse bodyDoesntContain(String... path) throws IOException {
        JsonNode elem = reader.readTree(response.getContentAsString());
        for (String component : path) {
            if (component == null) {
                throw new IllegalArgumentException("null in path list");
            }
            if (elem instanceof ArrayNode) {
                // handle arrays specially to support foo.bar.4.a
                int idx;
                try {
                    idx = Integer.parseInt(component);
                } catch (NumberFormatException e) {
                    // object key definitely doesn't exist in an array
                    return this;
                }
                if (elem.has(idx)) {
                    elem = elem.get(idx);
                } else {
                    return this;
                }
            } else if (elem.has(component)) {
                elem = elem.get(component);
            } else {
                return this;
            }
        }
        // if we got through the whole tree, we shouldn't have
        Assert.fail("Body contained " + String.join(".", path));
        return this;
    }
}
