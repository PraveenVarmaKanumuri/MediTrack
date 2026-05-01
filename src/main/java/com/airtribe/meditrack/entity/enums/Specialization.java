package com.airtribe.meditrack.entity.enums;

public enum Specialization {
    CARDIOLOGY("Cardiology", "Heart and cardiovascular system"),
    NEUROLOGY("Neurology", "Brain and nervous system"),
    ORTHOPEDICS("Orthopedics", "Bones and joints"),
    PEDIATRICS("Pediatrics", "Children's health"),
    DERMATOLOGY("Dermatology", "Skin conditions"),
    GENERAL_MEDICINE("General Medicine", "General health and wellness"),
    PSYCHIATRY("Psychiatry", "Mental health"),
    ONCOLOGY("Oncology", "Cancer treatment");

    private final String displayName;
    private final String description;

    Specialization(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}