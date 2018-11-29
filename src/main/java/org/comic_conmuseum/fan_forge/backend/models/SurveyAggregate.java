package org.comic_conmuseum.fan_forge.backend.models;

import javax.sql.RowSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class SurveyAggregate {
    public final int nps;
    public final float[] visitsExpected;
    public final Map<String, Float> populationsExpected;
    
    public SurveyAggregate(int nps, float[] visitsExpected, Map<String, Float> populationsExpected) {
        this.nps = nps;
        this.visitsExpected = visitsExpected;
        this.populationsExpected = populationsExpected;
    }
    
    public SurveyAggregate(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
        int totalSupports = rs.getInt("total_supports");
        int netSupports = rs.getInt("net_supports");
        // nps = %positive - %negative = (positive/total) - (negative/total) = (positive - negative)/total
        // ...and net_supports is (positive - negative)
        float nps = netSupports / (float) totalSupports;
        this.nps = Math.round(nps);
        
        
    }
}
