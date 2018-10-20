package org.comic_con.museum.fcb.controllers;

import org.comic_con.museum.fcb.controllers.inputs.ExhibitCreation;
import org.comic_con.museum.fcb.controllers.responses.ExhibitFull;
import org.comic_con.museum.fcb.controllers.responses.Feed;
import org.comic_con.museum.fcb.models.Exhibit;
import org.comic_con.museum.fcb.models.User;
import org.comic_con.museum.fcb.models.dal.ExhibitQueryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class ExhibitEndpoints {
    private final Logger LOG = LoggerFactory.getLogger("endpoints.exhibit");

    private ExhibitQueryBean exhibits;
    
    public ExhibitEndpoints(ExhibitQueryBean exhibitQueryBean) {
        this.exhibits = exhibitQueryBean;
        
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
            Integer id = createExhibit(new ExhibitCreation(
                    title,
                    "Description for " + title,
                    new String[] { "tag1", "tag2" }
            ), original).getBody();
        }
    }

    @RequestMapping(value = "/feed/{type}", method = RequestMethod.GET)
    public ResponseEntity<Feed> getFeed(@PathVariable("type") String feedName, @RequestParam int startIdx, @AuthenticationPrincipal User user) {
        switch (feedName) {
            case "new":
                LOG.info("NEW feed");
                break;
            case "alphabetical":
                LOG.info("ALPHABETICAL feed");
                break;
            default:
                return ResponseEntity.notFound().build();
        }
        Feed respData = new Feed(
                startIdx,
                7,
                new Feed.Entry(0, "item 0", "desc", 4),
                new Feed.Entry(1, "item 1", "desc", 4),
                new Feed.Entry(2, "item 2", "desc", 4),
                new Feed.Entry(3, "item 3", "desc", 4),
                new Feed.Entry(4, "item 4", "desc", 4),
                new Feed.Entry(5, "item 5", "desc", 4),
                new Feed.Entry(6, "item 6", "desc", 4)
        );
        return ResponseEntity.ok(respData);
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
