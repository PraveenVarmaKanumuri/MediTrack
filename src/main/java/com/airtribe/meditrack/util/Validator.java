package com.airtribe.meditrack.util;

import com.airtribe.meditrack.exception.InvalidDataException;

import java.time.LocalDate;

public final class Validator {

    private Validator() {}

    public static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidDataException(fieldName, "cannot be null or empty");
        }
    }

    public static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new InvalidDataException(fieldName, "cannot be null");
        }
    }

    public static void requirePositive(double value, String fieldName) {
        if (value <= 0) {
            throw new InvalidDataException(fieldName, "must be greater than zero");
        }
    }

    public static void requireInRange(double value, double min, double max, String fieldName) {
        if (value < min || value > max) {
            throw new InvalidDataException(fieldName,
                    String.format("must be between %.0f and %.0f", min, max));
        }
    }

    public static void requireValidEmail(String email, String fieldName) {
        if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new InvalidDataException(fieldName, "invalid email format");
        }
    }

    public static void requireValidPhone(String phone, String fieldName) {
        if (phone == null || phone.isBlank() || !phone.matches("\\d{10}")) {
            throw new InvalidDataException(fieldName, "must be a 10-digit number");
        }
    }

    public static void requirePastDate(LocalDate date, String fieldName) {
        if (date == null || date.isAfter(LocalDate.now())) {
            throw new InvalidDataException(fieldName, "cannot be null or in the future");
        }
    }

    public static void requireFutureDate(LocalDate date, String fieldName) {
        if (date == null || !date.isAfter(LocalDate.now())) {
            throw new InvalidDataException(fieldName, "must be a future date");
        }
    }
}