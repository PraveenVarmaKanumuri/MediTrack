package com.airtribe.meditrack.entity;

import com.airtribe.meditrack.entity.enums.AppointmentStatus;
import com.airtribe.meditrack.exception.InvalidDataException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Appointment extends MedicalEntity implements Cloneable {

    private final String patientId;
    private final String doctorId;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private AppointmentStatus status;
    private String reason;
    private final List<String> notes;

    private Appointment(String id, String patientId, String doctorId,
                        LocalDate appointmentDate, LocalTime appointmentTime, String reason) {
        super(id);
        if (patientId == null || patientId.isBlank()) {
            throw new InvalidDataException("patientId", "cannot be null or empty");
        }
        if (doctorId == null || doctorId.isBlank()) {
            throw new InvalidDataException("doctorId", "cannot be null or empty");
        }
        if (appointmentDate == null) {
            throw new InvalidDataException("appointmentDate", "cannot be null");
        }
        if (appointmentDate.isBefore(LocalDate.now())) {
            throw new InvalidDataException("appointmentDate", "cannot be in the past");
        }
        if (appointmentTime == null) {
            throw new InvalidDataException("appointmentTime", "cannot be null");
        }
        if (reason == null || reason.isBlank()) {
            throw new InvalidDataException("reason", "cannot be null or empty");
        }
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.reason = reason;
        this.status = AppointmentStatus.PENDING;
        this.notes = new ArrayList<>();
    }

    public static Appointment create(String id, String patientId, String doctorId,
                                     LocalDate appointmentDate, LocalTime appointmentTime,
                                     String reason) {
        return new Appointment(id, patientId, doctorId, appointmentDate, appointmentTime, reason);
    }

    @Override
    public String getEntityType() { return "Appointment"; }

    // Status transitions — uses enum's canTransitionTo logic
    public void confirm() {
        transitionTo(AppointmentStatus.CONFIRMED);
    }

    public void cancel() {
        transitionTo(AppointmentStatus.CANCELLED);
    }

    public void complete() {
        transitionTo(AppointmentStatus.COMPLETED);
    }

    private void transitionTo(AppointmentStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new InvalidDataException("status",
                    String.format("Cannot transition from %s to %s",
                            status.getDisplayName(), newStatus.getDisplayName()));
        }
        this.status = newStatus;
        markUpdated();
    }

    // Domain methods
    public void reschedule(LocalDate newDate, LocalTime newTime) {
        if (newDate == null || newDate.isBefore(LocalDate.now())) {
            throw new InvalidDataException("appointmentDate", "cannot be null or in the past");
        }
        if (newTime == null) {
            throw new InvalidDataException("appointmentTime", "cannot be null");
        }
        if (!status.canTransitionTo(AppointmentStatus.CONFIRMED)) {
            throw new InvalidDataException("status",
                    "Cannot reschedule a cancelled or completed appointment");
        }
        this.appointmentDate = newDate;
        this.appointmentTime = newTime;
        markUpdated();
    }

    public void addNote(String note) {
        if (note == null || note.isBlank()) {
            throw new InvalidDataException("note", "cannot be null or empty");
        }
        notes.add(note);
        markUpdated();
    }

    public void updateReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new InvalidDataException("reason", "cannot be null or empty");
        }
        this.reason = reason;
        markUpdated();
    }

    // Shallow copy
    @Override
    public Appointment clone() {
        try {
            return (Appointment) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Shallow clone failed", e);
        }
    }

    // Deep copy — notes list is fully independent
    public Appointment deepClone() {
        Appointment copy = this.clone();
        copy.notes.clear();
        copy.notes.addAll(this.notes);
        return copy;
    }

    // Getters
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public LocalDate getAppointmentDate() { return appointmentDate; }
    public LocalTime getAppointmentTime() { return appointmentTime; }
    public AppointmentStatus getStatus() { return status; }
    public String getReason() { return reason; }
    public List<String> getNotes() { return Collections.unmodifiableList(notes); }

    @Override
    public String toString() {
        return String.format("Appointment[%s] Patient: %s | Doctor: %s | Date: %s %s | Status: %s | Reason: %s",
                getId(), patientId, doctorId, appointmentDate, appointmentTime,
                status.getDisplayName(), reason);
    }
}