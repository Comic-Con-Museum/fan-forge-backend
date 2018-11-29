package org.comic_conmuseum.fan_forge.backend.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Survey {
    // These should _never_ be referenced directly if at all possible. It
    // must also never, ever be edited. If you edit it, I will slap you.
    public static final String[] POPULATIONS = {
            "male", "female", "kids", "teenagers", "adults"
    };
    
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
        for (String pop : POPULATIONS) {
            this.populations.put(pop, rs.getBoolean("pop_" + pop));
        }
        this.rating = rs.getInt("rating");
    }
}
