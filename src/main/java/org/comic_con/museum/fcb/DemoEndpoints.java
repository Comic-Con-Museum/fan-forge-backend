package org.comic_con.museum.fcb;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class DemoEndpoints {
    private static final Map<Integer, Exhibit> exhibits = new HashMap<>();
    private static int lastInserted = 0;

    {
        Integer id;
        id = createExhibit(new ExhibitInput("Hello, World!", "demo desc 1")).getBody();
        if (null != id) exhibits.get(id).setCreated(Instant.now().minus(id, ChronoUnit.DAYS));
        id = createExhibit(new ExhibitInput("smook", "demo desc 2")).getBody();
        if (null != id) exhibits.get(id).setCreated(Instant.now().minus(id, ChronoUnit.DAYS));
        id = createExhibit(new ExhibitInput("Batman in the 1960s", "demo desc 3")).getBody();
        if (null != id) exhibits.get(id).setCreated(Instant.now().minus(id, ChronoUnit.DAYS));
        id = createExhibit(new ExhibitInput("Jason Voorhees", "demo desc 4")).getBody();
        if (null != id) exhibits.get(id).setCreated(Instant.now().minus(id, ChronoUnit.DAYS));
        id = createExhibit(new ExhibitInput("Yahtzee Croshaw", "demo desc 5")).getBody();
        if (null != id) exhibits.get(id).setCreated(Instant.now().minus(id, ChronoUnit.DAYS));
    }

    @RequestMapping(value = "/feed/{type}", method = RequestMethod.GET)
    public ResponseEntity<List<Exhibit>> getFeed(@PathVariable("type") String feedType) {
        switch (feedType) {
            case "new":
                return ResponseEntity.ok(
                        exhibits.values().stream()
                                .sorted(Comparator.comparing(Exhibit::getCreated))
                                .collect(Collectors.toList())
                );
            case "alphabetical":
                return ResponseEntity.ok(
                        exhibits.values().stream()
                                .sorted(Comparator.comparing(Exhibit::getTitle))
                                .collect(Collectors.toList())
                );
            default:
                return ResponseEntity.notFound().build();
        }
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
