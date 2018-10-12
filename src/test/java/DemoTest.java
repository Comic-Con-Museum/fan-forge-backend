import org.junit.Assume;
import org.junit.Test;

public class DemoTest {
    @Test
    public void demoTest() {
        System.out.println("This is a test!");
    }
    
    @Test
    public void demoIgnoredTest() {
        Assume.assumeTrue("This is skipped because the condition is false", false);
    }
}
