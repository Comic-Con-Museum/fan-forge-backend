package org.comic_conmuseum.fan_forge.backend.endpoints.inputs;

import org.comic_conmuseum.fan_forge.backend.models.Survey;
import org.comic_conmuseum.fan_forge.backend.models.User;

import java.util.Map;

public class SurveyCreation {
    private int visits;
    private Map<String, Boolean> populations;
    private int nps;

    public Survey build(User by) {
        return new Survey(visits, populations, nps, by.getId());
    }
}
