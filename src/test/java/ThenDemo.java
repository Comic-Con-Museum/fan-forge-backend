import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import org.junit.jupiter.api.Assertions;

public class ThenDemo extends Stage<ThenDemo> {
    @ExpectedScenarioState
    String demoName;
    
    public ThenDemo theDemoWasGood() {
        Assertions.assertTrue("good".equalsIgnoreCase(demoName), "The demo was not named 'good'");
        return this;
    }
    
    public ThenDemo theDemoWasNotGood() {
        Assertions.assertFalse("good".equalsIgnoreCase(demoName), "The demo was named 'good'");
        return this;
    }
}
