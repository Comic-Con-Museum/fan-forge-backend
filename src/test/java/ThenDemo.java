import com.tngtech.jgiven.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThenDemo extends Stage<ThenDemo> {
    private static final Logger LOG = LoggerFactory.getLogger(ThenDemo.class);
    
    public ThenDemo expectADemo(String whatDemo) {
        LOG.info("Expected a demo: {}", whatDemo);
        return self();
    }
}
