package com.nelson.riskmanager.model;

import java.util.List;

public class Hazard {
    private String name;

    public int getHazardId() {
        return hazardId;
    }

    public void setHazardId(int hazardId) {
        this.hazardId = hazardId;
    }

    public int getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(int assessmentId) {
        this.assessmentId = assessmentId;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    private int hazardId;
    private int assessmentId;
    private String priority;        // PRIMARY, SECONDARY, TERTIARY
    private double[] boundingBox;   // [x_min, y_min, x_max, y_max] normalized 0-1
    private String description;
    private List<StandardReference> standardReferences;
    private List<String> recommendation;
    
    
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSeverity() {
        return priority;
    }

    public void setSeverity(String severity) {
        this.priority = severity;
    }

    public double[] getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(double[] boundingBox) {
        this.boundingBox = boundingBox;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<StandardReference> getStandardReferences() {
        return standardReferences;
    }

    public void setStandardReferences(List<StandardReference> standardReferences) {
        this.standardReferences = standardReferences;
    }

    public List<String> getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(List<String> recommendation) {
        this.recommendation = recommendation;
    }


}
