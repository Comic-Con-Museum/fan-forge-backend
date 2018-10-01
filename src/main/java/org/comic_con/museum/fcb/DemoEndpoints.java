package org.comic_con.museum.fcb;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class DemoEndpoints {
    private static final Map<Integer, Exhibit> exhibits = new HashMap<>();
    private static int lastInserted = 0;

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
            Integer id = createExhibit(new ExhibitInput(title, "Description for " + title)).getBody();
            if (null != id) exhibits.get(id).setCreated(Instant.now().minus(id, ChronoUnit.DAYS));
        }
    }

    @RequestMapping(value = "/feed/{type}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getFeed(@PathVariable("type") String feedType, @RequestParam(value = "start", defaultValue = "0") int startIdx) {
        List<Exhibit.Abbreviated> sorted;
        switch (feedType) {
            case "new":
                sorted = exhibits.values().stream()
                                .sorted(Comparator.comparing(Exhibit::getCreated))
                                .map(e -> e.new Abbreviated())
                                .collect(Collectors.toList());
                break;
            case "alphabetical":
                sorted = exhibits.values().stream()
                                .sorted(Comparator.comparing(Exhibit::getTitle))
                                .map(e -> e.new Abbreviated())
                                .collect(Collectors.toList());
                break;
            default:
                return ResponseEntity.notFound().build();
        }
        final int pageSize = 10;
        int lastIdx = sorted.size() - 1;
        sorted = sorted.subList(startIdx, Math.min(lastIdx, startIdx + pageSize));
        Map<String, Object> respData = new HashMap<>();
        respData.put("exhibits", sorted);
        respData.put("startIdx", startIdx);
        respData.put("count", lastIdx + 1);
        respData.put("pageSize", pageSize);
        return ResponseEntity.ok(respData);
    }

    @RequestMapping(value = "/exhibit/{id}")
    public ResponseEntity<Exhibit> getExhibit(@PathVariable int id) {
        Exhibit ex = exhibits.get(id);
        if (ex == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ex);
    }

    static class ExhibitInput {
        public void setTitle(String title) { this.title = title; }
        public void setDescription(String description) { this.description = description; }

        String title;
        String description;
        ExhibitInput() {}
        ExhibitInput(String title, String description) {
            this.title = title;
            this.description = description;
        }
    }
    @RequestMapping(value = "/exhibit", method = RequestMethod.POST)
    public ResponseEntity<Integer> createExhibit(@RequestBody ExhibitInput data) {
        String title = data.title;
        if (null == title) {
            return ResponseEntity.badRequest().build();
        }
        String description = data.description;
        if (null == description) {
            description = "This is a dummy exhibit with a dummy description.";
        }
        LocalDateTime created = LocalDateTime.now();
        Exhibit exhibit = new Exhibit(title, description);
        exhibits.put(lastInserted, exhibit);
        return ResponseEntity.ok(lastInserted++);
    }

    @RequestMapping(value = "/exhibit/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deleteExhibit(@PathVariable int id) {
        exhibits.remove(id);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/upvote/exhibit/{id}", method = RequestMethod.POST)
    public ResponseEntity supportExhibit(@PathVariable int id, @RequestParam String user) {
        Exhibit ex = exhibits.get(id);
        if (ex == null) {
            return ResponseEntity.notFound().build();
        }
        boolean newSupporter = ex.addSupporter(user);
        if (newSupporter) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/upvote/exhibit/{id}", method = RequestMethod.DELETE)
    public ResponseEntity upvoteExhibit(@PathVariable int id, @RequestParam String user) {
        Exhibit ex = exhibits.get(id);
        if (ex == null) {
            return ResponseEntity.notFound().build();
        }
        boolean wasSupporter = ex.removeSupporter(user);
        if (wasSupporter) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
