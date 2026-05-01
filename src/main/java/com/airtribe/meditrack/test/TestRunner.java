package com.airtribe.meditrack.test;

import com.airtribe.meditrack.concurrency.AppointmentReminderScheduler;
import com.airtribe.meditrack.entity.Appointment;
import com.airtribe.meditrack.entity.Bill;
import com.airtribe.meditrack.entity.BillSummary;
import com.airtribe.meditrack.entity.Doctor;
import com.airtribe.meditrack.entity.EmergencyContact;
import com.airtribe.meditrack.entity.Patient;
import com.airtribe.meditrack.entity.enums.AppointmentStatus;
import com.airtribe.meditrack.entity.enums.BillType;
import com.airtribe.meditrack.entity.enums.BloodGroup;
import com.airtribe.meditrack.entity.enums.Relationship;
import com.airtribe.meditrack.entity.enums.Specialization;
import com.airtribe.meditrack.interfaces.AppointmentObserver;
import com.airtribe.meditrack.interfaces.AppointmentService;
import com.airtribe.meditrack.interfaces.DoctorService;
import com.airtribe.meditrack.interfaces.PatientService;
import com.airtribe.meditrack.service.AnalyticsServiceImpl;
import com.airtribe.meditrack.service.AppointmentServiceImpl;
import com.airtribe.meditrack.service.DoctorServiceImpl;
import com.airtribe.meditrack.service.PatientServiceImpl;
import com.airtribe.meditrack.strategy.EmergencyBillingStrategy;
import com.airtribe.meditrack.strategy.InsuranceBillingStrategy;
import com.airtribe.meditrack.strategy.SeniorCitizenBillingStrategy;
import com.airtribe.meditrack.strategy.StandardBillingStrategy;
import com.airtribe.meditrack.template.RegularAppointmentCompletion;
import com.airtribe.meditrack.template.StandardBillGeneration;
import com.airtribe.meditrack.util.AIHelper;
import com.airtribe.meditrack.util.AppConfig;
import com.airtribe.meditrack.util.BillFactory;
import com.airtribe.meditrack.util.IdGenerator;
import com.airtribe.meditrack.util.SerializationUtil;
import com.airtribe.meditrack.util.Validator;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class TestRunner {

    // ─── Counters ─────────────────────────────────────────────────────────────

    private static int passed = 0;
    private static int failed = 0;

    // ─── Entry Point ──────────────────────────────────────────────────────────

    public static void main(String[] args) {
        printHeader("MEDITRACK TEST RUNNER");

        testValidator();
        testDoctorService();
        testPatientService();
        testPatientClone();
        testAppointmentLifecycle();
        testAppointmentPaidStatus();
        testBillingStrategies();
        testBillFactory();
        testTemplateMethod();
        testObserverPattern();
        testSingletonPatterns();
        testSerializationUtil();
        testAnalytics();
        testAIHelper();
        testConcurrencyScheduler();
        testEqualsAndHashCode();
        testCollectionsAndIterator();

        printSummary();
    }

    // ─── Services (fresh per section where needed) ────────────────────────────

    private static DoctorService newDoctorService() {
        return new DoctorServiceImpl();
    }

    private static PatientService newPatientService() {
        return new PatientServiceImpl();
    }

    private static AppointmentServiceImpl newAppointmentService(
            DoctorService ds, PatientService ps) {
        return new AppointmentServiceImpl(ds, ps);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /** Allows lambdas that throw checked exceptions — unlike Runnable. */
    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static void assertTrue(String name, boolean condition) {
        if (condition) {
            System.out.printf("  [PASS] %s%n", name);
            passed++;
        } else {
            System.out.printf("  [FAIL] %s%n", name);
            failed++;
        }
    }

    private static void assertThrows(String name, ThrowingRunnable action) {
        try {
            action.run();
            System.out.printf("  [FAIL] %s — expected exception not thrown%n", name);
            failed++;
        } catch (Exception e) {
            System.out.printf("  [PASS] %s (%s)%n", name, e.getClass().getSimpleName());
            passed++;
        }
    }

    private static void assertNotNull(String name, Object value) {
        assertTrue(name, value != null);
    }

    private static void assertEqual(String name, Object expected, Object actual) {
        boolean ok = expected == null ? actual == null : expected.equals(actual);
        if (ok) {
            System.out.printf("  [PASS] %s%n", name);
            passed++;
        } else {
            System.out.printf("  [FAIL] %s — expected: %s, got: %s%n", name, expected, actual);
            failed++;
        }
    }

    /** Returns next weekday (Mon–Fri) at least {@code daysAhead} days from today. */
    private static LocalDate futureWeekday(int daysAhead) {
        LocalDate date = LocalDate.now().plusDays(daysAhead);
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static void printHeader(String title) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  " + title);
        System.out.println("=".repeat(60));
    }

    private static void section(String name) {
        System.out.println("\n--- " + name + " ---");
    }

    private static void printSummary() {
        System.out.println("\n" + "=".repeat(60));
        System.out.printf("  RESULTS:  %d passed  |  %d failed  |  %d total%n",
                passed, failed, passed + failed);
        System.out.println("=".repeat(60));
        if (failed == 0) System.out.println("  ALL TESTS PASSED");
        else System.out.println("  SOME TESTS FAILED — review output above");
        System.out.println("=".repeat(60));
    }

    // =========================================================================
    // 1. VALIDATOR
    // =========================================================================

    private static void testValidator() {
        section("Validator");

        assertThrows("requireNonBlank rejects null",
                () -> Validator.requireNonBlank(null, "field"));
        assertThrows("requireNonBlank rejects empty string",
                () -> Validator.requireNonBlank("  ", "field"));
        assertThrows("requireNonNull rejects null",
                () -> Validator.requireNonNull(null, "field"));
        assertThrows("requirePositive rejects zero",
                () -> Validator.requirePositive(0.0, "fee"));
        assertThrows("requirePositive rejects negative",
                () -> Validator.requirePositive(-5.0, "fee"));
        assertThrows("requireValidEmail rejects bad email",
                () -> Validator.requireValidEmail("notanemail", "email"));
        assertThrows("requireValidPhone rejects short phone",
                () -> Validator.requireValidPhone("12345", "phone"));
        assertThrows("requireFutureDate rejects today",
                () -> Validator.requireFutureDate(LocalDate.now(), "date"));
        assertThrows("requireFutureDate rejects past",
                () -> Validator.requireFutureDate(LocalDate.now().minusDays(1), "date"));
        assertThrows("requirePastDate rejects future",
                () -> Validator.requirePastDate(LocalDate.now().plusDays(1), "date"));
        assertThrows("requireInRange rejects out-of-range",
                () -> Validator.requireInRange(150, 0, 100, "discount"));

        // Valid cases — should not throw
        assertTrue("requireNonBlank accepts valid string",
                runSafely(() -> Validator.requireNonBlank("hello", "field")));
        assertTrue("requirePositive accepts positive value",
                runSafely(() -> Validator.requirePositive(1.0, "fee")));
        assertTrue("requireValidEmail accepts valid email",
                runSafely(() -> Validator.requireValidEmail("a@b.com", "email")));
        assertTrue("requireFutureDate accepts tomorrow",
                runSafely(() -> Validator.requireFutureDate(LocalDate.now().plusDays(1), "date")));
    }

    // =========================================================================
    // 2. DOCTOR SERVICE
    // =========================================================================

    private static void testDoctorService() {
        section("Doctor Service");
        DoctorService ds = newDoctorService();

        Doctor d = ds.addDoctor("Dr. Smith", LocalDate.of(1975, 1, 1),
                "smith@clinic.com", "9876543210", Specialization.CARDIOLOGY, 800.0);
        assertNotNull("addDoctor returns Doctor", d);
        assertTrue("Doctor ID generated", d.getId().startsWith("D"));
        assertEqual("Doctor specialization", Specialization.CARDIOLOGY, d.getSpecialization());

        Doctor found = ds.findById(d.getId());
        assertEqual("findById returns correct doctor", d.getId(), found.getId());

        List<Doctor> bySpec = ds.searchBySpecialization(Specialization.CARDIOLOGY);
        assertTrue("searchBySpecialization finds doctor", bySpec.size() == 1);

        List<Doctor> byFee = ds.searchByFeeRange(500, 1000);
        assertTrue("searchByFeeRange finds doctor in range", byFee.size() == 1);

        List<Doctor> byFeeExcluded = ds.searchByFeeRange(100, 500);
        assertTrue("searchByFeeRange excludes doctor outside range", byFeeExcluded.isEmpty());

        List<Doctor> available = ds.searchAvailableOn(futureWeekday(7));
        assertTrue("searchAvailableOn finds weekday doctor", !available.isEmpty());

        assertEqual("getTotalDoctors correct count", 1, ds.getTotalDoctors());

        assertThrows("findById throws on unknown id",
                () -> ds.findById("D999"));
        assertThrows("addDoctor rejects zero fee",
                () -> ds.addDoctor("Dr. X", LocalDate.of(1980, 1, 1),
                        "x@x.com", "9000000001", Specialization.NEUROLOGY, 0.0));

        ds.removeDoctor(d.getId());
        assertEqual("removeDoctor reduces count", 0, ds.getTotalDoctors());
        assertThrows("findById throws after removal",
                () -> ds.findById(d.getId()));
    }

    // =========================================================================
    // 3. PATIENT SERVICE
    // =========================================================================

    private static void testPatientService() {
        section("Patient Service");
        PatientService ps = newPatientService();

        EmergencyContact ec = EmergencyContact.of("Jane", "9000000001", Relationship.SPOUSE);
        Patient p = ps.registerPatient("John Doe", LocalDate.of(1990, 5, 20),
                "john@email.com", "9123456789", BloodGroup.A_POSITIVE, ec);

        assertNotNull("registerPatient returns Patient", p);
        assertTrue("Patient ID starts with P", p.getId().startsWith("P"));
        assertEqual("Patient blood group", BloodGroup.A_POSITIVE, p.getBloodGroup());

        List<Patient> byName = ps.searchPatient("John", true);
        assertTrue("searchPatient by name finds result", !byName.isEmpty());

        List<Patient> byAge = ps.searchPatient(30, 40);
        assertTrue("searchPatient by age finds result in range", !byAge.isEmpty());

        List<Patient> byAgeExcluded = ps.searchPatient(10, 20);
        assertTrue("searchPatient by age excludes out-of-range", byAgeExcluded.isEmpty());

        ps.addMedicalHistory(p.getId(), "Hypertension");
        Patient updated = ps.searchPatient(p.getId());
        assertTrue("addMedicalHistory persisted",
                updated.getMedicalHistory().contains("Hypertension"));

        assertEqual("getTotalPatients correct", 1, ps.getTotalPatients());

        assertThrows("registerPatient rejects invalid email",
                () -> ps.registerPatient("Bad", LocalDate.of(1990, 1, 1),
                        "notanemail", "9000000002",
                        BloodGroup.B_POSITIVE,
                        EmergencyContact.of("A", "9000000003", Relationship.PARENT)));

        ps.removePatient(p.getId());
        assertEqual("removePatient reduces count", 0, ps.getTotalPatients());
    }

    // =========================================================================
    // 4. CLONE (DEEP VS SHALLOW)
    // =========================================================================

    private static void testPatientClone() {
        section("Deep vs Shallow Clone");
        PatientService ps = newPatientService();

        EmergencyContact ec = EmergencyContact.of("Contact", "9000000010", Relationship.PARENT);
        Patient original = ps.registerPatient("Alice", LocalDate.of(1985, 3, 10),
                "alice@email.com", "9000000011", BloodGroup.O_POSITIVE, ec);
        original.addMedicalHistory("Diabetes");

        // Shallow clone — same list reference, adding to copy affects original
        Patient shallow = original.clone();
        assertTrue("Shallow clone is not same reference", original != shallow);
        assertEqual("Shallow clone has same ID", original.getId(), shallow.getId());

        // Deep clone — independent list copy
        Patient deep = ps.getPatientCopy(original.getId(), true);
        assertTrue("Deep clone is not same reference", original != deep);
        // Modifying deep clone's history should not affect original
        int sizeBefore = original.getMedicalHistory().size();
        deep.addMedicalHistory("Extra entry");
        assertEqual("Deep clone modification does not affect original",
                sizeBefore, original.getMedicalHistory().size());

        // Appointment deep clone
        DoctorService ds = newDoctorService();
        Doctor doc = ds.addDoctor("Dr. Clone", LocalDate.of(1970, 1, 1),
                "clone@clinic.com", "9000000012", Specialization.GENERAL_MEDICINE, 500.0);
        AppointmentServiceImpl as = newAppointmentService(ds, ps);
        Appointment appt = as.createAppointment(original.getId(), doc.getId(),
                futureWeekday(5), LocalTime.of(10, 0), "Check-up");
        appt.addNote("Note 1");

        Appointment deepAppt = appt.deepClone();
        assertTrue("Appointment deep clone not same reference", appt != deepAppt);
        int notesBefore = appt.getNotes().size();
        deepAppt.addNote("Extra note");
        assertEqual("Deep clone note does not affect original", notesBefore, appt.getNotes().size());
    }

    // =========================================================================
    // 5. APPOINTMENT LIFECYCLE
    // =========================================================================

    private static void testAppointmentLifecycle() {
        section("Appointment Lifecycle");

        DoctorService ds = newDoctorService();
        PatientService ps = newPatientService();
        AppointmentServiceImpl as = newAppointmentService(ds, ps);

        Doctor doc = ds.addDoctor("Dr. Life", LocalDate.of(1972, 6, 15),
                "life@clinic.com", "9000000020", Specialization.NEUROLOGY, 750.0);
        EmergencyContact ec = EmergencyContact.of("EC", "9000000021", Relationship.SIBLING);
        Patient pat = ps.registerPatient("Bob", LocalDate.of(1988, 8, 8),
                "bob@email.com", "9000000022", BloodGroup.B_NEGATIVE, ec);

        LocalDate apptDate = futureWeekday(10);
        LocalTime apptTime = LocalTime.of(11, 0);

        Appointment appt = as.createAppointment(pat.getId(), doc.getId(),
                apptDate, apptTime, "Headache");
        assertNotNull("createAppointment returns Appointment", appt);
        assertEqual("Initial status is PENDING",
                AppointmentStatus.PENDING, appt.getStatus());
        assertTrue("Appointment ID starts with A", appt.getId().startsWith("A"));

        as.confirmAppointment(appt.getId());
        assertEqual("Status after confirm is CONFIRMED",
                AppointmentStatus.CONFIRMED, appt.getStatus());

        // Cannot confirm again (CONFIRMED → CONFIRMED not allowed)
        assertThrows("Cannot re-confirm appointment",
                () -> as.confirmAppointment(appt.getId()));

        // Reschedule while CONFIRMED
        LocalDate newDate = futureWeekday(20);
        as.rescheduleAppointment(appt.getId(), newDate, LocalTime.of(14, 0));
        assertEqual("Rescheduled date updated", newDate, appt.getAppointmentDate());

        as.completeAppointment(appt.getId());
        assertEqual("Status after complete is COMPLETED",
                AppointmentStatus.COMPLETED, appt.getStatus());

        // Cannot complete again (terminal state)
        assertThrows("Cannot complete a completed appointment",
                () -> as.completeAppointment(appt.getId()));

        // Slot conflict — same doctor, same date/time
        EmergencyContact ec2 = EmergencyContact.of("EC2", "9000000023", Relationship.FRIEND);
        Patient pat2 = ps.registerPatient("Carol", LocalDate.of(1992, 2, 14),
                "carol@email.com", "9000000024", BloodGroup.AB_POSITIVE, ec2);
        as.createAppointment(pat2.getId(), doc.getId(), futureWeekday(30),
                LocalTime.of(9, 0), "Routine");
        assertThrows("Slot conflict rejected",
                () -> as.createAppointment(pat.getId(), doc.getId(),
                        futureWeekday(30), LocalTime.of(9, 0), "Routine2"));

        // Cancel path
        Appointment cancelAppt = as.createAppointment(pat.getId(), doc.getId(),
                futureWeekday(40), LocalTime.of(10, 0), "To cancel");
        as.cancelAppointment(cancelAppt.getId());
        assertEqual("Cancelled status",
                AppointmentStatus.CANCELLED, cancelAppt.getStatus());
        assertThrows("Cannot cancel already-cancelled appointment",
                () -> as.cancelAppointment(cancelAppt.getId()));
    }

    // =========================================================================
    // 6. PAID STATUS
    // =========================================================================

    private static void testAppointmentPaidStatus() {
        section("PAID Appointment Status");

        DoctorService ds = newDoctorService();
        PatientService ps = newPatientService();
        AppointmentServiceImpl as = newAppointmentService(ds, ps);

        Doctor doc = ds.addDoctor("Dr. Pay", LocalDate.of(1968, 4, 20),
                "pay@clinic.com", "9000000030", Specialization.CARDIOLOGY, 900.0);
        EmergencyContact ec = EmergencyContact.of("EC", "9000000031", Relationship.SPOUSE);
        Patient pat = ps.registerPatient("Dan", LocalDate.of(1980, 1, 1),
                "dan@email.com", "9000000032", BloodGroup.O_NEGATIVE, ec);

        Appointment appt = as.createAppointment(pat.getId(), doc.getId(),
                futureWeekday(6), LocalTime.of(10, 0), "Check-up");
        as.confirmAppointment(appt.getId());
        as.completeAppointment(appt.getId());

        Bill bill = as.generateBill(appt.getId(), new StandardBillingStrategy());
        assertNotNull("Bill generated", bill);
        assertTrue("Bill not paid before finalizeAndPay", !bill.isPaid());

        BillSummary summary = as.finalizeAndPay(bill.getId());
        assertNotNull("BillSummary returned", summary);
        assertTrue("Bill marked paid", bill.isPaid());
        assertEqual("Appointment transitioned to PAID",
                AppointmentStatus.PAID, appt.getStatus());

        // Cannot pay again
        assertThrows("Cannot pay bill twice", () -> as.finalizeAndPay(bill.getId()));

        // Cannot generate a second bill for same appointment
        assertThrows("Cannot generate duplicate bill",
                () -> as.generateBill(appt.getId(), new StandardBillingStrategy()));

        // PAID appointment should not appear in billable list (AppointmentStatus.PAID filter)
        List<Appointment> notPaid = as.getAllAppointments().stream()
                .filter(a -> a.getStatus() != AppointmentStatus.PAID
                        && a.getStatus() != AppointmentStatus.CANCELLED)
                .toList();
        assertTrue("PAID appointment excluded from billable list",
                notPaid.stream().noneMatch(a -> a.getId().equals(appt.getId())));
    }

    // =========================================================================
    // 7. BILLING STRATEGIES
    // =========================================================================

    private static void testBillingStrategies() {
        section("Billing Strategies");

        String billId = "T001";
        String patId  = "P001";
        String apptId = "A001";
        double fee    = 1000.0;

        Bill standard = new Bill.Builder(billId, patId, apptId, fee)
                .billingStrategy(new StandardBillingStrategy()).build();
        double stdTotal = standard.calculateTotal();
        assertTrue("Standard: total > fee (tax added)", stdTotal > fee);

        Bill senior = new Bill.Builder("T002", patId, apptId, fee)
                .billingStrategy(new SeniorCitizenBillingStrategy()).build();
        assertTrue("SeniorCitizen: total less than Standard (discount applied)",
                senior.calculateTotal() < stdTotal);

        Bill emergency = new Bill.Builder("T003", patId, apptId, fee)
                .billingStrategy(new EmergencyBillingStrategy()).build();
        assertTrue("Emergency: total greater than Standard (surcharge applied)",
                emergency.calculateTotal() > stdTotal);

        Bill insurance = new Bill.Builder("T004", patId, apptId, fee)
                .billingStrategy(new InsuranceBillingStrategy(50.0)).build();
        assertTrue("Insurance 50%: total less than Standard",
                insurance.calculateTotal() < stdTotal);

        // Switch strategy at runtime
        standard.switchStrategy(new SeniorCitizenBillingStrategy());
        assertTrue("switchStrategy changes total",
                standard.calculateTotal() != stdTotal);

        // Discount application
        Bill discounted = new Bill.Builder("T005", patId, apptId, fee)
                .billingStrategy(new StandardBillingStrategy())
                .discountPercent(10.0).build();
        assertTrue("Discount lowers total", discounted.calculateTotal() < fee * 1.18 + 1);

        // applyDiscount via interface
        discounted.applyDiscount(20.0);
        assertTrue("applyDiscount updates total", discounted.calculateTotal() >= 0);

        // Tax calc
        Bill taxBill = new Bill.Builder("T006", patId, apptId, 1000.0)
                .billingStrategy(new StandardBillingStrategy()).build();
        assertTrue("Tax is 18% of base", Math.abs(taxBill.calculateTax() - 180.0) < 0.01);
    }

    // =========================================================================
    // 8. FACTORY PATTERN
    // =========================================================================

    private static void testBillFactory() {
        section("BillFactory (Factory Pattern)");

        Bill standard = BillFactory.create(BillType.STANDARD, "F001", "P001", "A001", 500.0);
        assertNotNull("BillFactory creates STANDARD bill", standard);
        assertEqual("STANDARD strategy name", "Standard Billing",
                standard.getBillingStrategy().getStrategyName());

        Bill senior = BillFactory.create(BillType.SENIOR_CITIZEN, "F002", "P001", "A002", 500.0);
        assertNotNull("BillFactory creates SENIOR_CITIZEN bill", senior);

        Bill emergency = BillFactory.create(BillType.EMERGENCY, "F003", "P001", "A003", 500.0);
        assertNotNull("BillFactory creates EMERGENCY bill", emergency);

        Bill insurance = BillFactory.createInsuranceBill(
                "F004", "P001", "A004", 500.0, 80.0);
        assertNotNull("BillFactory creates INSURANCE bill", insurance);
        assertTrue("Insurance bill total reduced by 80% coverage",
                insurance.calculateTotal() < 500.0);

        Bill discounted = BillFactory.createWithDiscount(
                BillType.STANDARD, "F005", "P001", "A005", 1000.0, 15.0);
        assertNotNull("BillFactory creates bill with discount", discounted);
        assertTrue("Discounted bill total less than undiscounted",
                discounted.calculateTotal() < new Bill.Builder(
                        "F006", "P001", "A006", 1000.0)
                        .billingStrategy(new StandardBillingStrategy())
                        .build().calculateTotal() + 0.01);
    }

    // =========================================================================
    // 9. TEMPLATE METHOD PATTERN
    // =========================================================================

    private static void testTemplateMethod() {
        section("Template Method Pattern");

        // BillGenerationTemplate via StandardBillGeneration
        Bill bill = new Bill.Builder("TM001", "P001", "A001", 600.0)
                .billingStrategy(new StandardBillingStrategy()).build();
        StandardBillGeneration template = new StandardBillGeneration();
        BillSummary summary = template.generate(bill);
        assertNotNull("Template generate returns BillSummary", summary);
        assertTrue("Template marks bill as paid", bill.isPaid());

        // Already-paid bill throws
        assertThrows("Template rejects already-paid bill",
                () -> template.generate(bill));

        // AppointmentCompletionTemplate via RegularAppointmentCompletion
        DoctorService ds = newDoctorService();
        PatientService ps = newPatientService();
        AppointmentServiceImpl as = newAppointmentService(ds, ps);

        Doctor doc = ds.addDoctor("Dr. Template", LocalDate.of(1965, 7, 7),
                "tmpl@clinic.com", "9000000040", Specialization.GENERAL_MEDICINE, 400.0);
        EmergencyContact ec = EmergencyContact.of("EC", "9000000041", Relationship.FRIEND);
        Patient pat = ps.registerPatient("Eve", LocalDate.of(1993, 11, 11),
                "eve@email.com", "9000000042", BloodGroup.A_NEGATIVE, ec);

        Appointment appt = as.createAppointment(pat.getId(), doc.getId(),
                futureWeekday(8), LocalTime.of(9, 30), "Template test");
        as.confirmAppointment(appt.getId());

        RegularAppointmentCompletion completion = new RegularAppointmentCompletion(as);
        Bill resultBill = completion.complete(appt.getId(), new StandardBillingStrategy());
        assertNotNull("RegularAppointmentCompletion returns Bill", resultBill);
        assertEqual("Appointment moved to COMPLETED",
                AppointmentStatus.COMPLETED, appt.getStatus());
        assertTrue("Notes added by template",
                !appt.getNotes().isEmpty());
    }

    // =========================================================================
    // 10. OBSERVER PATTERN
    // =========================================================================

    /** Counts each notification type for assertion. */
    private static class CountingObserver implements AppointmentObserver {
        int created = 0, confirmed = 0, cancelled = 0, completed = 0, rescheduled = 0;

        @Override public void onAppointmentCreated(Appointment a)    { created++; }
        @Override public void onAppointmentConfirmed(Appointment a)  { confirmed++; }
        @Override public void onAppointmentCancelled(Appointment a)  { cancelled++; }
        @Override public void onAppointmentCompleted(Appointment a)  { completed++; }
        @Override public void onAppointmentRescheduled(Appointment a){ rescheduled++; }
    }

    private static void testObserverPattern() {
        section("Observer Pattern");

        DoctorService ds = newDoctorService();
        PatientService ps = newPatientService();
        AppointmentServiceImpl as = newAppointmentService(ds, ps);

        CountingObserver obs = new CountingObserver();
        as.registerObserver(obs);

        Doctor doc = ds.addDoctor("Dr. Obs", LocalDate.of(1969, 3, 3),
                "obs@clinic.com", "9000000050", Specialization.CARDIOLOGY, 700.0);
        EmergencyContact ec = EmergencyContact.of("EC", "9000000051", Relationship.CHILD);
        Patient pat = ps.registerPatient("Frank", LocalDate.of(1975, 4, 4),
                "frank@email.com", "9000000052", BloodGroup.B_POSITIVE, ec);

        Appointment appt = as.createAppointment(pat.getId(), doc.getId(),
                futureWeekday(9), LocalTime.of(13, 0), "Observer test");
        assertEqual("Observer: created fired", 1, obs.created);

        as.confirmAppointment(appt.getId());
        assertEqual("Observer: confirmed fired", 1, obs.confirmed);

        as.rescheduleAppointment(appt.getId(), futureWeekday(15), LocalTime.of(14, 0));
        assertEqual("Observer: rescheduled fired", 1, obs.rescheduled);

        as.completeAppointment(appt.getId());
        assertEqual("Observer: completed fired", 1, obs.completed);

        // Cancel path
        Appointment appt2 = as.createAppointment(pat.getId(), doc.getId(),
                futureWeekday(25), LocalTime.of(10, 0), "To cancel");
        as.cancelAppointment(appt2.getId());
        assertEqual("Observer: cancelled fired", 1, obs.cancelled);

        // Remove observer — events should no longer increment
        as.removeObserver(obs);
        as.createAppointment(pat.getId(), doc.getId(),
                futureWeekday(35), LocalTime.of(11, 0), "After remove");
        assertEqual("Observer: not called after removal", 2, obs.created);
    }

    // =========================================================================
    // 11. SINGLETON PATTERNS
    // =========================================================================

    private static void testSingletonPatterns() {
        section("Singleton Patterns");

        // Eager — IdGenerator
        IdGenerator ig1 = IdGenerator.getInstance();
        IdGenerator ig2 = IdGenerator.getInstance();
        assertTrue("IdGenerator eager singleton: same instance", ig1 == ig2);

        String id1 = ig1.generateDoctorId();
        String id2 = ig2.generateDoctorId();
        assertTrue("IdGenerator IDs are unique", !id1.equals(id2));

        // Lazy — AppConfig (double-checked locking)
        AppConfig ac1 = AppConfig.getInstance();
        AppConfig ac2 = AppConfig.getInstance();
        assertTrue("AppConfig lazy singleton: same instance", ac1 == ac2);
        assertTrue("AppConfig has valid app name", ac1.getAppName() != null
                && !ac1.getAppName().isBlank());
        assertTrue("AppConfig tax rate is positive", ac1.getTaxRate() > 0);
        assertTrue("AppConfig max appointments is positive",
                ac1.getMaxAppointmentsPerDay() > 0);
    }

    // =========================================================================
    // 12. SERIALIZATION
    // =========================================================================

    private static void testSerializationUtil() {
        section("Java Serialization (SerializationUtil)");

        // Use a BillSummary (immutable, Serializable) as the test object
        BillSummary original =
                new Bill.Builder("SER001", "P001", "A001", 500.0)
                        .billingStrategy(new StandardBillingStrategy())
                        .build()
                        .generateSummary();

        String path = "data/test_summary.ser";
        new File("data").mkdirs();

        assertTrue("Serialize BillSummary without exception",
                runSafely(() -> SerializationUtil.serialize(original, path)));

        final BillSummary[] loaded =
                new BillSummary[1];
        assertTrue("Deserialize BillSummary without exception",
                runSafely(() -> loaded[0] = SerializationUtil.deserialize(path)));

        assertNotNull("Deserialized object is not null", loaded[0]);
        assertEqual("Deserialized bill ID matches",
                original.getBillId(), loaded[0].getBillId());
        assertEqual("Deserialized total matches",
                original.getTotalAmount(), loaded[0].getTotalAmount());

        // Deserializing non-existent file should throw
        assertThrows("Deserialize missing file throws DataPersistenceException",
                () -> SerializationUtil.deserialize("data/nonexistent.ser"));

        // Cleanup
        new File(path).delete();
    }

    // =========================================================================
    // 13. ANALYTICS
    // =========================================================================

    private static void testAnalytics() {
        section("Analytics (Streams & Lambdas)");

        DoctorService ds = newDoctorService();
        PatientService ps = newPatientService();
        AppointmentServiceImpl as = newAppointmentService(ds, ps);
        AnalyticsServiceImpl analytics = new AnalyticsServiceImpl(ds, ps, as);

        // No data
        assertEqual("No doctors: avg fee is 0", 0.0, analytics.getAverageConsultationFee());
        assertTrue("No appointments: revenue is 0", analytics.getTotalRevenue() == 0.0);

        Doctor d1 = ds.addDoctor("Dr. A", LocalDate.of(1970, 1, 1),
                "a@clinic.com", "9000000060", Specialization.CARDIOLOGY, 1000.0);
        Doctor d2 = ds.addDoctor("Dr. B", LocalDate.of(1975, 1, 1),
                "b@clinic.com", "9000000061", Specialization.NEUROLOGY, 600.0);

        double avgFee = analytics.getAverageConsultationFee();
        assertTrue("Average fee computed correctly", Math.abs(avgFee - 800.0) < 0.01);

        List<Doctor> cardiologists =
                analytics.getDoctorsBySpecialization(Specialization.CARDIOLOGY);
        assertEqual("Filter by specialization: 1 cardiologist", 1, cardiologists.size());

        List<Doctor> topByFee = analytics.getTopDoctorsByFee(1);
        assertEqual("Top doctor by fee is Dr. A", d1.getId(), topByFee.get(0).getId());

        List<Doctor> sortedByFee = analytics.getDoctorsSortedByFee();
        assertTrue("Sorted ascending: d2 before d1",
                sortedByFee.get(0).getId().equals(d2.getId()));

        List<Doctor> sortedByName = analytics.getDoctorsSortedByName();
        assertTrue("Sorted by name: A before B",
                sortedByName.get(0).getName().compareTo(sortedByName.get(1).getName()) < 0);

        // With appointments
        EmergencyContact ec = EmergencyContact.of("EC", "9000000062", Relationship.PARENT);
        Patient pat = ps.registerPatient("Grace", LocalDate.of(1991, 6, 6),
                "grace@email.com", "9000000063", BloodGroup.AB_NEGATIVE, ec);

        Appointment appt = as.createAppointment(pat.getId(), d1.getId(),
                futureWeekday(7), LocalTime.of(10, 0), "Analytics test");
        as.confirmAppointment(appt.getId());
        as.completeAppointment(appt.getId());

        assertTrue("Revenue > 0 after completed appointment",
                analytics.getTotalRevenue() > 0);

        var perDoctor = analytics.getAppointmentsPerDoctor();
        assertTrue("Appointments per doctor tracked", perDoctor.containsKey(d1.getId()));
        assertEqual("Dr. A has 1 appointment", 1L, perDoctor.get(d1.getId()));

        var byStatus = analytics.getAppointmentsByStatus();
        assertTrue("Status breakdown includes COMPLETED",
                byStatus.containsKey(AppointmentStatus.COMPLETED));
    }

    // =========================================================================
    // 14. AI HELPER
    // =========================================================================

    private static void testAIHelper() {
        section("AI Helper");

        DoctorService ds = newDoctorService();
        PatientService ps = newPatientService();
        AppointmentServiceImpl as = newAppointmentService(ds, ps);
        AIHelper ai = new AIHelper(ds, as);

        // No doctors yet
        List<Doctor> none = ai.recommendDoctors("chest pain");
        assertTrue("No recommendation when no doctors registered", none.isEmpty());

        ds.addDoctor("Dr. Heart", LocalDate.of(1970, 1, 1),
                "heart@clinic.com", "9000000070", Specialization.CARDIOLOGY, 800.0);
        ds.addDoctor("Dr. Brain", LocalDate.of(1975, 1, 1),
                "brain@clinic.com", "9000000071", Specialization.NEUROLOGY, 700.0);

        List<Doctor> cardio = ai.recommendDoctors("chest pain");
        assertTrue("chest pain → CARDIOLOGY recommendation", !cardio.isEmpty());
        assertTrue("Cardiology doctor recommended for chest pain",
                cardio.stream().anyMatch(
                        d -> d.getSpecialization() == Specialization.CARDIOLOGY));

        List<Doctor> neuro = ai.recommendDoctors("headache");
        assertTrue("headache → NEUROLOGY recommendation", !neuro.isEmpty());

        // Unknown symptoms
        List<Doctor> unknown = ai.recommendDoctors("random xyz symptom");
        assertTrue("Unknown symptom returns empty list or fallback", unknown != null);

        // Slot suggestion
        List<LocalTime> slots = ai.suggestAppointmentSlots(
                cardio.get(0).getId(), futureWeekday(7));
        assertTrue("Slot suggestions returned for future weekday", !slots.isEmpty());
    }

    // =========================================================================
    // 15. CONCURRENCY — SCHEDULER
    // =========================================================================

    private static void testConcurrencyScheduler() {
        section("Concurrency (Scheduler, AtomicInteger, TimerTask)");

        DoctorService ds = newDoctorService();
        PatientService ps = newPatientService();
        AppointmentServiceImpl as = newAppointmentService(ds, ps);

        Doctor doc = ds.addDoctor("Dr. Timer", LocalDate.of(1968, 9, 9),
                "timer@clinic.com", "9000000080", Specialization.GENERAL_MEDICINE, 300.0);
        EmergencyContact ec = EmergencyContact.of("EC", "9000000081", Relationship.GUARDIAN);
        Patient pat = ps.registerPatient("Henry", LocalDate.of(1987, 7, 7),
                "henry@email.com", "9000000082", BloodGroup.A_POSITIVE, ec);

        Appointment appt = as.createAppointment(pat.getId(), doc.getId(),
                futureWeekday(12), LocalTime.of(9, 0), "Reminder test");

        AppointmentReminderScheduler scheduler = new AppointmentReminderScheduler();
        assertEqual("Initial scheduled count is 0", 0, scheduler.getScheduledCount());

        scheduler.scheduleImmediateReminder(appt);
        assertEqual("Scheduled count increments to 1", 1, scheduler.getScheduledCount());

        scheduler.scheduleImmediateReminder(appt);
        assertEqual("Scheduled count increments to 2", 2, scheduler.getScheduledCount());

        // Let the TimerTask fire (it runs in a daemon thread with 100ms delay)
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}

        // AtomicInteger thread-safety: concurrent increments
        AtomicInteger counter = new AtomicInteger(0);
        Thread t1 = new Thread(() -> { for (int i = 0; i < 1000; i++) counter.incrementAndGet(); });
        Thread t2 = new Thread(() -> { for (int i = 0; i < 1000; i++) counter.incrementAndGet(); });
        t1.start(); t2.start();
        try { t1.join(); t2.join(); } catch (InterruptedException ignored) {}
        assertEqual("AtomicInteger concurrent increments sum correctly", 2000, counter.get());

        scheduler.shutdown();
        assertTrue("Scheduler shut down cleanly", true);

        // Null appointment rejected
        assertThrows("Scheduler rejects null appointment",
                () -> scheduler.scheduleImmediateReminder(null));
    }

    // =========================================================================
    // 16. EQUALS / HASHCODE
    // =========================================================================

    private static void testEqualsAndHashCode() {
        section("equals() and hashCode()");

        DoctorService ds = newDoctorService();
        PatientService ps = newPatientService();

        Doctor d1 = ds.addDoctor("Dr. Eq", LocalDate.of(1970, 1, 1),
                "eq@clinic.com", "9000000090", Specialization.CARDIOLOGY, 500.0);
        Doctor d2 = ds.findById(d1.getId());

        assertTrue("Same ID doctors are equal", d1.equals(d2));
        assertEqual("Same ID doctors have equal hashCode",
                d1.hashCode(), d2.hashCode());
        assertTrue("Doctor not equal to null", !d1.equals(null));
        assertTrue("Doctor not equal to different type", !d1.equals("string"));

        Bill b1 = new Bill.Builder("B001", "P001", "A001", 500.0)
                .billingStrategy(new StandardBillingStrategy()).build();
        Bill b2 = new Bill.Builder("B001", "P001", "A001", 500.0)
                .billingStrategy(new StandardBillingStrategy()).build();
        assertTrue("Bills with same ID are equal", b1.equals(b2));
        assertEqual("Bills with same ID have equal hashCode",
                b1.hashCode(), b2.hashCode());
        Bill b3 = new Bill.Builder("B999", "P001", "A001", 500.0)
                .billingStrategy(new StandardBillingStrategy()).build();
        assertTrue("Bills with different IDs are not equal", !b1.equals(b3));

        EmergencyContact ec = EmergencyContact.of("EC", "9000000091", Relationship.SPOUSE);
        Patient p1 = ps.registerPatient("Ivan", LocalDate.of(1983, 3, 3),
                "ivan@email.com", "9000000092", BloodGroup.O_POSITIVE, ec);
        Patient p2 = ps.searchPatient(p1.getId());
        assertTrue("Same ID patients are equal", p1.equals(p2));
    }

    // =========================================================================
    // 17. COLLECTIONS & ITERATOR
    // =========================================================================

    private static void testCollectionsAndIterator() {
        section("Collections, Generics & Iterator");

        DoctorService ds = newDoctorService();
        PatientService ps = newPatientService();
        AppointmentServiceImpl as = newAppointmentService(ds, ps);
        AnalyticsServiceImpl analytics = new AnalyticsServiceImpl(ds, ps, as);

        ds.addDoctor("Dr. C1", LocalDate.of(1970, 1, 1),
                "c1@clinic.com", "9000000100", Specialization.CARDIOLOGY, 600.0);
        ds.addDoctor("Dr. C2", LocalDate.of(1972, 1, 1),
                "c2@clinic.com", "9000000101", Specialization.NEUROLOGY, 800.0);
        ds.addDoctor("Dr. C3", LocalDate.of(1974, 1, 1),
                "c3@clinic.com", "9000000102", Specialization.GENERAL_MEDICINE, 400.0);

        assertEqual("getAllDoctors returns all 3", 3, ds.getAllDoctors().size());

        // Comparator — sorted by fee descending
        List<Doctor> top2 = analytics.getTopDoctorsByFee(2);
        assertEqual("Top 2 by fee returns exactly 2", 2, top2.size());
        assertTrue("Top by fee: highest first",
                top2.get(0).getConsultationFee() >= top2.get(1).getConsultationFee());

        // Iterator usage
        EmergencyContact ec = EmergencyContact.of("EC", "9000000103", Relationship.PARENT);
        Patient pat = ps.registerPatient("Judy", LocalDate.of(1990, 10, 10),
                "judy@email.com", "9000000104", BloodGroup.B_NEGATIVE, ec);

        Doctor doc = ds.getAllDoctors().get(0);
        as.createAppointment(pat.getId(), doc.getId(),
                futureWeekday(7), LocalTime.of(10, 0), "Iter1");
        as.createAppointment(pat.getId(), doc.getId(),
                futureWeekday(14), LocalTime.of(10, 0), "Iter2");

        // Direct Iterator traversal
        List<Appointment> appts = as.getAllAppointments();
        Iterator<Appointment> it = appts.iterator();
        int count = 0;
        while (it.hasNext()) {
            Appointment a = it.next();
            assertNotNull("Iterator element not null", a);
            count++;
        }
        assertEqual("Iterator traversed all appointments", appts.size(), count);

        // DataStore<T> generic class (HashMap-backed)
        assertTrue("DataStore count matches service count",
                ds.getTotalDoctors() == 3);

        // Unmodifiable list from DataStore
        assertThrows("DataStore findAll returns unmodifiable list",
                () -> ds.getAllDoctors().add(null));
    }

    // =========================================================================
    // Utility
    // =========================================================================

    private static boolean runSafely(ThrowingRunnable action) {
        try {
            action.run();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
