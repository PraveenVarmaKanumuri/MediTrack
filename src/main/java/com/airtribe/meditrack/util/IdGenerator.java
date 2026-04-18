package com.airtribe.meditrack.util;

import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {


    private static final IdGenerator INSTANCE = new IdGenerator();

    private final AtomicInteger patientCounter;
    private final AtomicInteger doctorCounter;
    private final AtomicInteger appointmentCounter;
    private final AtomicInteger billCounter;

    static {
        System.out.println("[IdGenerator] Initialized via static block on class load");
    }

    private IdGenerator() {
        patientCounter = new AtomicInteger(0);
        doctorCounter = new AtomicInteger(0);
        appointmentCounter = new AtomicInteger(0);
        billCounter = new AtomicInteger(0);
    }

    // Eager Singleton — instance already exists, just return it
    public static IdGenerator getInstance() {
        return INSTANCE;
    }

    // ID generation methods
    public String generatePatientId() {
        return String.format("P%03d", patientCounter.incrementAndGet());
    }

    public String generateDoctorId() {
        return String.format("D%03d", doctorCounter.incrementAndGet());
    }

    public String generateAppointmentId() {
        return String.format("A%03d", appointmentCounter.incrementAndGet());
    }

    public String generateBillId() {
        return String.format("B%03d", billCounter.incrementAndGet());
    }
}