package com.airtribe.meditrack.util;

import com.airtribe.meditrack.constants.Constants;

public class AppConfig {


    private static AppConfig instance;

    private final String appName;
    private final double taxRate;
    private final int maxAppointmentsPerDay;


    static {
        System.out.println("[AppConfig] Class loaded");
    }

    private AppConfig() {
        this.appName = Constants.APP_NAME;
        this.taxRate = Constants.TAX_RATE;
        this.maxAppointmentsPerDay = Constants.MAX_APPOINTMENTS_PER_DAY;
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    public String getAppName() { return appName; }
    public double getTaxRate() { return taxRate; }
    public int getMaxAppointmentsPerDay() { return maxAppointmentsPerDay; }

    @Override
    public String toString() {
        return String.format("AppConfig[app=%s | tax=%.0f%% | maxAppointments=%d]",
                appName, taxRate * 100, maxAppointmentsPerDay);
    }
}