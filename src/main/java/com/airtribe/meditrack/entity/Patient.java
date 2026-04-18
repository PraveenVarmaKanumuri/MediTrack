package com.airtribe.meditrack.entity;

import com.airtribe.meditrack.entity.enums.BloodGroup;
import com.airtribe.meditrack.exception.InvalidDataException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Patient extends Person implements Cloneable {

    private BloodGroup bloodGroup;
    private final List<String> medicalHistory;
    private final List<String> appointmentIds;
    private EmergencyContact emergencyContact;

    private Patient(String id, String name, LocalDate dateOfBirth, String email,
                    String phone, BloodGroup bloodGroup, EmergencyContact emergencyContact) {
        super(id, name, dateOfBirth, email, phone);
        if (bloodGroup == null) {
            throw new InvalidDataException("bloodGroup", "cannot be null");
        }
        if (emergencyContact == null) {
            throw new InvalidDataException("emergencyContact", "cannot be null");
        }
        this.bloodGroup = bloodGroup;
        this.emergencyContact = emergencyContact;
        this.medicalHistory = new ArrayList<>();
        this.appointmentIds = new ArrayList<>();
    }

    public static Patient create(String id, String name, LocalDate dateOfBirth, String email,
                                 String phone, BloodGroup bloodGroup,
                                 EmergencyContact emergencyContact) {
        return new Patient(id, name, dateOfBirth, email, phone, bloodGroup, emergencyContact);
    }

    @Override
    public String getRole() { return "Patient"; }

    // Domain methods
    public void addMedicalHistory(String entry) {
        if (entry == null || entry.isBlank()) {
            throw new InvalidDataException("medicalHistory", "entry cannot be null or empty");
        }
        medicalHistory.add(entry);
        markUpdated();
    }

    public void updateEmergencyContact(EmergencyContact emergencyContact) {
        if (emergencyContact == null) {
            throw new InvalidDataException("emergencyContact", "cannot be null");
        }
        this.emergencyContact = emergencyContact;
        markUpdated();
    }

    public void updateBloodGroup(BloodGroup bloodGroup) {
        if (bloodGroup == null) {
            throw new InvalidDataException("bloodGroup", "cannot be null");
        }
        this.bloodGroup = bloodGroup;
        markUpdated();
    }

    public void addAppointmentId(String appointmentId) {
        if (appointmentId == null || appointmentId.isBlank()) {
            throw new InvalidDataException("appointmentId", "cannot be null or empty");
        }
        appointmentIds.add(appointmentId);
        markUpdated();
    }

    public void removeAppointmentId(String appointmentId) {
        appointmentIds.remove(appointmentId);
        markUpdated();
    }

    // Shallow copy — medicalHistory and appointmentIds lists are shared
    @Override
    public Patient clone() {
        try {
            return (Patient) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Shallow clone failed", e);
        }
    }

    // Deep copy — all lists are fully independent
    public Patient deepClone() {
        Patient copy = this.clone();
        copy.medicalHistory.clear();
        copy.medicalHistory.addAll(this.medicalHistory);
        copy.appointmentIds.clear();
        copy.appointmentIds.addAll(this.appointmentIds);
        return copy;
    }

    // Getters
    public BloodGroup getBloodGroup() { return bloodGroup; }
    public EmergencyContact getEmergencyContact() { return emergencyContact; }

    public List<String> getMedicalHistory() {
        return Collections.unmodifiableList(medicalHistory);
    }

    public List<String> getAppointmentIds() {
        return Collections.unmodifiableList(appointmentIds);
    }

    @Override
    public String getDisplayInfo() {
        return super.getDisplayInfo() + String.format(
                " | Blood Group: %s | Emergency Contact: %s",
                bloodGroup.getDisplayName(),
                emergencyContact);
    }
}