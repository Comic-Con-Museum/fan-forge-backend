package org.comic_con.museum.fcb.controllers;

import org.comic_con.museum.fcb.controllers.inputs.ExhibitCreation;
import org.comic_con.museum.fcb.controllers.responses.ExhibitAbbreviated;
import org.comic_con.museum.fcb.controllers.responses.ExhibitFull;
import org.comic_con.museum.fcb.models.Exhibit;
import org.comic_con.museum.fcb.models.dal.ExhibitDAL;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
public class ExhibitEndpoints {
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

        for (String title : titles) {
            Integer id = createExhibit(new ExhibitCreation(title, "Description for " + title)).getBody();
            if (null != id) ExhibitDAL.getById(id).setCreated(Instant.now().minus(id, ChronoUnit.DAYS));
        }
    }

    // this should return a POJO, not a Map

    class FeedResponseData {
        List<ExhibitAbbreviated> exhibits;
        int startIdx;
        int count;
        int pageSize;

        public List<ExhibitAbbreviated> getExhibits() { return exhibits; }
        public int getStartIdx() { return startIdx; }
        public int getCount() { return count; }
        public int getPageSize() { return pageSize; }
    }
    @RequestMapping(value = "/feed/{type}", method = RequestMethod.GET)
    public ResponseEntity<FeedResponseData> getFeed(@PathVariable("type") String feedType, @RequestParam int startIdx) {
        String user = "nic";
        List<Exhibit> feed;
        switch (feedType) {
            case "new":
                feed = ExhibitDAL.getFeed(startIdx, Comparator.comparing(Exhibit::getCreated));
                break;
            case "alphabetical":
                feed = ExhibitDAL.getFeed(startIdx, Comparator.comparing(Exhibit::getTitle));
                break;
            default:
                return ResponseEntity.notFound().build();
        }
        if (feed == null) {
            return ResponseEntity.badRequest().build();
        }
        FeedResponseData respData = new FeedResponseData();
        respData.exhibits = feed.stream().map(e -> new ExhibitAbbreviated(e, user)).collect(Collectors.toList());
        respData.startIdx = startIdx;
        respData.count = ExhibitDAL.getTotalCount();
        respData.pageSize = ExhibitDAL.FEED_PAGE_SIZE;
        return ResponseEntity.ok(respData);
    }

    @RequestMapping(value = "/exhibit/{id}")
    public ResponseEntity<ExhibitFull> getExhibit(@PathVariable int id) {
        String user = "nic";
        Exhibit ex = ExhibitDAL.getById(id);
        if (ex == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ExhibitFull(ex, user));
    }

    @RequestMapping(value = "/exhibit", method = RequestMethod.POST)
    public ResponseEntity<Integer> createExhibit(@RequestBody ExhibitCreation data) {
        String user = "nic";
        Exhibit built = data.build(user);
        if (built == null) {
            return ResponseEntity.badRequest().build();
        }
        ExhibitDAL.create(built);
        return ResponseEntity.ok(built.getId());
    }

    @RequestMapping(value = "/exhibit/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deleteExhibit(@PathVariable int id) {
        String user = "nic";
        ExhibitDAL.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
