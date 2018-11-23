import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tngtech.jgiven.Stage;

public class GivenDemo extends Stage<GivenDemo> {
    private static final Logger LOG = LoggerFactory.getLogger(GivenDemo.class);
    
    public GivenDemo someDemoThing(String theThing) {
        LOG.info("Setting up demo with {}", theThing);
        return self();
    }
}
