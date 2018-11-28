package org.comic_conmuseum.fan_forge.backend.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Survey {
    public final String supporter;
    public final long exhibit;
    public final int visits;
    public final Map<String, Boolean> populations;
    public final int nps;

    public Survey(int visits, Map<String, Boolean> populations, int nps, String supporter) {
        this.visits = visits;
        this.populations = populations;
        this.nps = nps;
        this.supporter = supporter;
    }

    public Survey(ResultSet rs, @SuppressWarnings("unused") int rowIndex) throws SQLException {
        this.supporter = rs.getString("supporter");
        this.exhibit = rs.getLong("exhibit");
        this.visits = rs.getInt("visits");
        this.populations = new HashMap<>();
        this.populations.put("male", rs.getBoolean("pop_male"));
        this.populations.put("female", rs.getBoolean("pop_female"));
        this.populations.put("kids", rs.getBoolean("pop_kids"));
        this.populations.put("teenagers", rs.getBoolean("pop_teenagers"));
        this.populations.put("adults", rs.getBoolean("pop_adults"));
        this.nps = rs.getInt("nps");
    }
}
