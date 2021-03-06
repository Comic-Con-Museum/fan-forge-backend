package org.comic_conmuseum.fan_forge.backend.persistence;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;

@Repository
public class S3Bean {
    private static final Logger LOG = LoggerFactory.getLogger("persist.s3");
    
    @Value("${s3.bucket}") private String bucketName;
    
    private final AmazonS3 client;
    // TODO Probably also connect to DB to make sure we don't duplicate IDs?
    // TODO Figure out how to make transactional (maybe request-scoped transaction manager?)
    
    public S3Bean(
            @Value("${s3.access-key}") String accessKey,
            @Value("${s3.secret-key}") String secretKey,
            @Value("${s3.url}") String url,
            @Value("${s3.region}") String region,
            @Value("${s3.bucket}") String bucketName
    ) {
        this.bucketName = bucketName;
    
        this.client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(accessKey, secretKey)
                )).withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                        url, region
                )).build();
    }

    public void setupBucket(boolean resetOnStart) {
        if (resetOnStart) {
            if (client.doesBucketExist(bucketName)) {
                LOG.info("Clearing {}", bucketName);
                ObjectListing objectListing = client.listObjects(bucketName);
                while (true) {
                    for (S3ObjectSummary s3ObjectSummary : objectListing.getObjectSummaries()) {
                        client.deleteObject(bucketName, s3ObjectSummary.getKey());
                    }
                    
                    // If the bucket contains many objects, the listObjects() call
                    // might not return all of the objects in the first listing. Check to
                    // see whether the listing was truncated. If so, retrieve the next page of objects 
                    // and delete them.
                    if (objectListing.isTruncated()) {
                        objectListing = client.listNextBatchOfObjects(objectListing);
                    } else {
                        break;
                    }
                }
            } else {
                LOG.info("Creating {}", bucketName);
                this.client.createBucket(bucketName);
            }
        } else {
            if (!client.doesBucketExist(bucketName)) {
                LOG.info("Creating {}", bucketName);
                this.client.createBucket(bucketName);
            } else {
                LOG.info("{} already existed", bucketName);
            }
        }
    }

    public S3Object getImage(long id) {
        LOG.info("Getting image of ID {}", id);
        return client.getObject(bucketName, String.valueOf(id));
    }
    
    public void putImage(long id, MultipartFile image) throws IOException {
        LOG.info("Storing image to ID {}", id);
        validateImage(image);
        client.putObject(
                bucketName,
                String.valueOf(id),
                image.getInputStream(),
                getMetadata(image));
    }
    
    private static void validateImage(MultipartFile image) throws IOException {
        if (image.getContentType() == null) {
            throw new IllegalArgumentException("Content-Type part header must be specified");
        }
        
        // TODO Check for whitelisted Content-Type
        
        ImageInputStream iis = ImageIO.createImageInputStream(image.getInputStream());
        Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
        
        if (!readers.hasNext()) {
            LOG.info("Image didn't match any formats");
            throw new IllegalArgumentException("Invalid image uploaded");
        }
        
        String expectedType = image.getContentType().split("/")[1];
        ImageReader matchingReader = null;
        while (readers.hasNext()) {
            ImageReader reader = readers.next();
            LOG.info("Possible match: {}", reader.getFormatName());
            if (reader.getFormatName().equalsIgnoreCase(expectedType)) {
                // TODO possible jpg/jpeg conflicts?
                matchingReader = reader;
                break;
            }
        }
        if (matchingReader == null) {
            LOG.info("Rejected upload; expected {} but didn't get it", expectedType);
            throw new IllegalArgumentException("Content-Type didn't match image content");
        }
        
        matchingReader.setInput(iis);
        BufferedImage fullyRead = matchingReader.read(0);
        if (fullyRead == null) {
            throw new IllegalArgumentException("Invalid image uploaded");
        }
    }
    
    private static ObjectMetadata getMetadata(MultipartFile image) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(image.getContentType());
        metadata.setContentLength(image.getSize());
        return metadata;
    }
}
