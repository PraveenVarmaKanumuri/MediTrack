package com.airtribe.meditrack;

import com.airtribe.meditrack.concurrency.AppointmentReminderScheduler;
import com.airtribe.meditrack.constants.Constants;
import com.airtribe.meditrack.entity.Appointment;
import com.airtribe.meditrack.entity.Bill;
import com.airtribe.meditrack.entity.BillSummary;
import com.airtribe.meditrack.entity.Doctor;
import com.airtribe.meditrack.entity.EmergencyContact;
import com.airtribe.meditrack.entity.Patient;
import com.airtribe.meditrack.entity.enums.AppointmentStatus;
import com.airtribe.meditrack.entity.enums.BloodGroup;
import com.airtribe.meditrack.entity.enums.Relationship;
import com.airtribe.meditrack.entity.enums.Specialization;
import com.airtribe.meditrack.interfaces.AnalyticsService;
import com.airtribe.meditrack.interfaces.AppointmentService;
import com.airtribe.meditrack.interfaces.BillingStrategy;
import com.airtribe.meditrack.interfaces.DoctorService;
import com.airtribe.meditrack.interfaces.PatientService;
import com.airtribe.meditrack.observer.ConsoleNotificationObserver;
import com.airtribe.meditrack.service.AnalyticsServiceImpl;
import com.airtribe.meditrack.service.AppointmentServiceImpl;
import com.airtribe.meditrack.service.DoctorServiceImpl;
import com.airtribe.meditrack.service.PatientServiceImpl;
import com.airtribe.meditrack.strategy.EmergencyBillingStrategy;
import com.airtribe.meditrack.strategy.InsuranceBillingStrategy;
import com.airtribe.meditrack.strategy.SeniorCitizenBillingStrategy;
import com.airtribe.meditrack.strategy.StandardBillingStrategy;
import com.airtribe.meditrack.util.AIHelper;
import com.airtribe.meditrack.util.AppConfig;
import com.airtribe.meditrack.util.CSVPersistence;
import com.airtribe.meditrack.util.ConsoleHelper;
import com.airtribe.meditrack.util.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final Scanner scanner = new Scanner(System.in);
    private static DoctorService doctorService;
    private static PatientService patientService;
    private static AppointmentService appointmentService;
    private static AnalyticsService analyticsService;
    private static AIHelper aiHelper;
    private static AppointmentReminderScheduler reminderScheduler;

    public static void main(String[] args) {
        initializeServices();
        new File("data").mkdirs();

        if (Arrays.asList(args).contains("--loadData")) {
            loadFromCSV();
        } else {
            loadSampleData();
        }

        printWelcome();
        runMainLoop();
        reminderScheduler.shutdown();
    }

    private static void loadFromCSV() {
        logger.info("Loading data from CSV files...");
        try {
            CSVPersistence.loadDoctors(doctorService);
        } catch (Exception e) {
            logger.error("Could not load doctors: {} | Cause: {}",
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "none");
        }
        try {
            CSVPersistence.loadPatients(patientService);
        } catch (Exception e) {
            logger.error("Could not load patients: {} | Cause: {}",
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "none");
        }
        // Deserialize appointments if the serialized file exists
        java.io.File serFile = new java.io.File(Constants.APPOINTMENTS_SER_FILE);
        if (serFile.exists()) {
            try {
                java.util.List<Appointment> appointments =
                        SerializationUtil.deserialize(Constants.APPOINTMENTS_SER_FILE);
                appointments.forEach(a ->
                        ((com.airtribe.meditrack.service.AppointmentServiceImpl) appointmentService)
                                .importAppointment(a));
                logger.info("Deserialized {} appointment(s) from {}",
                        appointments.size(), Constants.APPOINTMENTS_SER_FILE);
            } catch (Exception e) {
                logger.error("Could not deserialize appointments: {} | Cause: {}",
                        e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "none");
            }
        }
    }

    private static void saveToCSV() {
        try {
            CSVPersistence.saveDoctors(doctorService.getAllDoctors());
            CSVPersistence.savePatients(patientService.getAllPatients());
            System.out.println("[Success] Doctors and patients saved to CSV in data/");
        } catch (Exception e) {
            logger.error("Failed to save CSV data: {}", e.getMessage());
            System.out.println("[Error] Failed to save CSV data: " + e.getMessage());
        }
        try {
            // Serialize full appointments list via Java Serialization
            SerializationUtil.serialize(
                    new java.util.ArrayList<>(appointmentService.getAllAppointments()),
                    Constants.APPOINTMENTS_SER_FILE);
            System.out.println("[Success] Appointments serialized to " + Constants.APPOINTMENTS_SER_FILE);
        } catch (Exception e) {
            logger.error("Failed to serialize appointments: {}", e.getMessage());
            System.out.println("[Error] Failed to serialize appointments: " + e.getMessage());
        }
    }

    // ─── Initialization ───────────────────────────────────────────────────────

    private static void initializeServices() {
        doctorService = new DoctorServiceImpl();
        patientService = new PatientServiceImpl();
        AppointmentServiceImpl appointmentServiceImpl =
                new AppointmentServiceImpl(doctorService, patientService);
        appointmentServiceImpl.registerObserver(new ConsoleNotificationObserver());
        appointmentService = appointmentServiceImpl;
        analyticsService = new AnalyticsServiceImpl(
                doctorService, patientService, appointmentService);
        aiHelper = new AIHelper(doctorService, appointmentService);
        reminderScheduler = new AppointmentReminderScheduler();
        logger.info("Services initialized");
        logger.info("App: {}", AppConfig.getInstance().getAppName());
    }

    private static void loadSampleData() {
        doctorService.addDoctor("Dr. Arjun Mehta",
                LocalDate.of(1975, 3, 15),
                "arjun@meditrack.com", "9876543210",
                Specialization.CARDIOLOGY, 800.0);

        doctorService.addDoctor("Dr. Priya Sharma",
                LocalDate.of(1980, 7, 22),
                "priya@meditrack.com", "9876543211",
                Specialization.NEUROLOGY, 750.0);

        doctorService.addDoctor("Dr. Ravi Kumar",
                LocalDate.of(1978, 11, 5),
                "ravi@meditrack.com", "9876543212",
                Specialization.GENERAL_MEDICINE, 500.0);

        patientService.registerPatient("Sneha Patel",
                LocalDate.of(1995, 6, 10),
                "sneha@email.com", "9123456789",
                BloodGroup.B_POSITIVE,
                EmergencyContact.of("Raj Patel", "9123456780", Relationship.SPOUSE));

        patientService.registerPatient("Vikram Singh",
                LocalDate.of(1988, 2, 28),
                "vikram@email.com", "9123456790",
                BloodGroup.O_POSITIVE,
                EmergencyContact.of("Anita Singh", "9123456791", Relationship.SPOUSE));

        logger.info("Sample data loaded");
    }

    // ─── Welcome ──────────────────────────────────────────────────────────────

    private static void printWelcome() {
        System.out.println("\n" + Constants.SEPARATOR);
        System.out.println("      " + Constants.APP_NAME);
        System.out.println(Constants.SEPARATOR);
    }

    // ─── Main Loop ────────────────────────────────────────────────────────────

    private static void runMainLoop() {
        while (true) {
            printMainMenu();
            int choice = readInt("Enter choice: ");
            switch (choice) {
                case 1 -> doctorMenu();
                case 2 -> patientMenu();
                case 3 -> appointmentMenu();
                case 4 -> billingMenu();
                case 5 -> aiMenu();
                case 6 -> analyticsMenu();
                case 7 -> saveToCSV();
                case 8 -> {
                    System.out.println("\nThank you for using MediTrack. Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void printMainMenu() {
        System.out.println("\n" + Constants.SEPARATOR);
        System.out.println("                 MAIN MENU");
        System.out.println(Constants.SEPARATOR);
        System.out.println("  1. Doctor Management");
        System.out.println("  2. Patient Management");
        System.out.println("  3. Appointment Management");
        System.out.println("  4. Billing");
        System.out.println("  5. AI Assistant");
        System.out.println("  6. Analytics");
        System.out.println("  7. Save Data to CSV");
        System.out.println("  8. Exit");
        System.out.println(Constants.SEPARATOR);
    }

    // ─── Doctor Menu ──────────────────────────────────────────────────────────

    private static void doctorMenu() {
        while (true) {
            System.out.println("\n--- Doctor Management ---");
            System.out.println("1. Add Doctor");
            System.out.println("2. View All Doctors");
            System.out.println("3. Find Doctor");
            System.out.println("4. Search by Specialization");
            System.out.println("5. Search by Fee Range");
            System.out.println("6. Search Available Doctors by Date");
            System.out.println("7. Remove Doctor");
            System.out.println("0. Back");

            int choice = readInt("Enter choice: ");
            switch (choice) {
                case 1 -> addDoctor();
                case 2 -> viewAllDoctors();
                case 3 -> findDoctor();
                case 4 -> searchDoctorsBySpecialization();
                case 5 -> searchDoctorsByFeeRange();
                case 6 -> searchAvailableDoctors();
                case 7 -> removeDoctor();
                case 0 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void addDoctor() {
        System.out.println("\n--- Add Doctor ---");
        String name = readString("Name: ");
        LocalDate dob = readDate("Date of Birth (yyyy-MM-dd): ");
        String email = readString("Email: ");
        String phone = readString("Phone (10 digits): ");
        Specialization spec = readSpecialization();
        double fee = readDouble("Consultation Fee: ");
        try {
            Doctor doctor = doctorService.addDoctor(
                    name, dob, email, phone, spec, fee);
            System.out.println("\n[Success] Doctor added: " + doctor.getDisplayInfo());
        } catch (Exception e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }

    private static void viewAllDoctors() {
        List<Doctor> doctors = doctorService.getAllDoctors();
        if (doctors.isEmpty()) {
            System.out.println("No doctors registered.");
            return;
        }
        System.out.println("\n--- All Doctors ---");
        doctors.forEach(d -> System.out.println(d.getDisplayInfo()));
    }

    private static void findDoctor() {
        Doctor doctor = ConsoleHelper.selectDoctor(doctorService.getAllDoctors());
        if (doctor != null) {
            System.out.println("\n" + doctor.getDisplayInfo());
            System.out.println(doctor.getAuditInfo());
        }
    }

    private static void searchDoctorsBySpecialization() {
        Specialization spec = readSpecialization();
        List<Doctor> doctors = doctorService.searchBySpecialization(spec);
        if (doctors.isEmpty()) {
            System.out.println("No doctors found for " + spec.getDisplayName());
            return;
        }
        Doctor doctor = ConsoleHelper.selectDoctor(doctors);
        if (doctor != null) {
            System.out.println("\n" + doctor.getDisplayInfo());
        }
    }

    private static void searchDoctorsByFeeRange() {
        double min = readDouble("Min Fee: ");
        double max = readDouble("Max Fee: ");
        try {
            List<Doctor> doctors = doctorService.searchByFeeRange(min, max);
            if (doctors.isEmpty()) {
                System.out.println("No doctors found in fee range.");
                return;
            }
            Doctor doctor = ConsoleHelper.selectDoctor(doctors);
            if (doctor != null) {
                System.out.println("\n" + doctor.getDisplayInfo());
            }
        } catch (Exception e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }

    private static void searchAvailableDoctors() {
        LocalDate date = readDate("Date (yyyy-MM-dd): ");
        List<Doctor> doctors = doctorService.searchAvailableOn(date);
        if (doctors.isEmpty()) {
            System.out.println("No doctors available on " + date);
            return;
        }
        Doctor doctor = ConsoleHelper.selectDoctor(doctors);
        if (doctor != null) {
            System.out.println("\n" + doctor.getDisplayInfo());
        }
    }

    private static void removeDoctor() {
        Doctor doctor = ConsoleHelper.selectDoctor(doctorService.getAllDoctors());
        if (doctor == null) return;
        try {
            doctorService.removeDoctor(doctor.getId());
            System.out.println("[Success] Doctor removed.");
        } catch (Exception e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }

    // ─── Patient Menu ─────────────────────────────────────────────────────────

    private static void patientMenu() {
        while (true) {
            System.out.println("\n--- Patient Management ---");
            System.out.println("1. Register Patient");
            System.out.println("2. View All Patients");
            System.out.println("3. Find Patient");
            System.out.println("4. Search by Name");
            System.out.println("5. Search by Age Range");
            System.out.println("6. Add Medical History");
            System.out.println("7. Remove Patient");
            System.out.println("0. Back");

            int choice = readInt("Enter choice: ");
            switch (choice) {
                case 1 -> registerPatient();
                case 2 -> viewAllPatients();
                case 3 -> findPatient();
                case 4 -> searchPatientByName();
                case 5 -> searchPatientByAge();
                case 6 -> addMedicalHistory();
                case 7 -> removePatient();
                case 0 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void registerPatient() {
        System.out.println("\n--- Register Patient ---");
        String name = readString("Name: ");
        LocalDate dob = readDate("Date of Birth (yyyy-MM-dd): ");
        String email = readString("Email: ");
        String phone = readString("Phone (10 digits): ");
        BloodGroup bloodGroup = readBloodGroup();

        System.out.println("\n--- Emergency Contact ---");
        String ecName = readString("Name: ");
        String ecPhone = readString("Phone (10 digits): ");
        Relationship relationship = readRelationship();

        try {
            EmergencyContact ec = EmergencyContact.of(ecName, ecPhone, relationship);
            Patient patient = patientService.registerPatient(
                    name, dob, email, phone, bloodGroup, ec);
            System.out.println("\n[Success] Patient registered: " + patient.getDisplayInfo());
        } catch (Exception e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }

    private static void viewAllPatients() {
        List<Patient> patients = patientService.getAllPatients();
        if (patients.isEmpty()) {
            System.out.println("No patients registered.");
            return;
        }
        System.out.println("\n--- All Patients ---");
        patients.forEach(p -> System.out.println(p.getDisplayInfo()));
    }

    private static void findPatient() {
        Patient patient = ConsoleHelper.selectPatient(patientService.getAllPatients());
        if (patient != null) {
            System.out.println("\n" + patient.getDisplayInfo());
            System.out.println("Medical History: " + patient.getMedicalHistory());
            System.out.println("Appointments: " + patient.getAppointmentIds());
            System.out.println(patient.getAuditInfo());
        }
    }

    private static void searchPatientByName() {
        String name = readString("Name to search: ");
        try {
            List<Patient> patients = patientService.searchPatient(name, true);
            if (patients.isEmpty()) {
                System.out.println("No patients found.");
                return;
            }
            Patient patient = ConsoleHelper.selectPatient(patients);
            if (patient != null) {
                System.out.println("\n" + patient.getDisplayInfo());
            }
        } catch (Exception e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }

    private static void searchPatientByAge() {
        int min = readInt("Min Age: ");
        int max = readInt("Max Age: ");
        try {
            List<Patient> patients = patientService.searchPatient(min, max);
            if (patients.isEmpty()) {
                System.out.println("No patients found.");
                return;
            }
            Patient patient = ConsoleHelper.selectPatient(patients);
            if (patient != null) {
                System.out.println("\n" + patient.getDisplayInfo());
            }
        } catch (Exception e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }

    private static void addMedicalHistory() {
        Patient patient = ConsoleHelper.selectPatient(patientService.getAllPatients());
        if (patient == null) return;
        String entry = readString("Medical History Entry: ");
        try {
            patientService.addMedicalHistory(patient.getId(), entry);
            System.out.println("[Success] Medical history added.");
        } catch (Exception e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }

    private static void removePatient() {
        Patient patient = ConsoleHelper.selectPatient(patientService.getAllPatients());
        if (patient == null) return;
        try {
            patientService.removePatient(patient.getId());
            System.out.println("[Success] Patient removed.");
        } catch (Exception e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }

    // ─── Appointment Menu ─────────────────────────────────────────────────────

    private static void appointmentMenu() {
        while (true) {
            System.out.println("\n--- Appointment Management ---");
            System.out.println("1.  Book Appointment");
            System.out.println("2.  View All Appointments");
            System.out.println("3.  Find Appointment");
            System.out.println("4.  View by Patient");
            System.out.println("5.  View by Doctor");
            System.out.println("6.  Confirm Appointment");
            System.out.println("7.  Cancel Appointment");
            System.out.println("8.  Complete Appointment");
            System.out.println("9.  Reschedule Appointment");
            System.out.println("10. Schedule Reminder");
            System.out.println("0.  Back");

            int choice = readInt("Enter choice: ");
            switch (choice) {
                case 1 -> bookAppointment();
                case 2 -> viewAllAppointments();
                case 3 -> findAppointment();
                case 4 -> viewAppointmentsByPatient();
                case 5 -> viewAppointmentsByDoctor();
                case 6 -> confirmAppointment();
                case 7 -> cancelAppointment();
                case 8 -> completeAppointment();
                case 9 -> rescheduleAppointment();
                case 10 -> scheduleReminder();
                case 0 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void bookAppointment() {
        System.out.println("\n--- Book Appointment ---");

        Patient patient = ConsoleHelper.selectPatient(patientService.getAllPatients());
        if (patient == null) return;

        LocalDate date = readDate("Date (yyyy-MM-dd): ");

        List<Doctor> availableDoctors = doctorService.searchAvailableOn(date);
        if (availableDoctors.isEmpty()) {
            System.out.println("No doctors available on " + date);
            return;
        }

        Doctor doctor = ConsoleHelper.selectDoctor(availableDoctors);
        if (doctor == null) return;

        System.out.println("\n[AI] Fetching available slots...");
        List<LocalTime> slots = aiHelper.suggestAppointmentSlots(doctor.getId(), date);
        if (slots.isEmpty()) {
            System.out.println("No available slots for this doctor on " + date);
            return;
        }

        LocalTime time = readTime("Time (HH:mm): ");
        String reason = readString("Reason: ");

        try {
            Appointment appointment = appointmentService.createAppointment(
                    patient.getId(), doctor.getId(), date, time, reason);
            System.out.println("\n[Success] Appointment booked: " + appointment);
        } catch (Exception e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }

    private static void viewAllAppointments() {
        List<Appointment> appointments = appointmentService.getAllAppointments();
        if (appointments.isEmpty()) {
            System.out.println("No appointments found.");
            return;
        }
        System.out.println("\n--- All Appointments ---");
        appointments.forEach(System.out::println);
    }

    private static void findAppointment() {
        Appointment appointment = ConsoleHelper.selectAppointment(
                appointmentService.getAllAppointments());
        if (appointment != null) {
            System.out.println("\n" + appointment);
            System.out.println(appointment.getAuditInfo());
        }
    }

    private static void viewAppointmentsByPatient() {
        Patient patient = ConsoleHelper.selectPatient(patientService.getAllPatients());
        if (patient == null) return;
        List<Appointment> appointments = appointmentService.findByPatientId(patient.getId());
        if (appointments.isEmpty()) {
            System.out.println("No appointments found for " + patient.getName());
            return;
        }
        appointments.forEach(System.out::println);
    }

    private static void viewAppointmentsByDoctor() {
        Doctor doctor = ConsoleHelper.selectDoctor(doctorService.getAllDoctors());
        if (doctor == null) return;
        List<Appointment> appointments = appointmentService.findByDoctorId(doctor.getId());
        if (appointments.isEmpty()) {
            System.out.println("No appointments found for " + doctor.getName());
            return;
        }
        appointments.forEach(System.out::println);
    }

    private static void confirmAppointment() {
        Appointment appointment = ConsoleHelper.selectAppointment(
                appointmentService.getAllAppointments());
        if (appointment == null) return;
        try {
            appointmentService.confirmAppointment(appointment.getId());
            System.out.println("[Success] Appointment confirmed.");
        } catch (Exception e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }

    private static void cancelAppointment() {
        Appointment appointment = ConsoleHelper.selectAppointment(
                appointmentService.getAllAppointments());
        if (appointment == null) return;
        try {
            appointmentService.cancelAppointment(appointment.getId());
            System.out.println("[Success] Appointment cancelled.");
        } catch (Exception e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }

    private static void completeAppointment() {
        Appointment appointment = ConsoleHelper.selectAppointment(
                appointmentService.getAllAppointments());
        if (appointment == null) return;
        try {
            appointmentService.completeAppointment(appointment.getId());
            System.out.println("[Success] Appointment completed.");
        } catch (Exception e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }

    private static void rescheduleAppointment() {
        Appointment appointment = ConsoleHelper.selectAppointment(
                appointmentService.getAllAppointments());
        if (appointment == null) return;
        LocalDate newDate = readDate("New Date (yyyy-MM-dd): ");
        LocalTime newTime = readTime("New Time (HH:mm): ");
        try {
            appointmentService.rescheduleAppointment(
                    appointment.getId(), newDate, newTime);
            System.out.println("[Success] Appointment rescheduled.");
        } catch (Exception e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }

    private static void scheduleReminder() {
        Appointment appointment = ConsoleHelper.selectAppointment(
                appointmentService.getAllAppointments());
        if (appointment == null) return;
        try {
            reminderScheduler.scheduleImmediateReminder(appointment);
            System.out.println("[Success] Reminder scheduled.");
        } catch (Exception e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }

    // ─── Billing Menu ─────────────────────────────────────────────────────────

    private static void billingMenu() {
        while (true) {
            System.out.println("\n--- Billing ---");
            System.out.println("1. Generate Bill");
            System.out.println("2. Pay Bill");
            System.out.println("0. Back");

            int choice = readInt("Enter choice: ");
            switch (choice) {
                case 1 -> generateBill();
                case 2 -> payBill();
                case 0 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void generateBill() {
        List<Appointment> billable = appointmentService.getAllAppointments().stream()
                .filter(a -> a.getStatus() != AppointmentStatus.PAID
                        && a.getStatus() != AppointmentStatus.CANCELLED)
                .collect(Collectors.toList());
        if (billable.isEmpty()) {
            System.out.println("No appointments available for billing.");
            return;
        }
        Appointment appointment = ConsoleHelper.selectAppointment(billable);
        if (appointment == null) return;
        BillingStrategy strategy = readBillingStrategy();
        try {
            Bill bill = appointmentService.generateBill(appointment.getId(), strategy);
            System.out.println("\n[Success] Bill generated: " + bill);
            System.out.println(bill.getPaymentSummary());
        } catch (Exception e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }

    private static void payBill() {
        List<Bill> unpaidBills = appointmentService.getAllBills().stream()
                .filter(b -> !b.isPaid())
                .collect(Collectors.toList());
        Bill bill = ConsoleHelper.selectBill(unpaidBills);
        if (bill == null) return;
        try {
            BillSummary summary = appointmentService.finalizeAndPay(bill.getId());
            System.out.println("\n[Success] Payment processed.");
            System.out.println(summary);
        } catch (Exception e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }

    // ─── AI Menu ──────────────────────────────────────────────────────────────

    private static void aiMenu() {
        while (true) {
            System.out.println("\n--- AI Assistant ---");
            System.out.println("1. Recommend Doctor by Symptoms");
            System.out.println("2. Suggest Available Appointment Slots");
            System.out.println("0. Back");

            int choice = readInt("Enter choice: ");
            switch (choice) {
                case 1 -> recommendDoctor();
                case 2 -> suggestSlots();
                case 0 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void recommendDoctor() {
        String symptoms = readString("Describe your symptoms: ");
        List<Doctor> doctors = aiHelper.recommendDoctors(symptoms);
        if (doctors.isEmpty()) {
            System.out.println("No doctors found for your symptoms.");
            return;
        }
        System.out.println("\n--- Recommended Doctors ---");
        Doctor doctor = ConsoleHelper.selectDoctor(doctors);
        if (doctor != null) {
            System.out.println("\n" + doctor.getDisplayInfo());
        }
    }

    private static void suggestSlots() {
        Doctor doctor = ConsoleHelper.selectDoctor(doctorService.getAllDoctors());
        if (doctor == null) return;
        LocalDate date = readDate("Date (yyyy-MM-dd): ");
        List<LocalTime> slots = aiHelper.suggestAppointmentSlots(doctor.getId(), date);
        if (slots.isEmpty()) {
            System.out.println("No available slots for " + doctor.getName() + " on " + date);
        } else {
            System.out.println("\n[AI] Available slots:");
            slots.forEach(t -> System.out.println("   " + t));
        }
    }

    // ─── Analytics Menu ───────────────────────────────────────────────────────

    private static void analyticsMenu() {
        while (true) {
            System.out.println("\n--- Analytics ---");
            System.out.println("1. View Summary");
            System.out.println("2. Doctors by Specialization");
            System.out.println("3. Average Consultation Fee");
            System.out.println("4. Appointments per Doctor");
            System.out.println("5. Top 3 Doctors by Fee");
            System.out.println("0. Back");

            int choice = readInt("Enter choice: ");
            switch (choice) {
                case 1 -> analyticsService.printSummary();
                case 2 -> {
                    Specialization spec = readSpecialization();
                    List<Doctor> doctors = analyticsService
                            .getDoctorsBySpecialization(spec);
                    if (doctors.isEmpty()) {
                        System.out.println("No doctors found.");
                    } else {
                        doctors.forEach(d -> System.out.println(d.getDisplayInfo()));
                    }
                }
                case 3 -> System.out.printf("Average Fee: %.2f%n",
                        analyticsService.getAverageConsultationFee());
                case 4 -> analyticsService.getAppointmentsPerDoctor()
                        .forEach((id, count) ->
                                System.out.println("Doctor " + id
                                        + ": " + count + " appointments"));
                case 5 -> analyticsService.getTopDoctorsByFee(3)
                        .forEach(d -> System.out.println(d.getDisplayInfo()));
                case 0 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // ─── Input Helpers ────────────────────────────────────────────────────────

    private static String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static LocalDate readDate(String prompt) {
        while (true) {
            try {
                return LocalDate.parse(readString(prompt));
            } catch (Exception e) {
                System.out.println("Invalid date. Use yyyy-MM-dd.");
            }
        }
    }

    private static LocalTime readTime(String prompt) {
        while (true) {
            try {
                return LocalTime.parse(readString(prompt));
            } catch (Exception e) {
                System.out.println("Invalid time. Use HH:mm.");
            }
        }
    }

    private static Specialization readSpecialization() {
        System.out.println("Select Specialization:");
        Specialization[] values = Specialization.values();
        for (int i = 0; i < values.length; i++) {
            System.out.println((i + 1) + ". " + values[i].getDisplayName());
        }
        while (true) {
            int choice = readInt("Choice: ");
            if (choice >= 1 && choice <= values.length) {
                return values[choice - 1];
            }
            System.out.println("Invalid choice.");
        }
    }

    private static BloodGroup readBloodGroup() {
        System.out.println("Select Blood Group:");
        BloodGroup[] values = BloodGroup.values();
        for (int i = 0; i < values.length; i++) {
            System.out.println((i + 1) + ". " + values[i].getDisplayName());
        }
        while (true) {
            int choice = readInt("Choice: ");
            if (choice >= 1 && choice <= values.length) {
                return values[choice - 1];
            }
            System.out.println("Invalid choice.");
        }
    }

    private static Relationship readRelationship() {
        System.out.println("Select Relationship:");
        Relationship[] values = Relationship.values();
        for (int i = 0; i < values.length; i++) {
            System.out.println((i + 1) + ". " + values[i].getDisplayName());
        }
        while (true) {
            int choice = readInt("Choice: ");
            if (choice >= 1 && choice <= values.length) {
                return values[choice - 1];
            }
            System.out.println("Invalid choice.");
        }
    }

    private static BillingStrategy readBillingStrategy() {
        System.out.println("Select Billing Strategy:");
        System.out.println("1. Standard");
        System.out.println("2. Insurance");
        System.out.println("3. Senior Citizen");
        System.out.println("4. Emergency");
        while (true) {
            int choice = readInt("Choice: ");
            switch (choice) {
                case 1 -> { return new StandardBillingStrategy(); }
                case 2 -> {
                    double coverage = readDouble("Insurance Coverage %: ");
                    return new InsuranceBillingStrategy(coverage);
                }
                case 3 -> { return new SeniorCitizenBillingStrategy(); }
                case 4 -> { return new EmergencyBillingStrategy(); }
                default -> System.out.println("Invalid choice.");
            }
        }
    }
}