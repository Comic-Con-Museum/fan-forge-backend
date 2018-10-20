package org.comic_con.museum.fcb.controllers;

import org.comic_con.museum.fcb.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class ExhibitSupportEndpoints {
    private static final Logger LOG = LoggerFactory.getLogger("endpoints.support.exhibit");
    
    @RequestMapping(value = "/support/exhibit/{id}", method = RequestMethod.POST)
    public ResponseEntity supportExhibit(@PathVariable int id, @RequestBody String data, @AuthenticationPrincipal User user) {
        LOG.info("Supporting %d as %s", id, user);
        boolean newSupporter = true;
        if (newSupporter) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/support/exhibit/{id}", method = RequestMethod.DELETE)
    public ResponseEntity upvoteExhibit(@PathVariable int id, @AuthenticationPrincipal User user) {
        LOG.info("Unsupporting %d as %s", id, user);
        boolean wasSupporter = true;
        if (wasSupporter) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
