package org.comic_con.museum.fcb;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class ExhibitEndpoints {
    private static final Map<Integer, Exhibit> exhibits = new HashMap<>();
    private static int lastInserted = 0;

    {
        createExhibit(new ExhibitInput("demo title 1", "demo desc 1"));
        createExhibit(new ExhibitInput("demo title 2", "demo desc 2"));
        createExhibit(new ExhibitInput("demo title 3", "demo desc 3"));
        createExhibit(new ExhibitInput("demo title 4", "demo desc 4"));
        createExhibit(new ExhibitInput("demo title 5", "demo desc 5"));
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
                return ResponseEntity.badRequest().build();
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
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        String title;
        String description;
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
        Exhibit exhibit = new Exhibit(title, description, created);
        exhibits.put(lastInserted, exhibit);
        return ResponseEntity.ok(lastInserted++);
    }

    @RequestMapping(value = "/exhibit/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deleteExhibit(@PathVariable int id) {
        exhibits.remove(id);
        return ResponseEntity.noContent().build();
    }
}
