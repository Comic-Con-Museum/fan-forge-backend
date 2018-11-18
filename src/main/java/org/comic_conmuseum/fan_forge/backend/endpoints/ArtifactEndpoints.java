package org.comic_conmuseum.fan_forge.backend.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.comic_conmuseum.fan_forge.backend.endpoints.inputs.ArtifactCreation;
import org.comic_conmuseum.fan_forge.backend.models.Artifact;
import org.comic_conmuseum.fan_forge.backend.models.User;
import org.comic_conmuseum.fan_forge.backend.persistence.ArtifactQueryBean;
import org.comic_conmuseum.fan_forge.backend.persistence.S3Bean;
import org.comic_conmuseum.fan_forge.backend.persistence.TransactionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.sql.SQLException;

@RestController
public class ArtifactEndpoints {
    private static final Logger LOG = LoggerFactory.getLogger("endpoints.artifact");
    private static final ObjectReader CREATE_PARAMS_READER = new ObjectMapper().readerFor(ArtifactCreation.class);

    private final ArtifactQueryBean artifacts;
    private final S3Bean s3;
    private final TransactionWrapper transactions;

    @Autowired
    public ArtifactEndpoints(ArtifactQueryBean artifacts, S3Bean s3, TransactionWrapper transactions) {
        this.artifacts = artifacts;
        this.s3 = s3;
        this.transactions = transactions;
    }

    @RequestMapping(value = "/artifact/{id}", method = RequestMethod.GET)
    public ResponseEntity<Artifact> getArtifact(@PathVariable long id) {
        return ResponseEntity.ok(artifacts.byId(id));
    }

    @RequestMapping(value = "/artifact", method = RequestMethod.POST)
    public ResponseEntity<Long> createArtifact(MultipartHttpServletRequest req,
                                               @RequestParam("data") String dataString,
                                               @AuthenticationPrincipal User user) throws IOException, SQLException {
        ArtifactCreation data = CREATE_PARAMS_READER.readValue(dataString);
        if (null == data.getTitle() || null == data.getDescription() || null == data.getParent()) {
            LOG.info("Required field not provided");
            return ResponseEntity.badRequest().build();
        }
        Artifact full = data.build(user);
        full.setCover(false);
        
        long id;
        try (TransactionWrapper.Transaction t = transactions.start()) {
            id = artifacts.create(full, data.getParent(), user);
            MultipartFile file = req.getFile("image");
            if (file == null) {
                return ResponseEntity.badRequest().build();
            }
            s3.putImage(id, file);
        
            t.commit();
        }
        return ResponseEntity.ok(id);
    }
    
    @RequestMapping(value = "/artifact/{id}", method = RequestMethod.POST)
    public ResponseEntity<Artifact> editArtifact(@PathVariable long id, MultipartHttpServletRequest req,
                                                 @RequestParam("data") String dataString,
                                                 @AuthenticationPrincipal User user) throws IOException {
        ArtifactCreation data = CREATE_PARAMS_READER.readValue(dataString);
        Artifact full = data.build(user);
        full.setCover(false);
        full.setId(id);
        
        try (TransactionWrapper.Transaction t = transactions.start()) {
            artifacts.update(full, user);
            MultipartFile file = req.getFile("image");
            if (file != null) {
                s3.putImage(id, file);
            }
            t.commit();
        }
        return getArtifact(id);
    }
    
    @RequestMapping(value = "/artifact/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deleteArtifact(@PathVariable long id, @AuthenticationPrincipal User user) {
        artifacts.delete(id, user);
        return ResponseEntity.ok().build();
    }
}
