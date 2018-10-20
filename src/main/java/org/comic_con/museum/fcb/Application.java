package org.comic_con.museum.fcb;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.comic_con.museum.fcb.models.Exhibit;
import org.comic_con.museum.fcb.models.User;
import org.comic_con.museum.fcb.dal.ExhibitQueryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SpringBootApplication
public class Application implements CommandLineRunner {
    private final Logger LOG = LoggerFactory.getLogger("application");

    // TODO Replace JDBC usage with JPA
    @Autowired
    JdbcTemplate jdbcTemplate;
    
    @Autowired
    ExhibitQueryBean exhibits;

    @Value("${s3.access-key}")
    String accessKey;

    @Value("${s3.secret-key}")
    String secretKey;

    @Value("${s3.url}")
    String url;

    @Value("${s3.region}")
    String region;
    
    @Value("${fcb.reset-on-start}")
    boolean resetOnStart;
    
    @Value("${fcb.add-test-data}")
    boolean addTestData;

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
        for (int i = 0; i < exhibitTitles.size(); ++i) {
            String title = exhibitTitles.get(i);
            exhibits.create(new Exhibit(
                    0, title, "Description for " + title, 0,
                    Instant.now().minus(i, ChronoUnit.DAYS),
                    new String[] { "post", "exhibit", "index:" + i }
            ), original);
        }
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            LOG.info("Initializing DB");
            exhibits.setupExhibitTable(resetOnStart);
            addTestData();
        
            LOG.info("Testing DB connection");
            User author = new User(3, "me", new byte[0], false);
            long id = exhibits.create(new Exhibit(
                    0, "title example", "description example", 7, Instant.now().minus(1, ChronoUnit.DAYS),
                    new String[] { "tag1", "tag2" }
            ), author);
            LOG.info("Created exhibit {}", id);
            Exhibit queried = exhibits.getById(id);
            LOG.info("Exhibit has ID {}, author {}", queried.getId(), queried.getAuthor());
            queried.setDescription("Updated description for " + id);
            exhibits.update(queried, author);
            exhibits.delete(id, author);
            
            LOG.info("Testing feed:");
            List<Exhibit> feed = exhibits.getFeedBy(ExhibitQueryBean.FeedType.NEW, 2);
            for (Exhibit e : feed) {
                LOG.info("#{}: {}", e.getId(), e.getDescription());
            }
        
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
            LOG.error("Error occurred while doing S3 stuff", e);
        }
    }
}
