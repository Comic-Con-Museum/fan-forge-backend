package org.comic_con.museum.fcb.controllers;

import org.comic_con.museum.fcb.controllers.inputs.ExhibitCreation;
import org.comic_con.museum.fcb.controllers.responses.ExhibitFull;
import org.comic_con.museum.fcb.controllers.responses.Feed;
import org.comic_con.museum.fcb.dal.SupportQueryBean;
import org.comic_con.museum.fcb.dal.TransactionWrapper;
import org.comic_con.museum.fcb.models.Exhibit;
import org.comic_con.museum.fcb.models.User;
import org.comic_con.museum.fcb.dal.ExhibitQueryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class ExhibitEndpoints {
    private final Logger LOG = LoggerFactory.getLogger("endpoints.exhibit");

    private final ExhibitQueryBean exhibits;
    private final SupportQueryBean supports;
    private final TransactionWrapper transactions;
    
    @Autowired
    public ExhibitEndpoints(ExhibitQueryBean exhibitQueryBean, SupportQueryBean supportQueryBean,
                            TransactionWrapper transactionWrapperBean) {
        this.exhibits = exhibitQueryBean;
        this.supports = supportQueryBean;
        this.transactions = transactionWrapperBean;
    }

    @RequestMapping(value = "/feed/{type}", method = RequestMethod.GET)
    public ResponseEntity<Feed> getFeed(@PathVariable("type") String feedName, @RequestParam int startIdx,
                                        @AuthenticationPrincipal User user) {
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
                // 404 instead of 400 because they're trying to hit a nonexistent endpoint (/feed/whatever), not passing
                // bad data to a real endpoint
                return ResponseEntity.notFound().build();
        }
        
        long count;
        List<Feed.Entry> entries;
        try (TransactionWrapper.Transaction tr = transactions.start()) {
            count = exhibits.getCount();
            List<Exhibit> feedRaw = exhibits.getFeedBy(feed, startIdx);
            entries = new ArrayList<>(feedRaw.size());
            for (Exhibit exhibit : feedRaw) {
                entries.add(new Feed.Entry(
                        exhibit, supports.supporterCount(exhibit),
                        supports.isSupporting(user, exhibit)
                ));
            }
            tr.commit();
        } // no catch because we're just closing the transaction, we want errors to fall through
        return ResponseEntity.ok(new Feed(startIdx, count, entries));
    }

    @RequestMapping(value = "/exhibit/{id}")
    public ResponseEntity<ExhibitFull> getExhibit(@PathVariable long id, @AuthenticationPrincipal User user) {
        Exhibit e = exhibits.getById(id);
        return ResponseEntity.ok(new ExhibitFull(e, supports.supporterCount(e), supports.isSupporting(user, e)));
    }

    @RequestMapping(value = "/exhibit", method = RequestMethod.POST)
    public ResponseEntity<Long> createExhibit(@RequestBody ExhibitCreation data, @AuthenticationPrincipal User user) throws SQLException {
        long id = exhibits.create(data.build(user), user);
        return ResponseEntity.ok(id);
    }
    
    @RequestMapping(value = "/exhibit/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<ExhibitFull> editExhibit(@PathVariable long id, @RequestBody ExhibitCreation data,
                                                   @AuthenticationPrincipal User user) {
        Exhibit ex = data.build(user);
        ex.setId(id);
        ExhibitFull resp;
        try (TransactionWrapper.Transaction t = transactions.start()) {
            exhibits.update(ex, user);
            resp = new ExhibitFull(
                    exhibits.getById(ex.getId()),
                    supports.supporterCount(ex),
                    supports.isSupporting(user, ex)
            );
            t.commit();
        }
        return ResponseEntity.ok(resp);
    }

    @RequestMapping(value = "/exhibit/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deleteExhibit(@PathVariable long id, @AuthenticationPrincipal User user) {
        exhibits.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
