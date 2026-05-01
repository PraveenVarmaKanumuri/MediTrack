package com.airtribe.meditrack.util;

import com.airtribe.meditrack.interfaces.DateTimeProvider;

import java.time.LocalDate;
import java.time.Period;
import java.util.concurrent.atomic.AtomicReference;

public final class DateUtil {

    private DateUtil() {}

    private static final AtomicReference<DateTimeProvider> dateTimeProvider =
            new AtomicReference<>(SystemDateTimeProvider.getInstance());

    // Allow overriding — useful for testing
    public static void setDateTimeProvider(DateTimeProvider provider) {
        dateTimeProvider.set(provider);
    }

    public static int calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            throw new IllegalArgumentException("Date of birth cannot be null");
        }
        LocalDate today = dateTimeProvider.get().today();
        if (dateOfBirth.isAfter(today)) {
            throw new IllegalArgumentException("Date of birth cannot be in the future");
        }
        return Period.between(dateOfBirth, today).getYears();
    }

    public static String format(LocalDate date) {
        if (date == null) return "N/A";
        return date.toString();
    }
}