package com.airtribe.meditrack.template;

import com.airtribe.meditrack.entity.Appointment;
import com.airtribe.meditrack.entity.Bill;
import com.airtribe.meditrack.interfaces.AppointmentService;

public class RegularAppointmentCompletion extends AppointmentCompletionTemplate {

    public RegularAppointmentCompletion(AppointmentService appointmentService) {
        super(appointmentService);
    }

    @Override
    protected void prepareNotes(Appointment appointment) {
        appointment.addNote("Appointment completed — standard consultation");
        System.out.println("[Regular] Notes added to appointment");
    }

    @Override
    protected void postComplete(Appointment appointment, Bill bill) {
        System.out.println("[Regular] Please collect payment at reception");
        super.postComplete(appointment, bill);
    }
}