package com.airtribe.meditrack.interfaces;

import com.airtribe.meditrack.entity.Appointment;

public interface AppointmentObserver {
    void onAppointmentCreated(Appointment appointment);
    void onAppointmentConfirmed(Appointment appointment);
    void onAppointmentCancelled(Appointment appointment);
    void onAppointmentCompleted(Appointment appointment);
    void onAppointmentRescheduled(Appointment appointment);
}