package com.airtribe.meditrack.template;

import com.airtribe.meditrack.entity.Appointment;
import com.airtribe.meditrack.entity.Bill;
import com.airtribe.meditrack.interfaces.AppointmentService;

public class EmergencyAppointmentCompletion extends AppointmentCompletionTemplate {

    public EmergencyAppointmentCompletion(AppointmentService appointmentService) {
        super(appointmentService);
    }

    @Override
    protected void prepareNotes(Appointment appointment) {
        appointment.addNote("Emergency consultation completed — priority billing applies");
        System.out.println("[Emergency] Priority notes added");
    }

    @Override
    protected void postComplete(Appointment appointment, Bill bill) {
        System.out.println("[Emergency] Urgent billing processed — insurance team notified");
        super.postComplete(appointment, bill);
    }
}