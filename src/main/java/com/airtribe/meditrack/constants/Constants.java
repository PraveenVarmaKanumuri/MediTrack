package com.airtribe.meditrack.constants;

public final class Constants {

    // Prevent instantiation — no one should do "new Constants()"
    private Constants() {}

    // Billing
    public static final double TAX_RATE = 0.18;
    public static final double STANDARD_CONSULTATION_FEE = 500.0;

    // Appointment limits
    public static final int MAX_APPOINTMENTS_PER_DAY = 10;

    // File paths (for Bonus A if you attempt it later)
    public static final String DATA_DIR = "data/";
    public static final String PATIENTS_FILE = DATA_DIR + "patients.csv";
    public static final String DOCTORS_FILE = DATA_DIR + "doctors.csv";
    public static final String APPOINTMENTS_FILE = DATA_DIR + "appointments.csv";

    // Display
    public static final String APP_NAME = "MediTrack -- Clinic & Appointment System";
    public static final String SEPARATOR = "=".repeat(50);
}