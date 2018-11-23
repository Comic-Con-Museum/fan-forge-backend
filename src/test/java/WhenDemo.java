import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;

public class WhenDemo extends Stage<WhenDemo> {
    @ProvidedScenarioState
    @ExpectedScenarioState
    String demoName;
    
    public WhenDemo theDemoRuns() {
        return this;
    }
}
