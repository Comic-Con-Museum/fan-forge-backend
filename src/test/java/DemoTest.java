import com.tngtech.jgiven.junit5.ScenarioTest;
import org.junit.jupiter.api.Test;

public class DemoTest extends ScenarioTest<GivenDemo, WhenDemo, ThenDemo> {
    @Test
    public void aGoodDemoIsGood() {
        given()
                .demoIsReady("good");
        
        when()
                .theDemoRuns();
        
        then()
                .theDemoWasGood();
    }
    
    @Test
    public void aNotGoodDemoIsNotGood() {
        given()
                .demoIsReady("not good");
        
        when()
                .theDemoRuns();
        
        then()
                .theDemoWasNotGood();
    }
}
