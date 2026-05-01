package com.airtribe.meditrack.interfaces;

import com.airtribe.meditrack.entity.Appointment;
import com.airtribe.meditrack.entity.Bill;
import com.airtribe.meditrack.entity.BillSummary;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Service contract for appointment lifecycle management.
 *
 * <p>Covers creation, status transitions (confirm/cancel/complete), rescheduling,
 * bill generation, and observer registration. The Observer pattern is wired here
 * so that any status change automatically notifies registered
 * {@link AppointmentObserver}s.
 */
public interface AppointmentService {

    Appointment createAppointment(String patientId, String doctorId,
                                  LocalDate date, LocalTime time, String reason);

    Appointment findById(String id);

    List<Appointment> findByPatientId(String patientId);

    List<Appointment> findByDoctorId(String doctorId);

    List<Appointment> findByDate(LocalDate date);

    List<Appointment> getAllAppointments();

    void confirmAppointment(String appointmentId);

    void cancelAppointment(String appointmentId);

    void completeAppointment(String appointmentId);

    void rescheduleAppointment(String appointmentId, LocalDate newDate, LocalTime newTime);

    Bill generateBill(String appointmentId, BillingStrategy strategy);

    BillSummary finalizeAndPay(String billId);

    List<Bill> getAllBills();

    List<Appointment> getAppointmentsPerDoctor(String doctorId);

    int getTotalAppointments();

    void registerObserver(AppointmentObserver observer);
    void removeObserver(AppointmentObserver observer);
}