package com.nelson.riskmanager.model;

public record StandardReference(
        String name, // Human name for the reference
        String code,    // e.g. "BEK nr. 835 §3"
        String section,
        String relevance        // why this standard applies
) {
}
