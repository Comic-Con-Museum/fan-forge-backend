import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tngtech.jgiven.Stage;

public class WhenDemo extends Stage<WhenDemo> {
    private static final Logger LOG = LoggerFactory.getLogger(GivenDemo.class);
    
    public WhenDemo aThingHappens(String theThing) {
        LOG.info("Setting up demo with {}", theThing);
        return self();
    }
}
