package org.comic_conmuseum.fan_forge.backend.endpoints.responses;

import org.comic_conmuseum.fan_forge.backend.models.Survey;

import java.util.Map;

public class SurveyView {
    public final int visits;
    public final Map<String, Boolean> populations;
    public final int rating;
    
    public SurveyView(Survey viewOf) {
        this.visits = viewOf.visits;
        this.populations = viewOf.populations;
        this.rating = viewOf.rating;
    }
}
