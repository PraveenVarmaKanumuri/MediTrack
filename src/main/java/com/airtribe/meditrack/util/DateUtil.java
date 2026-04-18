package com.airtribe.meditrack.util;

import com.airtribe.meditrack.interfaces.DateTimeProvider;

import java.time.LocalDate;
import java.time.Period;

public final class DateUtil {

    private DateUtil() {}

    private static DateTimeProvider dateTimeProvider = SystemDateTimeProvider.getInstance();

    // Allow overriding — useful for testing
    public static void setDateTimeProvider(DateTimeProvider provider) {
        dateTimeProvider = provider;
    }

    public static int calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            throw new IllegalArgumentException("Date of birth cannot be null");
        }
        if (dateOfBirth.isAfter(dateTimeProvider.today())) {
            throw new IllegalArgumentException("Date of birth cannot be in the future");
        }
        return Period.between(dateOfBirth, dateTimeProvider.today()).getYears();
    }

    public static String format(LocalDate date) {
        if (date == null) return "N/A";
        return date.toString();
    }
}