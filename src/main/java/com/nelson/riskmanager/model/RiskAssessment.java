package com.nelson.riskmanager.model;

import java.util.List;

public record RiskAssessment(
        List<Hazard> hazards,
        String overallSeverity,
        String summary
) {}

