import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;

public class GivenDemo extends Stage<GivenDemo> {
    @ProvidedScenarioState
    String demoName;
    
    public GivenDemo demoIsReady(String demoName) {
        this.demoName = demoName;
        return this;
    }
}
