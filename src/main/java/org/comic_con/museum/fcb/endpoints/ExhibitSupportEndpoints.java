package org.comic_con.museum.fcb.endpoints;

import org.comic_con.museum.fcb.persistence.SupportQueryBean;
import org.comic_con.museum.fcb.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@RestController
public class ExhibitSupportEndpoints {
    private static final Logger LOG = LoggerFactory.getLogger("endpoints.support.exhibit");
    
    private final SupportQueryBean supports;
    
    public ExhibitSupportEndpoints(SupportQueryBean supportQueryBean) {
        this.supports = supportQueryBean;
    }
    
    @RequestMapping(value = "/support/exhibit/{id}", method = RequestMethod.PUT)
    public ResponseEntity supportExhibit(@PathVariable int id, @RequestBody(required = false) String data,
                                         @AuthenticationPrincipal User user) {
        LOG.info("Supporting {} as {}", id, user);
        boolean newSupporter = supports.support(id, user, data);
        LOG.info("New supporter? {}", newSupporter);
        if (newSupporter) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/support/exhibit/{id}", method = RequestMethod.DELETE)
    public ResponseEntity upvoteExhibit(@PathVariable int id, @AuthenticationPrincipal User user) {
        LOG.info("Unsupporting {} as {}", id, user);
        boolean wasSupporter = supports.unsupport(id, user);
        LOG.info("Was a supporter? {}", wasSupporter);
        if (wasSupporter) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/admin/supports/{eid}", method = RequestMethod.GET)
    public ResponseEntity<String> getSurveys(@PathVariable long eid, @AuthenticationPrincipal User user) {
        if (!user.isAdmin()) {
            LOG.info("Non-admin user, {} tried to get surveys for an exhibit, {}", user, eid);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(APPLICATION_JSON)
                .body(supports.getSurveys(eid));
    }
}
