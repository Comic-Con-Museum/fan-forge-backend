import com.tngtech.jgiven.junit5.ScenarioTest;
import org.junit.jupiter.api.Test;

public class DemoTest extends ScenarioTest<GivenDemo, WhenDemo, ThenDemo> {
    @Test
    public void theTestSystemWorks() {
        given() .someDemoThing("magic");
        
        when()  .aThingHappens("pixie dust");
        
        then()  .expectADemo("leprechaun bones")
        .and()  .expectADemo("mushroom circles");
    }
}
