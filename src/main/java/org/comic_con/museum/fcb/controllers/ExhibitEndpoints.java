package org.comic_con.museum.fcb.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.comic_con.museum.fcb.controllers.inputs.ExhibitCreation;
import org.comic_con.museum.fcb.controllers.responses.ExhibitFull;
import org.comic_con.museum.fcb.controllers.responses.Feed;
import org.comic_con.museum.fcb.controllers.responses.Views;
import org.comic_con.museum.fcb.dal.TransactionWrapper;
import org.comic_con.museum.fcb.models.Exhibit;
import org.comic_con.museum.fcb.models.User;
import org.comic_con.museum.fcb.dal.ExhibitQueryBean;
import org.comic_con.museum.fcb.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ExhibitEndpoints {
    private final Logger LOG = LoggerFactory.getLogger("endpoints.exhibit");

    private final ExhibitQueryBean exhibits;
    private final TransactionWrapper transactions;
    
    @Autowired
    public ExhibitEndpoints(ExhibitQueryBean exhibitQueryBean, TransactionWrapper transactionWrapperBean) {
        this.exhibits = exhibitQueryBean;
        this.transactions = transactionWrapperBean;
    }

    @RequestMapping(value = "/feed/{type}", method = RequestMethod.GET)
    public ResponseEntity<String> getFeed(@PathVariable("type") String feedName, @RequestParam int startIdx,
                                     @AuthenticationPrincipal User user) throws JsonProcessingException {
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
        
        List<Exhibit> feedRaw;
        long count;
        try (TransactionWrapper.Transaction tr = transactions.start()) {
            feedRaw = exhibits.getFeedBy(feed, startIdx);
            count = exhibits.getCount();
            tr.commit();
        } // no catch because we're just closing the transaction, we want errors to fall through
        List<Feed.Entry> entries = feedRaw.stream()
                .map(e -> new Feed.Entry(e, 4, true))
                .collect(Collectors.toList());
        Feed f = new Feed(startIdx, count, entries);
        
        return ResponseEntity.ok(JSONUtil.toString(f, user));
    }

    @RequestMapping(value = "/exhibit/{id}")
    public ResponseEntity<String> getExhibit(@PathVariable long id, @AuthenticationPrincipal User user) throws JsonProcessingException {
        Exhibit e = exhibits.getById(id);
        ExhibitFull resp = new ExhibitFull(e, 8, false);
        return ResponseEntity.ok(new ObjectMapper().writerWithView(Views.byPrincipal(user)).writeValueAsString(resp));
    }

    @RequestMapping(value = "/exhibit", method = RequestMethod.POST)
    public ResponseEntity<Long> createExhibit(@RequestBody ExhibitCreation data, @AuthenticationPrincipal User user) throws SQLException {
        long id = exhibits.create(data.build(user), user);
        return ResponseEntity.ok(id);
    }
    
    @RequestMapping(value = "/exhibit/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<ExhibitFull> editExhibit(@PathVariable long id, @RequestBody ExhibitCreation data,
                                                   @AuthenticationPrincipal User user) {
        throw new UnsupportedOperationException(); // TODO PUT /exhibit/{id}, PATCH /exhibit/{id}
    }

    @RequestMapping(value = "/exhibit/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deleteExhibit(@PathVariable long id, @AuthenticationPrincipal User user) {
        exhibits.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
