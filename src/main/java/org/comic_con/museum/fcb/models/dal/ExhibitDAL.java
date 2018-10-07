package org.comic_con.museum.fcb.models.dal;

import org.comic_con.museum.fcb.models.Exhibit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// TODO: Rewrite this class to do actual database access
// TODO: Might be better off as an instance class, we can have one for normal users and one for admins.
// nb: all example SQL here is written in Postgres; ideally, our DAL will abstract it away entirely.
/*
Schemae:

CREATE tabLe users (
    uid SERIAL PRIMARY KEY,
    email STRING NOT NULL,
    display_name STRING NOT NULL,
    -- 60 bytes for bcrypt; might need changing for other password hashing mechanisms
    pwd_hash BYTEA(60) NOT NULL,
    admin BOOLEAN NOT NULL,
    deleted BOOLEAN NOT NULL
)

CREATE TABLE exhibits (
    eid SERIAL PRIMARY KEY,
    title STRING NOT NULL,
    description STRING NOT NULL,
    author STRING REFERENCES users ON DELETE SET NULL ON UPDATE CASCADE,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- these two might be better off as a single enum
    archived BOOLEAN NOT NULL,
    deleted BOOLEAN NOT NULL
)

CREATE TABLE support (
    user STRING REFERENCES users ON DELETE CASCADE ON UPDATE CASCADE,
    exhibit SERIAL REFERENCES exhibits ON DELETE CASCADE ON UPDATE CASCADE,
    survey_data STRING,
    PRIMARY KEY (exhibit, user) -- we mostly get what users support an exhibit, so exhibits first
)

 */
public class ExhibitDAL {
    public enum FeedType {
        NEW("created_on"),
        ALPHABETICAL("title");

        private String column;
        FeedType(String column) { this.column = column; }

        public String column() { return this.column; }
    }

    private static final Logger LOG = LoggerFactory.getLogger(ExhibitDAL.class);

    public static final int FEED_PAGE_SIZE = 10;

    private static final Map<Integer, Exhibit> exhibits = new HashMap<>();
    private static int lastInserted = 0;

    public static Exhibit getById(int id) {
        return exhibits.get(id);
    }

    public static int create(Exhibit adding) {
        LOG.info("Creating exhibit: {}", adding);
        /*
        INSERT INTO exhibits
        (title, description, author)
        VALUES (?, ?, ?)
        RETURNING id
         */
        ++lastInserted;
        adding.setId(lastInserted);
        exhibits.put(lastInserted, adding);
        return lastInserted;
    }

    // TODO Should this return an enum for more information?
    public static boolean delete(int id, String user) {
        LOG.info("{} attempting to delete: {}", user, id);
        /*
        DELETE FROM exhibits
        WHERE id = ?
          AND author = ?
         */
        Exhibit exhibit = exhibits.get(id);
        if (exhibit == null) {
            return false;
        }
        if (!exhibit.getAuthor().equals(user)) {
            return false;
        }
        exhibits.remove(id);
        return true;
    }

    public static List<Exhibit> getFeed(int startIdx, FeedType feed) {
        LOG.info("Getting {} feed, sorted by {}", feed.name(), feed.column());
        /*
        SELECT id, title, description FROM exhibits
        ORDER BY ? -- TODO: Might require multiple functions, or validated string-concat'd input
        LIMIT 10
        OFFSET ?
         */
        if (startIdx > exhibits.size()) {
            return null;
        }
        Comparator<Exhibit> sorter;
        switch (feed) {
            case NEW:
                sorter = Comparator.comparing(Exhibit::getCreated);
                break;
            case ALPHABETICAL:
                sorter = Comparator.comparing(Exhibit::getTitle);
                break;
            default:
                return null;
        }
        List<Exhibit> sorted = exhibits.values().stream()
                .sorted(sorter)
                .collect(Collectors.toList());

        int endIdx = Math.min(sorted.size(), startIdx + FEED_PAGE_SIZE);

        return sorted.subList(startIdx, endIdx);
    }

    public static int getTotalCount() {
        LOG.info("Getting count");
        /*
        SELECT COUNT(*) FROM exhibits;
         */
        return exhibits.size();
    }

    public static boolean addSupporter(int eid, String user) {
        return addSupporter(eid, user, null);
    }

    public static boolean addSupporter(int eid, String user, String surveyData) {
        LOG.info("Adding support by {} of {}, data: {}", user, eid, surveyData);
        /*
        INSERT INTO supporters (exhibit, user, survey_data)
        VALUES (?, ?, ?);
         */
        Exhibit exhibit = exhibits.get(eid);
        if (exhibit == null) {
            return false;
        }
        exhibit.getSupporters().add(user);
        return true;
    }

    public static boolean removeSupporter(int eid, String user) {
        LOG.info("Removing support by {} on {}", user, eid);
        /*
        DELETE FROM supporters
        WHERE user = ?
          AND exhibit = ?;
         */
        Exhibit exhibit = exhibits.get(eid);
        if (exhibit == null) {
            return false;
        }
        exhibit.getSupporters().remove(user);
        return true;
    }
}
