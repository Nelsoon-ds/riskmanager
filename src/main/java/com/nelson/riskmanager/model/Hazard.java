package com.nelson.riskmanager.model;

import java.util.List;

public record Hazard(
        String name,
        String severity,        // PRIMARY, SECONDARY, TERTIARY
        double[] boundingBox,   // [x_min, y_min, x_max, y_max] normalized 0-1
        String description,
        List<StandardReference> standardReferences,
        List<String> recommendations
) {}
