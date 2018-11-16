package org.comic_con.museum.fcb.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.comic_con.museum.fcb.endpoints.inputs.ArtifactCreation;
import org.comic_con.museum.fcb.models.Artifact;
import org.comic_con.museum.fcb.models.User;
import org.comic_con.museum.fcb.persistence.ArtifactQueryBean;
import org.comic_con.museum.fcb.persistence.S3Bean;
import org.comic_con.museum.fcb.persistence.TransactionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

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
    public ResponseEntity<Artifact> getArtifact(@PathVariable long id) throws SQLException {
        Artifact val = artifacts.byId(id);
        return ResponseEntity.ok(val);
    }

    @RequestMapping(value = "/artifact", method = RequestMethod.POST)
    public ResponseEntity<Long> createArtifact(MultipartHttpServletRequest req, @AuthenticationPrincipal User user) {
        // TODO createArtifact (and also editArtifact and deleteArtifact)
        return ResponseEntity.ok(-1L);
    }
}
