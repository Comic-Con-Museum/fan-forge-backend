package org.comic_conmuseum.fan_forge.backend.models;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Survey {
    public final String supporter;
    public final String survey;

    public Survey(ResultSet rs, int rowIndex) throws SQLException {
        this.supporter = rs.getString("supporter");
        this.survey = rs.getString("survey_data");
    }
}
