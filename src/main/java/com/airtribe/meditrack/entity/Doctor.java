package com.airtribe.meditrack.entity;

import com.airtribe.meditrack.entity.enums.Specialization;
import com.airtribe.meditrack.entity.enums.WeekDay;
import com.airtribe.meditrack.util.Validator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a clinic doctor with a specialization, consultation fee, and weekly schedule.
 *
 * <p>Use {@link #create} for a standard Mon–Fri schedule or
 * {@link #createWithSchedule} for a custom set of {@link com.airtribe.meditrack.entity.enums.WeekDay}s.
 * {@link #isAvailableOn} returns {@code false} on non-working days and explicit leave dates.
 */
public class Doctor extends Person {

    private Specialization specialization;
    private double consultationFee;
    private final Set<WeekDay> workingDays;
    private final List<LocalDate> leaveDates;

    private Doctor(String id, String name, LocalDate dateOfBirth, String email,
                   String phone, Specialization specialization, double consultationFee,
                   Set<WeekDay> workingDays) {
        super(id, name, dateOfBirth, email, phone);
        Validator.requireNonNull(specialization, "specialization");
        Validator.requirePositive(consultationFee, "consultationFee");
        Validator.requireNonNull(workingDays, "workingDays");
        if (workingDays.isEmpty()) {
            throw new com.airtribe.meditrack.exception.InvalidDataException(
                    "workingDays", "cannot be empty");
        }
        this.specialization = specialization;
        this.consultationFee = consultationFee;
        this.workingDays = EnumSet.copyOf(workingDays);
        this.leaveDates = new ArrayList<>();
    }

    public static Doctor create(String id, String name, LocalDate dateOfBirth, String email,
                                String phone, Specialization specialization, double consultationFee) {
        return new Doctor(id, name, dateOfBirth, email, phone, specialization, consultationFee,
                EnumSet.of(WeekDay.MONDAY, WeekDay.TUESDAY, WeekDay.WEDNESDAY,
                        WeekDay.THURSDAY, WeekDay.FRIDAY));
    }

    public static Doctor createWithSchedule(String id, String name, LocalDate dateOfBirth,
                                            String email, String phone,
                                            Specialization specialization, double consultationFee,
                                            Set<WeekDay> workingDays) {
        return new Doctor(id, name, dateOfBirth, email, phone, specialization,
                consultationFee, workingDays);
    }

    @Override
    public String getRole() {
        return "Doctor";
    }

    public boolean isAvailableOn(LocalDate date) {
        WeekDay day = WeekDay.from(date.getDayOfWeek());
        return workingDays.contains(day) && !leaveDates.contains(date);
    }

    public void updateSpecialization(Specialization specialization) {
        Validator.requireNonNull(specialization, "specialization");
        this.specialization = specialization;
        markUpdated();
    }

    public void updateConsultationFee(double consultationFee) {
        Validator.requirePositive(consultationFee, "consultationFee");
        this.consultationFee = consultationFee;
        markUpdated();
    }

    public void updateWorkingDays(Set<WeekDay> workingDays) {
        Validator.requireNonNull(workingDays, "workingDays");
        if (workingDays.isEmpty()) {
            throw new com.airtribe.meditrack.exception.InvalidDataException(
                    "workingDays", "cannot be empty");
        }
        this.workingDays.clear();
        this.workingDays.addAll(workingDays);
        markUpdated();
    }

    public void addLeave(LocalDate date) {
        Validator.requireNonNull(date, "leaveDate");
        leaveDates.add(date);
        markUpdated();
    }

    public void cancelLeave(LocalDate date) {
        leaveDates.remove(date);
        markUpdated();
    }

    public Specialization getSpecialization() {
        return specialization;
    }

    public double getConsultationFee() {
        return consultationFee;
    }

    public Set<WeekDay> getWorkingDays() {
        return Collections.unmodifiableSet(workingDays);
    }

    public List<LocalDate> getLeaveDates() {
        return Collections.unmodifiableList(leaveDates);
    }

    @Override
    public String getDisplayInfo() {
        return super.getDisplayInfo() + String.format(
                " | Specialization: %s | Fee: %.2f | Working Days: %s",
                specialization.getDisplayName(),
                consultationFee,
                workingDays);
    }
}