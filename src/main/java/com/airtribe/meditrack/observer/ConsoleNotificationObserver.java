package com.airtribe.meditrack.observer;

import com.airtribe.meditrack.entity.Appointment;
import com.airtribe.meditrack.interfaces.AppointmentObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleNotificationObserver implements AppointmentObserver {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleNotificationObserver.class);

    @Override
    public void onAppointmentCreated(Appointment appointment) {
        logger.info("NOTIFICATION | Created | Appointment: {} | Patient: {} | Doctor: {} | Date: {} at {} | Reason: {}",
                appointment.getId(), appointment.getPatientId(), appointment.getDoctorId(),
                appointment.getAppointmentDate(), appointment.getAppointmentTime(),
                appointment.getReason());
    }

    @Override
    public void onAppointmentConfirmed(Appointment appointment) {
        logger.info("NOTIFICATION | Confirmed | Appointment: {} | Date: {} at {}",
                appointment.getId(),
                appointment.getAppointmentDate(), appointment.getAppointmentTime());
    }

    @Override
    public void onAppointmentCancelled(Appointment appointment) {
        logger.info("NOTIFICATION | Cancelled | Appointment: {} | Patient: {}",
                appointment.getId(), appointment.getPatientId());
    }

    @Override
    public void onAppointmentCompleted(Appointment appointment) {
        logger.info("NOTIFICATION | Completed | Appointment: {} | Patient: {} | Doctor: {}",
                appointment.getId(), appointment.getPatientId(), appointment.getDoctorId());
    }

    @Override
    public void onAppointmentRescheduled(Appointment appointment) {
        logger.info("NOTIFICATION | Rescheduled | Appointment: {} | New Date: {} at {}",
                appointment.getId(),
                appointment.getAppointmentDate(), appointment.getAppointmentTime());
    }
}
