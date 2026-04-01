package com.nelson.riskmanager.model;

public record DetectedHazard(
        String name,
        String severity,        // PRIMARY, SECONDARY, TERTIARY
        double[] boundingBox   // [x_min, y_min, x_max, y_max] normalized 0-1
) {
}
