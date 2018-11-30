package integration.then;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;

import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.*;

public class ThenJsonResponse extends Stage<ThenJsonResponse> {
    @ExpectedScenarioState
    HttpServletResponse response;
    
    public ThenJsonResponse statusIs(int status) {
        assertEquals(status, response.getStatus());
        return this;
    }
}
