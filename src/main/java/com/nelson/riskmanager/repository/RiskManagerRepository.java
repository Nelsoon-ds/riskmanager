package com.nelson.riskmanager.repository;

import com.nelson.riskmanager.model.Hazard;
import com.nelson.riskmanager.model.RiskAssessment;
import com.nelson.riskmanager.model.User;
import com.nelson.riskmanager.repository.Mappers.UserRowMapper;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Transactional
@Repository
public class RiskManagerRepository {
    private final JdbcTemplate jdbcTemplate;
    private final JdbcClient jdbcClient;


    public RiskManagerRepository(JdbcTemplate jdbcTemplate, JdbcClient jdbcClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcClient = jdbcClient;
    }




    public RiskAssessment loadReport(OAuth2User oauthUser) {
        String oauthId = oauthUser.getName();
        jdbcClient.sql("SELECT * FROM users WHERE oauth_id = :oauthId and provider = :provider");

        RiskAssessment riskData = new RiskAssessment();
        return riskData;
    }

    public Optional<User> findByOauthIdAndProvider(String oauthId, String provider) {
        return jdbcClient.sql("SELECT * FROM users WHERE oauth_id = :oauthId AND provider = :provider")
                .param("oauthId", oauthId)
                .param("provider", provider)
                .query(new UserRowMapper())
                .optional();

    }

    public void save(RiskAssessment riskAssessment, int userId) {

        // Use a keyholder to get DB made keys
        KeyHolder keyHolder = new GeneratedKeyHolder();
        // Prepare the data objects
        List<Hazard> hazards = riskAssessment.getHazards();
       // SQL inserts
        String RiskAssessmentSQL = "insert into RiskAssessment (user_id, overall_severity, summary) VALUES (?, ?, ?)";
        String HazardSQL = "insert into Hazard (name, severity, description, bounding_box, assessment_id) VALUES (?, ?, ?,?, ?)";
        String RecommendationSQL = "insert into Recommendation (rec_description, hazard_id) values(?,?)";
        String StandardReferenceSQL = "insert into StandardReference (section, name, relevance, hazard_id) VALUES (?, ?, ?, ?)";


        jdbcTemplate.update( con -> {
            PreparedStatement ps = con.prepareStatement(RiskAssessmentSQL, new String[]{"assessment_id"});
            ps.setInt(1, userId);
            ps.setString(2, riskAssessment.getOverallSeverity());
            ps.setString(3, riskAssessment.getSummary());
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

    public User saveUser(User newUser) {
        String userSQL = "insert into users (oauth_id, provider, name, email, created_at) VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(userSQL);
            ps.setString(1, newUser.getOauthId());
            ps.setString(2, newUser.getProvider());
            ps.setString(3, newUser.getName());
            ps.setString(4, newUser.getEmail());
            ps.setString(5, String.valueOf(newUser.getCreatedAt()));
            return ps;
        });
        // Fetch the generated ID back
        return jdbcClient.sql("SELECT * FROM users WHERE oauth_id = :oauthId AND provider = :provider")
                .param("oauthId", newUser.getOauthId())
                .param("provider", newUser.getProvider())
                .query(new UserRowMapper())
                .single();
    }
}



