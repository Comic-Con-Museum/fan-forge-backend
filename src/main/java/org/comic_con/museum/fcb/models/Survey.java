package org.comic_con.museum.fcb.models;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Survey {
    public final String supporter;
    public final String survey;

    public Survey(ResultSet rs) throws SQLException {
        this.supporter = rs.getString("supporter");
        this.survey = rs.getString("survey_data");
    }
}