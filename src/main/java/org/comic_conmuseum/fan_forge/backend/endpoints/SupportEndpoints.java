package org.comic_conmuseum.fan_forge.backend.endpoints;

import org.comic_conmuseum.fan_forge.backend.endpoints.inputs.SurveyCreation;
import org.comic_conmuseum.fan_forge.backend.endpoints.responses.ErrorResponse;
import org.comic_conmuseum.fan_forge.backend.endpoints.responses.SurveyView;
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
public class SupportEndpoints {
    private static final Logger LOG = LoggerFactory.getLogger("endpoints.support.exhibit");
    
    private final SupportQueryBean supports;
    
    public SupportEndpoints(SupportQueryBean supportQueryBean) {
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
        for (Survey.Population pop : Survey.Population.values()) {
            if (!data.getPopulations().containsKey(pop.display())) {
                return ResponseEntity.badRequest().body(new ErrorResponse(
                        "Must provide all populations -- see documentation",
                        "Provide all of the populations"
                ));
            }
        }
        if (data.getRating() < 0 || 10 < data.getRating()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    "Invalid rating -- must be in [0,10]",
                    "Pass a rating in the valid range"
            ));
        }
        if (data.getVisits() < 1 || 10 < data.getVisits()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    "Invalid visits -- must be in [1,10]",
                    "Pass a visits in the valid range"
            ));
        }
        boolean newSupporter = supports.createSupport(id, data.build(user));
        LOG.info("New supporter? {}", newSupporter);
        if (newSupporter) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @RequestMapping(value = "/support/exhibit/{id}", method = RequestMethod.GET)
    public ResponseEntity<SurveyView> getSurvey(@PathVariable long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new SurveyView(supports.getSupportSurvey(id, user)));
    }

    @RequestMapping(value = "/support/exhibit/{id}", method = RequestMethod.DELETE)
    public ResponseEntity unsupportExhibit(@PathVariable int id, @AuthenticationPrincipal User user) {
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
    public ResponseEntity<SurveyAggregate> getSurveyAggregate(@PathVariable long eid) {
        return ResponseEntity.ok(supports.getAggregateData(eid));
    }
}
