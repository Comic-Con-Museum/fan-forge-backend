package integration.then;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.As;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.Format;
import com.tngtech.jgiven.format.PrintfFormatter;
import com.tngtech.jgiven.integration.spring.JGivenStage;
import org.json.JSONException;
import org.junit.Assert;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.mock.web.MockHttpServletResponse;
import util.JoiningFormatter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

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
    
    public ThenJsonResponse bodyMatches(JsonNode root) throws IOException, JSONException {
        JSONAssert.assertEquals(root.toString(), response.getContentAsString(), false);
        return this;
    }
    
    public ThenJsonResponse bodyIsExactly(JsonNode root) throws IOException, JSONException {
        JSONAssert.assertEquals(root.toString(), response.getContentAsString(), true);
        return this;
    }
    
    @As("body doesn't contain")
    public ThenJsonResponse bodyDoesntContain(
            @Format(value = JoiningFormatter.Array.class, args = { "." }) String... path
    ) throws IOException {
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
