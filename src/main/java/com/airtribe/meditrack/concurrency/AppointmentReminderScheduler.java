package com.airtribe.meditrack.concurrency;

import com.airtribe.meditrack.entity.Appointment;
import com.airtribe.meditrack.util.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;

public class AppointmentReminderScheduler {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentReminderScheduler.class);

    private final AtomicInteger scheduledCount = new AtomicInteger(0);
    private final Timer timer;

    public AppointmentReminderScheduler() {
        this.timer = new Timer("AppointmentReminderTimer", true);
    }

    public void scheduleReminder(Appointment appointment, long delayMillis) {
        Validator.requireNonNull(appointment, "appointment");
        timer.schedule(new AppointmentReminderTask(appointment), delayMillis);
        int count = scheduledCount.incrementAndGet();
        logger.info("Reminder scheduled for appointment: {} | Total scheduled: {}",
                appointment.getId(), count);
    }

    public void scheduleImmediateReminder(Appointment appointment) {
        scheduleReminder(appointment, 100);
    }

    public void shutdown() {
        timer.cancel();
        logger.info("Reminder scheduler shut down");
    }

    public int getScheduledCount() {
        return scheduledCount.get();
    }
}
