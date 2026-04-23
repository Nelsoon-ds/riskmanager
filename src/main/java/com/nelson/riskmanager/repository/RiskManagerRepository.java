package com.nelson.riskmanager.repository;

import com.nelson.riskmanager.model.Hazard;
import com.nelson.riskmanager.model.RiskAssessment;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


@Transactional
@Repository
public class RiskManagerRepository {
    private final JdbcTemplate jdbcTemplate;


    public RiskManagerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(RiskAssessment riskAssessment) {
        // Use a keyholder to get DB made keys
        KeyHolder keyHolder = new GeneratedKeyHolder();
        // Prepare the data objects
        List<Hazard> hazards = riskAssessment.getHazards();
       // SQL inserts
        String RiskAssessmentSQL = "insert into RiskAssessment (overall_severity, summary) VALUES (?, ?)";
        String HazardSQL = "insert into Hazard (name, severity, description, bounding_box, assessment_id) VALUES (?, ?, ?,?, ?)";
        String RecommendationSQL = "insert into Recommendation (rec_description, hazard_id) values(?,?)";
        String StandardReferenceSQL = "insert into StandardReference (section, name, relevance, hazard_id) VALUES (?, ?, ?, ?)";


        jdbcTemplate.update( con -> {
            PreparedStatement ps = con.prepareStatement(RiskAssessmentSQL, new String[]{"assessment_id"});
            ps.setString(1, riskAssessment.getOverallSeverity());
            ps.setString(2, riskAssessment.getSummary());
            return ps;
        }, keyHolder);
        int riskAssessmentId = Objects.requireNonNull(keyHolder.getKey()).intValue();
        // Loop over each Hazard to add in recommendations / references.
        for (Hazard item : hazards){
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(HazardSQL, new String[]{"hazard_id"});
                ps.setString(1, item.getName());
                ps.setString(2, item.getSeverity());
                ps.setString(3, item.getDescription());
                ps.setString(4, Arrays.toString(item.getBoundingBox())); // [0.1, 0.2] format
                ps.setInt(5, riskAssessmentId);
                return ps;
            }, keyHolder);

            int hazard_id = keyHolder.getKey().intValue();

            // Insert reccommendations for this hazard
            jdbcTemplate.batchUpdate(RecommendationSQL,new BatchPreparedStatementSetter() {
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ps.setString(1, item.getRecommendation().get(i));
                            ps.setInt(2, hazard_id);
                        }
                        public int getBatchSize() {
                            return item.getRecommendation().size();
                        }
                        });

            // Insert standard references  for this hazard
            jdbcTemplate.batchUpdate(StandardReferenceSQL, new BatchPreparedStatementSetter() {
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ps.setString(1, item.getStandardReferences().get(i).section());
                            ps.setString(2, item.getStandardReferences().get(i).standardName());
                            ps.setString(3, item.getStandardReferences().get(i).relevance());
                            ps.setInt(4, hazard_id);
                        } public int getBatchSize() {
                            return item.getStandardReferences().size();
                        }});
        }}
}



