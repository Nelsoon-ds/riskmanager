package com.nelson.riskmanager.model;

import java.time.LocalDateTime;
import java.util.List;

public class RiskAssessment {
    private int assessmentId;
    private List<Hazard> hazards;
    private String overallSeverity;
    private LocalDateTime created_at;
    private String image_path;

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getOverallSeverity() {
        return overallSeverity;
    }

    public void setOverallSeverity(String overallSeverity) {
        this.overallSeverity = overallSeverity;
    }

    public List<Hazard> getHazards() {
        return hazards;
    }

    public void setHazards(List<Hazard> hazards) {
        this.hazards = hazards;
    }

    private String summary;

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public String getImage_path() {
        return image_path;
    }
    public int getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(int assessmentId) {
        this.assessmentId = assessmentId;
    }
    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }
}

