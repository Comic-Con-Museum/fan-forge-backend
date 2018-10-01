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
            Integer id = createExhibit(new Exhibit.Input(title, "Description for " + title)).getBody();
            if (null != id) exhibits.get(id).setCreated(Instant.now().minus(id, ChronoUnit.DAYS));
        }
    }

    // this should return a POJO, not a Map
    @RequestMapping(value = "/feed/{type}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getFeed(@PathVariable("type") String feedType, @RequestParam(defaultValue = "0") int startIdx) {
        String user = "nic";
        if (startIdx > exhibits.size()) return ResponseEntity.badRequest().build();
        List<Exhibit.Abbreviated> sorted;
        switch (feedType) {
            case "new":
                sorted = exhibits.values().stream()
                                .sorted(Comparator.comparing(Exhibit::getCreated))
                                .map(e -> e.getAbbreviated(user))
                                .collect(Collectors.toList());
                break;
            case "alphabetical":
                sorted = exhibits.values().stream()
                                .sorted(Comparator.comparing(Exhibit::getTitle))
                                .map(e -> e.getAbbreviated(user))
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
    public ResponseEntity<Exhibit.Full> getExhibit(@PathVariable int id) {
        String user = "nic";
        Exhibit ex = exhibits.get(id);
        if (ex == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ex.getFull(user));
    }

    @RequestMapping(value = "/exhibit", method = RequestMethod.POST)
    public ResponseEntity<Integer> createExhibit(@RequestBody Exhibit.Input data) {
        String user = "nic";
        String title = data.title;
        if (null == title) {
            return ResponseEntity.badRequest().build();
        }
        String description = data.description;
        if (null == description) {
            description = "This is a dummy exhibit with a dummy description.";
        }
        LocalDateTime created = LocalDateTime.now();
        ++lastInserted;
        Exhibit exhibit = new Exhibit(lastInserted, title, description, user);
        exhibits.put(lastInserted, exhibit);
        return ResponseEntity.ok(lastInserted);
    }

    @RequestMapping(value = "/exhibit/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deleteExhibit(@PathVariable int id) {
        exhibits.remove(id);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/support/exhibit/{id}", method = RequestMethod.POST)
    public ResponseEntity supportExhibit(@PathVariable int id) {
        String user = "nic";
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

    @RequestMapping(value = "/support/exhibit/{id}", method = RequestMethod.DELETE)
    public ResponseEntity upvoteExhibit(@PathVariable int id) {
        String user = "nic";
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

    @RequestMapping("/*")
    public ResponseEntity noSuchEndpoint(HttpServletRequest req) {
        System.out.println("Non-existent " + req.getMethod() + " to " + req.getRequestURI());
        return ResponseEntity.notFound().build();
    }
}
