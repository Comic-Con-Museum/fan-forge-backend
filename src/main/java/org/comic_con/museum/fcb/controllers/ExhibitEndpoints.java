package org.comic_con.museum.fcb.controllers;

import org.comic_con.museum.fcb.controllers.inputs.ExhibitCreation;
import org.comic_con.museum.fcb.controllers.responses.ExhibitFull;
import org.comic_con.museum.fcb.controllers.responses.Feed;
import org.comic_con.museum.fcb.models.User;
import org.comic_con.museum.fcb.dal.ExhibitQueryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.Instant;

@RestController
public class ExhibitEndpoints {
    private final Logger LOG = LoggerFactory.getLogger("endpoints.exhibit");

    private final ExhibitQueryBean exhibits;
    
    public ExhibitEndpoints(ExhibitQueryBean exhibitQueryBean) {
        this.exhibits = exhibitQueryBean;
    }

    @RequestMapping(value = "/feed/{type}", method = RequestMethod.GET)
    public ResponseEntity<Feed> getFeed(@PathVariable("type") String feedName, @RequestParam int startIdx, @AuthenticationPrincipal User user) throws SQLException {
        ExhibitQueryBean.FeedType feed;
        switch (feedName) {
            case "new":
                LOG.info("NEW feed");
                feed = ExhibitQueryBean.FeedType.NEW;
                break;
            case "alphabetical":
                LOG.info("ALPHABETICAL feed");
                feed = ExhibitQueryBean.FeedType.ALPHABETICAL;
                break;
            default:
                LOG.info("Unknown feed: {}", feedName);
                return ResponseEntity.notFound().build();
        }
        Feed.Entry[] entries = exhibits.getFeedBy(feed, startIdx).stream()
                .map(e -> new Feed.Entry(e.getId(), e.getTitle(), e.getDescription(), 4))
                .toArray(Feed.Entry[]::new);
        Feed f = new Feed(
                startIdx,
                exhibits.getCount(),
                entries
        );
        return ResponseEntity.ok(f);
    }

    @RequestMapping(value = "/exhibit/{id}")
    public ResponseEntity<ExhibitFull> getExhibit(@PathVariable int id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new ExhibitFull(id, "item " + id, "asda", 3, 15, Instant.now()));
    }

    @RequestMapping(value = "/exhibit", method = RequestMethod.POST)
    public ResponseEntity<Integer> createExhibit(@RequestBody ExhibitCreation data, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(8);
    }

    @RequestMapping(value = "/exhibit/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deleteExhibit(@PathVariable int id, @AuthenticationPrincipal User user) {
        return ResponseEntity.noContent().build();
    }
}
