package com.airtribe.meditrack.entity.enums;

public enum BillType {
    STANDARD("Standard"),
    INSURANCE("Insurance"),
    SENIOR_CITIZEN("Senior Citizen"),
    EMERGENCY("Emergency");

    private final String displayName;

    BillType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    @Override
    public String toString() { return displayName; }
}