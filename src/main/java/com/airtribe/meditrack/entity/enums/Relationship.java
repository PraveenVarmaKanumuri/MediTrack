package com.airtribe.meditrack.entity.enums;

public enum Relationship {
    SPOUSE("Spouse"),
    PARENT("Parent"),
    CHILD("Child"),
    SIBLING("Sibling"),
    FRIEND("Friend"),
    GUARDIAN("Guardian"),
    OTHER("Other");

    private final String displayName;

    Relationship(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    @Override
    public String toString() { return displayName; }
}