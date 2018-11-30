package integration;

import integration.test.ExhibitEndpointTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ExhibitEndpointTests.class,
})
public class IntegrationSuite { /* blank intentionally */ }
