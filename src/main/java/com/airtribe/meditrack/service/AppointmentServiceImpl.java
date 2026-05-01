package com.airtribe.meditrack.service;

import com.airtribe.meditrack.constants.Constants;
import com.airtribe.meditrack.entity.Appointment;
import com.airtribe.meditrack.entity.Bill;
import com.airtribe.meditrack.entity.BillSummary;
import com.airtribe.meditrack.entity.Doctor;
import com.airtribe.meditrack.entity.Patient;
import com.airtribe.meditrack.entity.enums.AppointmentStatus;
import com.airtribe.meditrack.exception.AppointmentNotFoundException;
import com.airtribe.meditrack.exception.InvalidDataException;
import com.airtribe.meditrack.interfaces.AppointmentObserver;
import com.airtribe.meditrack.interfaces.AppointmentService;
import com.airtribe.meditrack.interfaces.BillingStrategy;
import com.airtribe.meditrack.interfaces.DoctorService;
import com.airtribe.meditrack.interfaces.PatientService;
import com.airtribe.meditrack.util.DataStore;
import com.airtribe.meditrack.util.IdGenerator;
import com.airtribe.meditrack.util.Validator;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class AppointmentServiceImpl implements AppointmentService {

    private final DataStore<Appointment> appointmentStore;
    private final DataStore<Bill> billStore;
    private final DoctorService doctorService;
    private final PatientService patientService;
    private final IdGenerator idGenerator;
    private final List<AppointmentObserver> observers;

    public AppointmentServiceImpl(DoctorService doctorService, PatientService patientService) {
        this.appointmentStore = new DataStore<>("Appointment");
        this.billStore = new DataStore<>("Bill");
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.idGenerator = IdGenerator.getInstance();
        this.observers = new CopyOnWriteArrayList<>();
    }

    @Override
    public void registerObserver(AppointmentObserver observer) {
        Validator.requireNonNull(observer, "observer");
        observers.add(observer);
    }
    @Override
    public void removeObserver(AppointmentObserver observer) {
        observers.remove(observer);
    }

    private void notifyCreated(Appointment a) {
        observers.forEach(o -> o.onAppointmentCreated(a));
    }

    private void notifyConfirmed(Appointment a) {
        observers.forEach(o -> o.onAppointmentConfirmed(a));
    }

    private void notifyCancelled(Appointment a) {
        observers.forEach(o -> o.onAppointmentCancelled(a));
    }

    private void notifyCompleted(Appointment a) {
        observers.forEach(o -> o.onAppointmentCompleted(a));
    }

    private void notifyRescheduled(Appointment a) {
        observers.forEach(o -> o.onAppointmentRescheduled(a));
    }

    @Override
    public Appointment createAppointment(String patientId, String doctorId,
                                         LocalDate date, LocalTime time, String reason) {
        Patient patient = patientService.findById(patientId);
        Doctor doctor = doctorService.findById(doctorId);

        if (!doctor.isAvailableOn(date)) {
            throw new InvalidDataException("appointmentDate",
                    "Doctor is not available on " + date);
        }

        long appointmentsOnDay = appointmentStore.findAll().stream()
                .filter(a -> a.getDoctorId().equals(doctorId)
                        && a.getAppointmentDate().equals(date))
                .count();

        if (appointmentsOnDay >= Constants.MAX_APPOINTMENTS_PER_DAY) {
            throw new InvalidDataException("appointmentDate",
                    "Doctor has reached max appointments for " + date);
        }

        boolean slotTaken = appointmentStore.findAll().stream()
                .filter(a -> a.getDoctorId().equals(doctorId)
                        && a.getAppointmentDate().equals(date)
                        && a.getAppointmentTime().equals(time)
                        && a.getStatus() != AppointmentStatus.CANCELLED)
                .findAny()
                .isPresent();

        if (slotTaken) {
            throw new InvalidDataException("appointmentTime",
                    "Time slot " + time + " is already booked for this doctor on " + date);
        }

        String id = idGenerator.generateAppointmentId();
        Appointment appointment = Appointment.create(id, patientId, doctorId, date, time, reason);
        appointmentStore.save(id, appointment);
        patient.addAppointmentId(id);
        notifyCreated(appointment);
        return appointment;
    }

    @Override
    public Appointment findById(String id) {
        Validator.requireNonBlank(id, "id");
        return appointmentStore.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
    }

    @Override
    public List<Appointment> findByPatientId(String patientId) {
        Validator.requireNonBlank(patientId, "patientId");
        return appointmentStore.findAll().stream()
                .filter(a -> a.getPatientId().equals(patientId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Appointment> findByDoctorId(String doctorId) {
        Validator.requireNonBlank(doctorId, "doctorId");
        return appointmentStore.findAll().stream()
                .filter(a -> a.getDoctorId().equals(doctorId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Appointment> findByDate(LocalDate date) {
        Validator.requireNonNull(date, "date");
        return appointmentStore.findAll().stream()
                .filter(a -> a.getAppointmentDate().equals(date))
                .collect(Collectors.toList());
    }

    @Override
    public List<Appointment> getAllAppointments() {
        return appointmentStore.findAll();
    }

    @Override
    public void confirmAppointment(String appointmentId) {
        Appointment appointment = findById(appointmentId);
        appointment.confirm();
        notifyConfirmed(appointment);
    }

    @Override
    public void cancelAppointment(String appointmentId) {
        Appointment appointment = findById(appointmentId);
        appointment.cancel();
        Patient patient = patientService.findById(appointment.getPatientId());
        patient.removeAppointmentId(appointmentId);
        notifyCancelled(appointment);
    }

    @Override
    public void completeAppointment(String appointmentId) {
        Appointment appointment = findById(appointmentId);
        appointment.complete();
        notifyCompleted(appointment);
    }

    @Override
    public void rescheduleAppointment(String appointmentId, LocalDate newDate, LocalTime newTime) {
        Appointment appointment = findById(appointmentId);
        Doctor doctor = doctorService.findById(appointment.getDoctorId());
        if (!doctor.isAvailableOn(newDate)) {
            throw new InvalidDataException("appointmentDate",
                    "Doctor is not available on " + newDate);
        }
        appointment.reschedule(newDate, newTime);
        notifyRescheduled(appointment);
    }

    @Override
    public Bill generateBill(String appointmentId, BillingStrategy strategy) {
        Appointment appointment = findById(appointmentId);

        boolean billExists = billStore.findAll().stream()
                .anyMatch(b -> b.getAppointmentId().equals(appointmentId));
        if (billExists) {
            throw new InvalidDataException("appointmentId",
                    "A bill has already been generated for this appointment");
        }

        Doctor doctor = doctorService.findById(appointment.getDoctorId());
        String billId = idGenerator.generateBillId();
        Bill bill = new Bill.Builder(billId, appointment.getPatientId(),
                appointmentId, doctor.getConsultationFee())
                .billingStrategy(strategy)
                .build();
        billStore.save(billId, bill);
        return bill;
    }

    @Override
    public List<Bill> getAllBills() {
        return billStore.findAll();
    }

    @Override
    public BillSummary finalizeAndPay(String billId) {
        Validator.requireNonBlank(billId, "billId");
        Bill bill = billStore.findById(billId)
                .orElseThrow(() -> new InvalidDataException("billId",
                        "No bill found with id: " + billId));
        if (bill.isPaid()) {
            throw new InvalidDataException("bill", "Bill is already paid");
        }
        bill.markAsPaid();
        Appointment appointment = findById(bill.getAppointmentId());
        appointment.markBillPaid();
        return bill.generateSummary();
    }

    public void importAppointment(Appointment appointment) {
        Validator.requireNonNull(appointment, "appointment");
        appointmentStore.save(appointment.getId(), appointment);
    }

    @Override
    public List<Appointment> getAppointmentsPerDoctor(String doctorId) {
        return findByDoctorId(doctorId);
    }

    @Override
    public int getTotalAppointments() {
        return appointmentStore.count();
    }
}