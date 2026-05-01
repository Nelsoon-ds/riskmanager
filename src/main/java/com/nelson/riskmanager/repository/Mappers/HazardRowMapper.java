package com.nelson.riskmanager.repository.Mappers;

import com.nelson.riskmanager.model.Hazard;
import com.nelson.riskmanager.model.RiskAssessment;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class HazardRowMapper implements RowMapper<Hazard> {

    @Override
    public Hazard mapRow(ResultSet rs, int rowNum) throws SQLException {
        Hazard hazard = new Hazard();
        hazard.setHazardId(rs.getInt("hazard_id"));
        hazard.setName(rs.getString("name"));
        hazard.setSeverity(rs.getString("severity"));
        hazard.setDescription(rs.getString("description"));
        hazard.setAssessmentId(rs.getInt("assessment_id"));
        hazard.setBoundingBox(parseBoundingBox(rs.getString("bounding_box")));

        return  hazard;
    }



    private double[] parseBoundingBox(String raw) {                                                                                                                                                                             │
        if (raw == null || raw.isBlank()) return new double[]{0, 0, 0, 0};                                                                                                                                                      │
        String[] parts = raw.replace("[", "").replace("]", "").split(",\\s*");                                                                                                                                                  │
        double[] result = new double[parts.length];                                                                                                                                                                             │
        for (int i = 0; i < parts.length; i++) result[i] = Double.parseDouble(parts[i].trim());                                                                                                                                 │
        return result;                                                                                                                                                                                                          │
    }
}
