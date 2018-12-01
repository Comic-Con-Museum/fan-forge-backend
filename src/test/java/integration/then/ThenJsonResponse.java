package integration.then;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.integration.spring.JGivenStage;

import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.*;

@JGivenStage
public class ThenJsonResponse extends Stage<ThenJsonResponse> {
    @ExpectedScenarioState
    HttpServletResponse response;
    
    public ThenJsonResponse statusIs(int status) {
        assertEquals(status, response.getStatus());
        return this;
    }
}
