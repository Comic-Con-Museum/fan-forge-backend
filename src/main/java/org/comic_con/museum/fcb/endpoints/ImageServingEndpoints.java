package org.comic_con.museum.fcb.endpoints;

import org.comic_con.museum.fcb.persistence.S3Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ImageServingEndpoints {
    private static final Logger LOG = LoggerFactory.getLogger("endpoints.images");
    
    private final S3Bean s3;
    
    @Autowired
    public ImageServingEndpoints(S3Bean s3) {
        this.s3 = s3;
    }
    
    // TODO GET /image/{id}
}
