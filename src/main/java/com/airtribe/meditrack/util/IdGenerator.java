package com.airtribe.meditrack.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Eager singleton that generates sequential, prefixed entity IDs.
 *
 * <p>The single instance is created in a {@code static} initializer block, which the
 * JVM guarantees runs exactly once under the class-loading lock — making this
 * approach inherently thread-safe without explicit synchronization.
 * Counters use {@link java.util.concurrent.atomic.AtomicInteger} for lock-free
 * thread-safety when IDs are generated concurrently.
 */
public class IdGenerator {

    private static final Logger logger = LoggerFactory.getLogger(IdGenerator.class);
    private static final IdGenerator INSTANCE;

    static {
        INSTANCE = new IdGenerator();
        logger.info("Singleton initialized (eager static block)");
    }

    private final AtomicInteger patientCounter;
    private final AtomicInteger doctorCounter;
    private final AtomicInteger appointmentCounter;
    private final AtomicInteger billCounter;

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