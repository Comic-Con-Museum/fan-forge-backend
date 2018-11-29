package org.comic_conmuseum.fan_forge.backend.endpoints.inputs;

import org.comic_conmuseum.fan_forge.backend.models.Survey;
import org.comic_conmuseum.fan_forge.backend.models.User;

import java.util.Map;

public class SurveyCreation {
    private Integer visits;
    private Map<String, Boolean> populations;
    private Integer nps;

    public Survey build(User by) {
        return new Survey(visits, populations, nps, by.getId());
    }
    
    public Integer getVisits() { return visits; }
    public Map<String, Boolean> getPopulations() { return populations; }
    public Integer getNps() { return nps; }
    
    public void setVisits(Integer visits) { this.visits = visits; }
    public void setPopulations(Map<String, Boolean> populations) { this.populations = populations; }
    public void setNps(Integer nps) { this.nps = nps; }
}
