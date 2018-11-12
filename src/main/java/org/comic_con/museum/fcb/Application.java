package org.comic_con.museum.fcb;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.comic_con.museum.fcb.models.Artifact;
import org.comic_con.museum.fcb.persistence.ArtifactQueryBean;
import org.comic_con.museum.fcb.persistence.S3Bean;
import org.comic_con.museum.fcb.persistence.SupportQueryBean;
import org.comic_con.museum.fcb.models.Exhibit;
import org.comic_con.museum.fcb.models.User;
import org.comic_con.museum.fcb.persistence.ExhibitQueryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@SpringBootApplication(exclude = {ErrorMvcAutoConfiguration.class})
public class Application implements CommandLineRunner {
    private static final Logger LOG = LoggerFactory.getLogger("application");

    // TODO Migrate the S3 stuff to its own bean
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
    
    @Value("${fcb.close-on-init-fail}")
    private boolean closeOnInitFail;
    
    private final ExhibitQueryBean exhibits;
    private final SupportQueryBean supports;
    private final ArtifactQueryBean artifacts;
    private final S3Bean s3;
    private final ConfigurableApplicationContext ctx;
    
    @Autowired
    public Application(ExhibitQueryBean exhibits, SupportQueryBean supports, ArtifactQueryBean artifacts,
                       S3Bean s3, ConfigurableApplicationContext ctx) {
        this.exhibits = exhibits;
        this.supports = supports;
        this.artifacts = artifacts;
        this.s3 = s3;
        this.ctx = ctx;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    /**
     * A complex bunch of code that can all mostly be ignored. It just
     * populates the database with some random-looking data, so that the code
     * can be tested more easily.
     */
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
        
        User original = new User("nic", "nic", null, false);
        User[] supporters = IntStream.range(0, 5)
                .mapToObj(i -> new User(("user" + i), "user" + i, null, false))
                .toArray(User[]::new);
        int imageId = 0;
        for (int eIdx = 0; eIdx < exhibitTitles.size(); ++eIdx) {
            String title = exhibitTitles.get(eIdx);
            long newId = exhibits.create(new Exhibit(
                    0, title, "Description for " + title, original.getId(),
                    Instant.now().minus(eIdx % 4, ChronoUnit.DAYS).minus(eIdx / 4, ChronoUnit.HOURS),
                    new String[] { "post", "exhibit", "index:" + eIdx },
                    null
            ), original);
            for (int sIdx = 0; sIdx < supporters.length; ++sIdx) {
                if ((eIdx & sIdx) == sIdx) {
                    supports.support(
                            newId, supporters[sIdx],
                            String.format("Support for %d by %s", newId, supporters[sIdx].getUsername())
                    );
                }
            }
            for (int aIdx = 0; aIdx < eIdx % 4; ++aIdx) {
                artifacts.create(new Artifact(
                        0, "artifact " + aIdx + " of " + eIdx,
                        "description of artifact",
                        aIdx == 0, imageId++,
                        Instant.now()
                ), newId, supporters[aIdx]);
            }
        }
        LOG.info("Done adding test data");
    }

    @Override
    public void run(String... args) {
        try {
            LOG.info("Initializing DB");
            // Order is important! Some tables depend on others.
            exhibits.setupTable(resetOnStart);
            supports.setupTable(resetOnStart);
            artifacts.setupTable(resetOnStart);
            LOG.info("Done initializing DB");
        } catch (Exception e) {
            LOG.error("Failed while initializing DB", e);
            if (closeOnInitFail) {
                ctx.close();
                return;
            }
        }
        
        try {
            LOG.info("Initializing S3");
            
        } catch (Exception e) {
            LOG.error("Failed while demoing S3", e);
            if (closeOnInitFail) {
                ctx.close();
                return;
            }
        }
        
        try {
            addTestData();
        } catch (SQLException e) {
            LOG.error("Failed while adding test data", e);
            if (closeOnInitFail) {
                ctx.close();
            }
        }
    }
}
