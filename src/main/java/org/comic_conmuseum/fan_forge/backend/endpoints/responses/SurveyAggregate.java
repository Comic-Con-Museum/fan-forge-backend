package org.comic_conmuseum.fan_forge.backend.endpoints.responses;

import java.util.Map;

public class SurveyAggregate {
    public final long total;
    public final int nps;
    public final double[] visitsExpected;
    public final Map<String, Float> populationsExpected;
    
    public SurveyAggregate(long total, int nps, double[] visitsExpected, Map<String, Float> populationsExpected) {
        this.total = total;
        this.nps = nps;
        this.visitsExpected = visitsExpected;
        this.populationsExpected = populationsExpected;
    }
}
