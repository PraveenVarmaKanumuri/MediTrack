package com.airtribe.meditrack.util;

import com.airtribe.meditrack.interfaces.DateTimeProvider;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SystemDateTimeProvider implements DateTimeProvider {

    private static final long serialVersionUID = 1L;

    // Singleton — eager initialization
    private static final SystemDateTimeProvider INSTANCE = new SystemDateTimeProvider();

    private SystemDateTimeProvider() {}

    public static SystemDateTimeProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public LocalDate today() {
        return LocalDate.now();
    }

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}