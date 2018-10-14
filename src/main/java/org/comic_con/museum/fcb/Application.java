package org.comic_con.museum.fcb;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class Application implements CommandLineRunner {
    private final Logger LOG = LoggerFactory.getLogger("application");

    // TODO Replace JDBC usage with JPA
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Value("${s3.access-key}")
    String accessKey;

    @Value("${s3.secret-key}")
    String secretKey;

    @Value("${s3.url}")
    String url;

    @Value("${s3.region}")
    String region;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            LOG.info("Demoing DB connection:");
            jdbcTemplate.execute("DROP TABLE IF EXISTS demo");
            jdbcTemplate.execute("CREATE TABLE demo (id INTEGER PRIMARY KEY)");
            jdbcTemplate.execute("INSERT INTO demo VALUES (1), (2), (3)");
            jdbcTemplate.query(
                    "SELECT * FROM demo",
                    (rs, rowNum) -> String.valueOf(rs.getInt("id"))
            ).forEach(LOG::info);
            LOG.info("Done with DB stuff");
        } catch (Exception e) {
            LOG.error("Error occurred while doing database stuff", e);
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
