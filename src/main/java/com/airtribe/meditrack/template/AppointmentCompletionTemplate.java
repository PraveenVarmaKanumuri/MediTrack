package com.airtribe.meditrack.template;

import com.airtribe.meditrack.entity.Appointment;
import com.airtribe.meditrack.entity.Bill;
import com.airtribe.meditrack.interfaces.AppointmentService;
import com.airtribe.meditrack.interfaces.BillingStrategy;

public abstract class AppointmentCompletionTemplate {

    private final AppointmentService appointmentService;

    protected AppointmentCompletionTemplate(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    public final Bill complete(String appointmentId, BillingStrategy strategy) {
        Appointment appointment = validateAppointment(appointmentId);
        prepareNotes(appointment);
        appointmentService.completeAppointment(appointmentId);
        Bill bill = appointmentService.generateBill(appointmentId, strategy);
        postComplete(appointment, bill);
        return bill;
    }


    private Appointment validateAppointment(String appointmentId) {
        Appointment appointment = appointmentService.findById(appointmentId);
        System.out.println("\n[Completing Appointment] " + appointmentId);
        return appointment;
    }

    protected abstract void prepareNotes(Appointment appointment);


    protected void postComplete(Appointment appointment, Bill bill) {
        System.out.println("[Appointment completed] Bill ID: " + bill.getId());
        System.out.printf("[Total Due] %.2f%n", bill.calculateTotal());
    }
}