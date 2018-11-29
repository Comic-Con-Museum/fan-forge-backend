package org.comic_conmuseum.fan_forge.backend.endpoints;

import org.comic_conmuseum.fan_forge.backend.endpoints.inputs.SurveyCreation;
import org.comic_conmuseum.fan_forge.backend.endpoints.responses.ErrorResponse;
import org.comic_conmuseum.fan_forge.backend.models.Survey;
import org.comic_conmuseum.fan_forge.backend.endpoints.responses.SurveyAggregate;
import org.comic_conmuseum.fan_forge.backend.persistence.SupportQueryBean;
import org.comic_conmuseum.fan_forge.backend.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ExhibitSupportEndpoints {
    private static final Logger LOG = LoggerFactory.getLogger("endpoints.support.exhibit");
    
    private final SupportQueryBean supports;
    
    public ExhibitSupportEndpoints(SupportQueryBean supportQueryBean) {
        this.supports = supportQueryBean;
    }
    
    @RequestMapping(value = "/support/exhibit/{id}", method = RequestMethod.PUT)
    public ResponseEntity supportExhibit(@PathVariable int id, @RequestBody SurveyCreation data,
                                         @AuthenticationPrincipal User user) {
        LOG.info("Supporting {} as {}", id, user);
        if (data.getVisits() == null || data.getPopulations() == null || data.getRating() == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    "Must provide all fields -- visit, populations, and rating",
                    "Provide all the fields"
            ));
        }
        for (String pop : Survey.POPULATIONS) {
            if (!data.getPopulations().containsKey(pop)) {
                return ResponseEntity.badRequest().body(new ErrorResponse(
                        "Must provide all populations -- see documentation",
                        "Provide all of the populations"
                ));
            }
        }
        boolean newSupporter = supports.createSupport(id, data.build(user));
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
        boolean wasSupporter = supports.deleteSupport(id, user);
        LOG.info("Was a supporter? {}", wasSupporter);
        if (wasSupporter) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/admin/supports/{eid}", method = RequestMethod.GET)
    public ResponseEntity<List<Survey>> getSurveys(@PathVariable long eid) {
        return ResponseEntity.ok(supports.getSurveys(eid));
    }
    
    @RequestMapping(value = "/admin/survey-data/{eid}", method = RequestMethod.GET)
    public ResponseEntity<SurveyAggregate> getAggregate(@PathVariable long eid) {
        return ResponseEntity.ok(supports.getAggregateData(eid));
    }
}
