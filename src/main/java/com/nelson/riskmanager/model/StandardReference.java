package com.nelson.riskmanager.model;

public record StandardReference(
        String standardName,    // e.g. "BEK nr. 835 §3"
        String section,
        String relevance        // why this standard applies
) {}
