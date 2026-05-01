package com.airtribe.meditrack.util;

import com.airtribe.meditrack.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lazy singleton holding application-wide configuration values.
 *
 * <p>Uses double-checked locking: the outer {@code null} check avoids acquiring the
 * class-level lock on every call after initialization; the inner check inside the
 * {@code synchronized} block prevents two threads from creating separate instances
 * under a race condition. The {@code volatile} keyword ensures the reference is
 * published safely across CPU caches (no partial initialization visible to other threads).
 */
public class AppConfig {

    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    // Lazy singleton — instance created only on first call, not at class load
    private static volatile AppConfig instance;

    private final String appName;
    private final double taxRate;
    private final int maxAppointmentsPerDay;

    private AppConfig() {
        this.appName = Constants.APP_NAME;
        this.taxRate = Constants.TAX_RATE;
        this.maxAppointmentsPerDay = Constants.MAX_APPOINTMENTS_PER_DAY;
    }

    // Double-checked locking: outer check avoids lock overhead once initialized;
    // inner check inside synchronized block prevents duplicate creation under race.
    public static AppConfig getInstance() {
        if (instance == null) {
            synchronized (AppConfig.class) {
                if (instance == null) {
                    instance = new AppConfig();
                    logger.info("AppConfig lazy singleton initialized");
                }
            }
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
