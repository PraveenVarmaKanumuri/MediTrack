package com.airtribe.meditrack.exception;

public class AppointmentNotFoundException extends RuntimeException {

    private final String appointmentId;

    public  AppointmentNotFoundException(String appointmentId,Throwable cause){
        super("Appointment not found with ID: "+appointmentId, cause);
        this.appointmentId =appointmentId;

    }

    public String getAppointmentId(){
        return  appointmentId;
    }






}
