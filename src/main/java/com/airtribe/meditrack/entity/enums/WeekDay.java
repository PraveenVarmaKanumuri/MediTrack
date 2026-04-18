package com.airtribe.meditrack.entity.enums;

import java.time.DayOfWeek;

public enum WeekDay {
    MONDAY("Monday", DayOfWeek.MONDAY),
    TUESDAY("Tuesday", DayOfWeek.TUESDAY),
    WEDNESDAY("Wednesday", DayOfWeek.WEDNESDAY),
    THURSDAY("Thursday", DayOfWeek.THURSDAY),
    FRIDAY("Friday", DayOfWeek.FRIDAY),
    SATURDAY("Saturday", DayOfWeek.SATURDAY),
    SUNDAY("Sunday", DayOfWeek.SUNDAY);

    private final String displayName;
    private final DayOfWeek dayOfWeek;

    WeekDay(String displayName, DayOfWeek dayOfWeek) {
        this.displayName = displayName;
        this.dayOfWeek = dayOfWeek;
    }

    public String getDisplayName() { return displayName; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }

    // Convert Java's DayOfWeek to our WeekDay enum
    public static WeekDay from(DayOfWeek dayOfWeek) {
        for (WeekDay wd : values()) {
            if (wd.dayOfWeek == dayOfWeek) return wd;
        }
        throw new IllegalArgumentException("Unknown day: " + dayOfWeek);
    }

    @Override
    public String toString() { return displayName; }
}