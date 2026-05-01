package com.airtribe.meditrack.entity;

import com.airtribe.meditrack.entity.enums.BloodGroup;
import com.airtribe.meditrack.util.Validator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a clinic patient with blood group, medical history, and an emergency contact.
 *
 * <p>Medical history entries and appointment IDs are append-only through validated mutators.
 * {@link #deepClone()} constructs a fully independent copy by using the factory method;
 * {@code super.clone()} alone is insufficient because {@code final List} fields cannot
 * be reassigned after a shallow clone.
 */
public class Patient extends Person implements Cloneable {

    private BloodGroup bloodGroup;
    private final List<String> medicalHistory;
    private final List<String> appointmentIds;
    private EmergencyContact emergencyContact;

    private Patient(String id, String name, LocalDate dateOfBirth, String email,
                    String phone, BloodGroup bloodGroup, EmergencyContact emergencyContact) {
        super(id, name, dateOfBirth, email, phone);
        Validator.requireNonNull(bloodGroup, "bloodGroup");
        Validator.requireNonNull(emergencyContact, "emergencyContact");
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

    public void addMedicalHistory(String entry) {
        Validator.requireNonBlank(entry, "medicalHistory");
        medicalHistory.add(entry);
        markUpdated();
    }

    public void updateEmergencyContact(EmergencyContact emergencyContact) {
        Validator.requireNonNull(emergencyContact, "emergencyContact");
        this.emergencyContact = emergencyContact;
        markUpdated();
    }

    public void updateBloodGroup(BloodGroup bloodGroup) {
        Validator.requireNonNull(bloodGroup, "bloodGroup");
        this.bloodGroup = bloodGroup;
        markUpdated();
    }

    public void addAppointmentId(String appointmentId) {
        Validator.requireNonBlank(appointmentId, "appointmentId");
        appointmentIds.add(appointmentId);
        markUpdated();
    }

    public void removeAppointmentId(String appointmentId) {
        appointmentIds.remove(appointmentId);
        markUpdated();
    }

    @Override
    public Patient clone() {
        try {
            return (Patient) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Shallow clone failed", e);
        }
    }

    public Patient deepClone() {
        // Cannot reassign final List fields after super.clone() — both references point to the
        // same ArrayList. Construct a fresh instance and copy state explicitly instead.
        Patient copy = Patient.create(getId(), getName(), getDateOfBirth(),
                getEmail(), getPhone(), bloodGroup, emergencyContact);
        medicalHistory.forEach(copy::addMedicalHistory);
        appointmentIds.forEach(copy::addAppointmentId);
        return copy;
    }

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