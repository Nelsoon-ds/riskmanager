package com.nelson.riskmanager.model;

import java.util.List;

public record InitialAnalysis(
    List<DetectedHazard> hazards,
    String overallSeverity,
    String summary
)
{}


