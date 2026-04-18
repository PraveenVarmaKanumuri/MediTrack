package com.airtribe.meditrack.entity;

import com.airtribe.meditrack.entity.enums.Relationship;
import com.airtribe.meditrack.exception.InvalidDataException;

public class EmergencyContact {

    private final String name;
    private final String phone;
    private final Relationship relationship;

    private EmergencyContact(String name, String phone, Relationship relationship) {
        if (name == null || name.isBlank()) {
            throw new InvalidDataException("emergencyContact.name", "cannot be null or empty");
        }
        if (phone == null || phone.isBlank()) {
            throw new InvalidDataException("emergencyContact.phone", "cannot be null or empty");
        }
        if (relationship == null) {
            throw new InvalidDataException("emergencyContact.relationship", "cannot be null");
        }
        this.name = name;
        this.phone = phone;
        this.relationship = relationship;
    }

    public static EmergencyContact of(String name, String phone, Relationship relationship) {
        return new EmergencyContact(name, phone, relationship);
    }

    public String getName() { return name; }
    public String getPhone() { return phone; }
    public Relationship getRelationship() { return relationship; }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s", name, relationship.getDisplayName(), phone);
    }
}