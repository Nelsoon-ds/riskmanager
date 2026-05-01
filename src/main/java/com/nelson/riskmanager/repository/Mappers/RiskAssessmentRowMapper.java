package com.nelson.riskmanager.repository.Mappers;

import com.nelson.riskmanager.model.RiskAssessment;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class RiskAssessmentRowMapper implements RowMapper<RiskAssessment> {

    @Override
    public RiskAssessment mapRow(ResultSet rs, int rowNum) throws SQLException {
        RiskAssessment riskAssessment = new RiskAssessment();
        riskAssessment.setAssessmentId(rs.getInt("assessment_id"));
        riskAssessment.setOverallSeverity(rs.getString("overall_severity"));
        riskAssessment.setSummary(rs.getString("summary"));
        riskAssessment.setImage_path(rs.getString("image_path"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) riskAssessment.setCreated_at(ts.toLocalDateTime());
        return riskAssessment;
    }

}
