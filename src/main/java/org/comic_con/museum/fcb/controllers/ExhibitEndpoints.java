package org.comic_con.museum.fcb.controllers;

import org.comic_con.museum.fcb.controllers.inputs.ExhibitCreation;
import org.comic_con.museum.fcb.controllers.responses.ExhibitAbbreviated;
import org.comic_con.museum.fcb.controllers.responses.ExhibitFull;
import org.comic_con.museum.fcb.controllers.responses.Feed;
import org.comic_con.museum.fcb.models.Exhibit;
import org.comic_con.museum.fcb.models.User;
import org.comic_con.museum.fcb.models.dal.ExhibitDAL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class ExhibitEndpoints {
    private final Logger LOG = LoggerFactory.getLogger("endpoints.exhibit");

    {
        final List<String> titles = Arrays.asList(
                "Hello, World!",
                "smook",
                "Batman in the 1960s",
                "Jason Voorhees",
                "Yahtzee Croshaw",
                "A banana",
                "Why Sonic sucks",
                "Why Sonic rules",
                "Help, I've fallen and I can't get up!",
                "HI, BILLY MAYS HERE!",
                "Have you ever CCIDENTALLY HIT CAPSLOCK ISNTEAD OF a",
                "How post-2008 retro-terminal-colored ASCII art affected mid-2010s Batman linework",
                "~none of those are good exhibit titles, I'm sorry"
        );
        Collections.shuffle(titles);

        User original = new User("nic".hashCode(), "nic", null, false);
        for (String title : titles) {
            Integer id = createExhibit(new ExhibitCreation(title, "Description for " + title), original).getBody();
            if (null != id) {
                ExhibitDAL.getById(id).setCreated(Instant.now().minus(id, ChronoUnit.DAYS));
            }
        }
    }

    @RequestMapping(value = "/feed/{type}", method = RequestMethod.GET)
    public ResponseEntity<Feed> getFeed(@PathVariable("type") String feedName, @RequestParam int startIdx, @AuthenticationPrincipal User user) {
        ExhibitDAL.FeedType feedType;
        switch (feedName) {
            case "new":
                feedType = ExhibitDAL.FeedType.NEW;
                break;
            case "alphabetical":
                feedType = ExhibitDAL.FeedType.ALPHABETICAL;
                break;
            default:
                return ResponseEntity.notFound().build();
        }
        List<Exhibit> feed = ExhibitDAL.getFeed(startIdx, feedType);
        if (feed == null) {
            return ResponseEntity.notFound().build();
        }
        Feed respData = new Feed();
        respData.exhibits = feed.stream().map(e -> new ExhibitAbbreviated(e, user)).collect(Collectors.toList());
        respData.startIdx = startIdx;
        respData.count = ExhibitDAL.getTotalCount();
        respData.pageSize = ExhibitDAL.FEED_PAGE_SIZE;
        return ResponseEntity.ok(respData);
    }

    @RequestMapping(value = "/exhibit/{id}")
    public ResponseEntity<ExhibitFull> getExhibit(@PathVariable int id, @AuthenticationPrincipal User user) {
        Exhibit ex = ExhibitDAL.getById(id);
        if (ex == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ExhibitFull(ex, user));
    }

    @RequestMapping(value = "/exhibit", method = RequestMethod.POST)
    public ResponseEntity<Integer> createExhibit(@RequestBody ExhibitCreation data, @AuthenticationPrincipal User user) {
        LOG.info("Creating exhibit from user {}", user);
        Exhibit built = data.build(user);
        if (built == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(ExhibitDAL.create(built));
    }

    @RequestMapping(value = "/exhibit/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deleteExhibit(@PathVariable int id, @AuthenticationPrincipal User user) {
        if (ExhibitDAL.delete(id, user)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
