package org.comic_con.museum.fcb;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.comic_con.museum.fcb.dal.SupportQueryBean;
import org.comic_con.museum.fcb.models.Exhibit;
import org.comic_con.museum.fcb.models.User;
import org.comic_con.museum.fcb.dal.ExhibitQueryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@SpringBootApplication
public class Application implements CommandLineRunner {
    private final Logger LOG = LoggerFactory.getLogger("application");

    @Value("${s3.access-key}")
    private String accessKey;

    @Value("${s3.secret-key}")
    private String secretKey;

    @Value("${s3.url}")
    private String url;

    @Value("${s3.region}")
    private String region;
    
    @Value("${fcb.reset-on-start}")
    private boolean resetOnStart;
    
    @Value("${fcb.add-test-data}")
    private boolean addTestData;
    
    private final ExhibitQueryBean exhibits;
    private final SupportQueryBean supports;
    
    public Application(ExhibitQueryBean exhibits, SupportQueryBean supports) {
        this.exhibits = exhibits;
        this.supports = supports;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    private void addTestData() throws SQLException {
        if (!addTestData) return;
        
        final List<String> exhibitTitles = Arrays.asList(
                "Hello, World!",
                "smook",
                "Batman in the 1960s",
                "Jason Voorhees",
                "Yahtzee Croshaw",
                "A banana",
                "Why Sonic sucks",
                "Why Sonic rules",
                "Help, I've fallen and I can't get up!",
                "HI, BILLY MAYS HERE!",
                "Have you ever CCIDENTALLY HIT CAPSLOCK ISNTEAD OF a",
                "How post-2008 retro-terminal-colored ASCII art affected mid-2010s Batman linework",
                "~none of those are good exhibit titles, I'm sorry"
        );
        Collections.shuffle(exhibitTitles);
        
        User original = new User("nic".hashCode(), "nic", null, false);
        User[] supporters = IntStream.range(0, 5)
                .mapToObj(i -> new User(i, "user" + i, null, false))
                .toArray(User[]::new);
        for (int exIdx = 0; exIdx < exhibitTitles.size(); ++exIdx) {
            String title = exhibitTitles.get(exIdx);
            long newId = exhibits.create(new Exhibit(
                    0, title, "Description for " + title, 0,
                    Instant.now().minus(exIdx, ChronoUnit.DAYS),
                    new String[] { "post", "exhibit", "index:" + exIdx }
            ), original);
            for (int sIdx = 0; sIdx < supporters.length; ++sIdx) {
                if ((exIdx & sIdx) == sIdx) {
                    supports.support(
                            new Exhibit(newId, null, null, 0, null, null),
                            supporters[sIdx],
                            String.format("Support for %d by %s", newId, supporters[sIdx].getUsername())
                    );
                }
            }
        }
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            LOG.info("Initializing DB");
            exhibits.setupExhibitTable(resetOnStart);
            addTestData();
            LOG.info("Done initializing DB");
        } catch (Exception e) {
            LOG.error("Failed while initializing DB", e);
            throw e; // crash on error, but log it first
        }
        
        try {
            LOG.info("Demoing S3 connection:");
            AWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);
            AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(creds))
                    .withEndpointConfiguration(
                            new AwsClientBuilder.EndpointConfiguration(
                                    url, region
                            )
                    )
                    .build();
            s3.listBuckets().forEach(b -> LOG.info(b.getName()));
            LOG.info("Done with S3 stuff");
        } catch (Exception e) {
            LOG.error("Non-fatal error during S3 demo: {}", e.getMessage());
        }
    }
}
