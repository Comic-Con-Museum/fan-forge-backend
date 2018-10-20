package org.comic_con.museum.fcb.controllers;

import org.comic_con.museum.fcb.controllers.inputs.ExhibitCreation;
import org.comic_con.museum.fcb.controllers.responses.ExhibitFull;
import org.comic_con.museum.fcb.controllers.responses.Feed;
import org.comic_con.museum.fcb.models.Exhibit;
import org.comic_con.museum.fcb.models.User;
import org.comic_con.museum.fcb.dal.ExhibitQueryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.Instant;

@RestController
public class ExhibitEndpoints {
    private final Logger LOG = LoggerFactory.getLogger("endpoints.exhibit");

    private final ExhibitQueryBean exhibits;
    private final DataSourceTransactionManager transactionManager;
    
    public ExhibitEndpoints(ExhibitQueryBean exhibitQueryBean, DataSourceTransactionManager transactionManager) {
        this.exhibits = exhibitQueryBean;
        this.transactionManager = transactionManager;
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
    public ResponseEntity<ExhibitFull> getExhibit(@PathVariable long id, @AuthenticationPrincipal User user) {
        Exhibit ex = exhibits.getById(id);
        return ResponseEntity.ok(new ExhibitFull(
                ex.getId(), ex.getTitle(), ex.getDescription(), 4, ex.getAuthor(), ex.getCreated(), ex.getTags()
        ));
    }

    @RequestMapping(value = "/exhibit", method = RequestMethod.POST)
    public ResponseEntity<Long> createExhibit(@RequestBody ExhibitCreation data, @AuthenticationPrincipal User user) throws SQLException {
        long id = exhibits.create(new Exhibit(
                0, data.getTitle(), data.getDescription(), user.getId(), Instant.now(), data.getTags()
        ), user);
        return ResponseEntity.ok(id);
    }

    @RequestMapping(value = "/exhibit/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deleteExhibit(@PathVariable long id, @AuthenticationPrincipal User user) {
        exhibits.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
