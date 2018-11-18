package org.comic_conmuseum.fan_forge.backend.endpoints;

import com.amazonaws.services.s3.model.S3Object;
import org.comic_conmuseum.fan_forge.backend.persistence.S3Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ImageServingEndpoints {
    private static final Logger LOG = LoggerFactory.getLogger("endpoints.images");
    
    private final S3Bean s3;
    
    @Autowired
    public ImageServingEndpoints(S3Bean s3) {
        this.s3 = s3;
    }
    
    @RequestMapping(value = "/image/{id}", method = RequestMethod.GET)
    public ResponseEntity<Resource> getImage(@PathVariable long id) {
        S3Object obj = s3.getImage(id);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", obj.getObjectMetadata().getContentType());
        InputStreamResource res = new InputStreamResource(obj.getObjectContent());
        return new ResponseEntity<>(res, headers, HttpStatus.OK);
    }
    
    // TODO Delete this test endpoint
    long nextId = 10000; // start, hopefully, after all the test data (there's a reason this is getting deleted)
    @RequestMapping(value = "/image", method = RequestMethod.POST, consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Boolean>> putImages(MultipartHttpServletRequest req) {
        Map<String, Boolean> successes = new HashMap<>();
        for (MultipartFile f : req.getFiles("img")) {
            LOG.info("Uploading file {}", f.getOriginalFilename());
            try {
                s3.putImage(++nextId, f);
                successes.put(f.getOriginalFilename(), true);
                LOG.info("Upload succeeded.");
            } catch (IOException e) {
                successes.put(f.getOriginalFilename(), false);
                LOG.error("Upload failed!", e);
            }
        }
        return ResponseEntity.ok(successes);
    }
}
