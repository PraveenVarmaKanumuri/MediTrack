package com.airtribe.meditrack.concurrency;

import com.airtribe.meditrack.entity.Appointment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimerTask;

public class AppointmentReminderTask extends TimerTask {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentReminderTask.class);

    private final Appointment appointment;

    public AppointmentReminderTask(Appointment appointment) {
        this.appointment = appointment;
    }

    @Override
    public void run() {
        logger.info("REMINDER | Appointment: {} | Patient: {} | Doctor: {} | Date: {} at {} | Reason: {}",
                appointment.getId(),
                appointment.getPatientId(),
                appointment.getDoctorId(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                appointment.getReason());
    }
}
