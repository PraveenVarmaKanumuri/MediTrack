package com.airtribe.meditrack.entity;

import com.airtribe.meditrack.entity.enums.AppointmentStatus;
import com.airtribe.meditrack.exception.InvalidDataException;
import com.airtribe.meditrack.util.Validator;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a clinic appointment between a patient and a doctor.
 *
 * <p>Status transitions follow a strict state machine enforced by
 * {@link com.airtribe.meditrack.entity.enums.AppointmentStatus#canTransitionTo}:
 * {@code PENDING → CONFIRMED → COMPLETED → PAID} (terminal),
 * with {@code CANCELLED} reachable from {@code PENDING} or {@code CONFIRMED}.
 *
 * <p>{@link #deepClone()} uses the factory method to avoid the {@code final List} aliasing
 * problem that would occur with a plain {@code super.clone()}.
 */
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
        Validator.requireNonBlank(patientId, "patientId");
        Validator.requireNonBlank(doctorId, "doctorId");
        Validator.requireNonNull(appointmentDate, "appointmentDate");
        Validator.requireFutureDate(appointmentDate, "appointmentDate");
        Validator.requireNonNull(appointmentTime, "appointmentTime");
        Validator.requireNonBlank(reason, "reason");
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

    public void confirm() { transitionTo(AppointmentStatus.CONFIRMED); }
    public void cancel() { transitionTo(AppointmentStatus.CANCELLED); }
    public void complete() { transitionTo(AppointmentStatus.COMPLETED); }
    public void markBillPaid() { transitionTo(AppointmentStatus.PAID); }

    private void transitionTo(AppointmentStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new InvalidDataException("status",
                    String.format("Cannot transition from %s to %s",
                            status.getDisplayName(), newStatus.getDisplayName()));
        }
        this.status = newStatus;
        markUpdated();
    }

    public void reschedule(LocalDate newDate, LocalTime newTime) {
        Validator.requireNonNull(newDate, "appointmentDate");
        Validator.requireFutureDate(newDate, "appointmentDate");
        Validator.requireNonNull(newTime, "appointmentTime");
        if (status != AppointmentStatus.PENDING && status != AppointmentStatus.CONFIRMED) {
            throw new InvalidDataException("status",
                    "Cannot reschedule a cancelled or completed appointment");
        }
        this.appointmentDate = newDate;
        this.appointmentTime = newTime;
        markUpdated();
    }

    public void addNote(String note) {
        Validator.requireNonBlank(note, "note");
        notes.add(note);
        markUpdated();
    }

    public void updateReason(String reason) {
        Validator.requireNonBlank(reason, "reason");
        this.reason = reason;
        markUpdated();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Appointment that = (Appointment) o;
        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public Appointment clone() {
        try {
            return (Appointment) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Shallow clone failed", e);
        }
    }

    public Appointment deepClone() {
        // Cannot reassign final List fields after super.clone() — both references point to the
        // same ArrayList. Construct a fresh instance and copy state explicitly instead.
        Appointment copy = Appointment.create(getId(), patientId, doctorId,
                appointmentDate, appointmentTime, reason);
        notes.forEach(copy::addNote);
        return copy;
    }

    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public LocalDate getAppointmentDate() { return appointmentDate; }
    public LocalTime getAppointmentTime() { return appointmentTime; }
    public AppointmentStatus getStatus() { return status; }
    public String getReason() { return reason; }
    public List<String> getNotes() { return Collections.unmodifiableList(notes); }

    @Override
    public String toString() {
        return String.format(
                "Appointment[%s] Patient: %s | Doctor: %s | Date: %s %s | Status: %s | Reason: %s",
                getId(), patientId, doctorId, appointmentDate, appointmentTime,
                status.getDisplayName(), reason);
    }
}