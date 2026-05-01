package com.airtribe.meditrack.constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Constants {

    private static final Logger logger = LoggerFactory.getLogger(Constants.class);

    static {
        logger.info("Application constants loaded");
    }

    private Constants() {}

    public static final double TAX_RATE = 0.18;
    public static final double STANDARD_CONSULTATION_FEE = 500.0;
    public static final float TAX_RATE_FLOAT = (float) TAX_RATE;
    public static final long MAX_PATIENTS_LONG = (long) Integer.MAX_VALUE;
    public static final int MAX_APPOINTMENTS_PER_DAY = 10;
    public static final int MAX_AGE = 150;
    public static final byte MIN_AGE = 0; // byte — smallest integer primitive
    public static final short MAX_NAME_LENGTH = 100; // short — 16-bit integer
    public static final String DATA_DIR = "data/";
    public static final String PATIENTS_FILE = DATA_DIR + "patients.csv";
    public static final String DOCTORS_FILE = DATA_DIR + "doctors.csv";
    public static final String APPOINTMENTS_FILE = DATA_DIR + "appointments.csv";
    public static final String APPOINTMENTS_SER_FILE = DATA_DIR + "appointments.ser";
    public static final String APP_NAME = "MediTrack - Clinic & Appointment System";
    public static final String SEPARATOR = "=".repeat(50);
}