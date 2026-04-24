package com.nelson.riskmanager.repository.Mappers;

import com.nelson.riskmanager.model.RiskAssessment;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RiskAssessmentRowMapper implements RowMapper<RiskAssessment> {

    @Override
    public RiskAssessment mapRow(ResultSet rs, int rowNum) throws SQLException {
        RiskAssessment riskAssessment = new RiskAssessment();
        return riskAssessment;
    }
}
