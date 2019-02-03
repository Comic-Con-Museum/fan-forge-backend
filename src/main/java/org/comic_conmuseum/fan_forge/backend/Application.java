package org.comic_conmuseum.fan_forge.backend;

import org.comic_conmuseum.fan_forge.backend.models.*;
import org.comic_conmuseum.fan_forge.backend.persistence.*;
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
import java.util.*;
import java.util.stream.IntStream;

@SpringBootApplication(exclude = {ErrorMvcAutoConfiguration.class})
public class Application implements CommandLineRunner {
    private static final Logger LOG = LoggerFactory.getLogger("application");

    @Value("${s3.access-key}")
    private String accessKey;

    @Value("${s3.secret-key}")
    private String secretKey;

    @Value("${s3.url}")
    private String url;

    @Value("${s3.region}")
    private String region;
    
    @Value("${ff.reset-on-start}")
    private boolean resetOnStart;
    
    @Value("${ff.add-test-data}")
    private boolean addTestData;
    
    @Value("${ff.close-on-init-fail}")
    private boolean closeOnInitFail;
    
    private final ExhibitQueryBean exhibits;
    private final SupportQueryBean supports;
    private final ArtifactQueryBean artifacts;
    private final CommentQueryBean comments;
    private final S3Bean s3;
    private final ConfigurableApplicationContext ctx;
    
    @Autowired
    public Application(ExhibitQueryBean exhibits, SupportQueryBean supports, ArtifactQueryBean artifacts,
                       CommentQueryBean comments, S3Bean s3, ConfigurableApplicationContext ctx) {
        this.exhibits = exhibits;
        this.supports = supports;
        this.artifacts = artifacts;
        this.comments = comments;
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
        for (int eIdx = 0; eIdx < exhibitTitles.size(); ++eIdx) {
            String title = exhibitTitles.get(eIdx);
            Instant exhibitMade = Instant.now().minus(eIdx + 5, ChronoUnit.DAYS);
            // tags are based on even/odd indexes
            long exhibitId = exhibits.create(new Exhibit(
                    0, title, "Description for " + title, original.getId(),
                    exhibitMade, new String[] { "post", "exhibit", eIdx % 2 == 0 ? "even" : "odd", "index:" + eIdx },
                    null, false
            ), original);

            // exhibit 1 and 11 are featured
            if (exhibitId % 10 == 1) {
                exhibits.markFeatured(exhibitId);
            }

            for (int sIdx = 0; sIdx < supporters.length; ++sIdx) {
                if ((eIdx & sIdx) == sIdx) {
                    Map<String, Boolean> predictions = new HashMap<>();
                    predictions.put("male", (sIdx + eIdx) % 2 == 0);
                    predictions.put("female", (sIdx + eIdx) % 3 == 0);
                    predictions.put("kids", (sIdx + eIdx) % 4 == 0);
                    predictions.put("teenagers", (sIdx + eIdx) % 5 == 0);
                    predictions.put("adults", (sIdx + eIdx) % 6 == 0);
                    supports.createSupport(
                            exhibitId, new Survey(
                                    ((sIdx + eIdx) % 9) + 1, predictions,
                                    (sIdx + eIdx + 3) % 10,
                                    supporters[sIdx].getId()
                            )
                    );
                }
            }
            for (int aIdx = 0; aIdx < eIdx % 4; ++aIdx) {
                artifacts.create(new Artifact(
                        0, "artifact " + aIdx + " of " + eIdx,
                        "description of artifact",
                        aIdx == 0,
                        null, exhibitId,
                        exhibitMade.plus(aIdx, ChronoUnit.DAYS)
                ), supporters[aIdx]);
            }
            Long lastComment = null;
            for (int cIdx = 0; cIdx < eIdx % 8; ++cIdx) {
                long inserted = comments.create(new Comment(
                        0, "This is a test comment; idx " + eIdx + "." + cIdx,
                        "shouldn't be shown", exhibitId,
                        cIdx % 2 == 0 ? null : lastComment,
                        exhibitMade.plus(cIdx, ChronoUnit.HOURS)
                ), supporters[cIdx % supporters.length]);
                if (cIdx % 2 != 0) {
                    lastComment = inserted;
                }
            }
        }
        LOG.info("Done adding test data");
    }

    @Override
    public void run(String... args) {
        // TODO Should init stuff be moved into the beans' ctors/@PostConstruct?
        try {
            LOG.info("Initializing DB");
            // Order is important! Some tables depend on others.
            exhibits.setupTable(resetOnStart);
            supports.setupTable(resetOnStart);
            artifacts.setupTable(resetOnStart);
            comments.setupTable(resetOnStart);
            LOG.info("Done initializing DB");
        } catch (Exception e) {
            LOG.error("Failed while initializing DB", e);
            if (closeOnInitFail) {
                ctx.close();
                return;
            }
        }
        
//        try {
//            LOG.info("Initializing S3");
//            s3.setupBucket(resetOnStart);
//            LOG.info("Done initializing S3");
//        } catch (Exception e) {
//            LOG.error("Failed while initializing S3", e);
//            if (closeOnInitFail) {
//                ctx.close();
//                return;
//            }
//        }
        
        try {
            if (addTestData) {
                LOG.info("Adding test data");
                addTestData();
                LOG.info("Done adding test data");
            } else {
                LOG.info("Not adding test data");
            }
        } catch (SQLException e) {
            LOG.error("Failed while adding test data", e);
            if (closeOnInitFail) {
                ctx.close();
                return;
            }
        }
        
        LOG.info("Startup completed. Server is ready to take requests.");
    }
}
