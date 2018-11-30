package org.comic_conmuseum.fan_forge.backend.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Survey {
    public enum Population {
        MALE, FEMALE, KIDS, TEENAGERS, ADULTS;

        private final String lowercase;

        Population() {
            this.lowercase = this.name().toLowerCase();
        }

        public String displayName() { return lowercase; }
        public String columnName() { return "pop_" + this.lowercase; }
        public String sqlParam() { return ":" + columnName(); }

    }

    public final String supporter;
    public final int visits;
    public final Map<String, Boolean> populations;
    public final int rating;

    public Survey(int visits, Map<String, Boolean> populations, int rating, String supporter) {
        this.visits = visits;
        this.populations = populations;
        this.rating = rating;
        this.supporter = supporter;
    }

    public Survey(ResultSet rs, @SuppressWarnings("unused") int rowIndex) throws SQLException {
        this.supporter = rs.getString("supporter");
        this.visits = rs.getInt("visits");
        this.populations = new HashMap<>();
        for (Population pop : Population.values()) {
            this.populations.put(pop.displayName(), rs.getBoolean("pop_" + pop));
        }
        this.rating = rs.getInt("rating");
    }
}
