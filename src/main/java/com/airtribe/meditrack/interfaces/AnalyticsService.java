package com.airtribe.meditrack.interfaces;

import com.airtribe.meditrack.entity.Appointment;
import com.airtribe.meditrack.entity.Doctor;
import com.airtribe.meditrack.entity.enums.AppointmentStatus;
import com.airtribe.meditrack.entity.enums.Specialization;

import java.util.List;
import java.util.Map;

public interface AnalyticsService {

    List<Doctor> getDoctorsBySpecialization(Specialization specialization);

    double getAverageConsultationFee();

    Map<Specialization, Double> getAverageFeeBySpecialization();

    Map<String, Long> getAppointmentsPerDoctor();

    Map<AppointmentStatus, Long> getAppointmentsByStatus();

    List<Doctor> getTopDoctorsByFee(int n);

    double getTotalRevenue();

    Map<String, Long> getAppointmentsPerPatient();

    void printSummary();

    List<Doctor> getDoctorsSortedByName();
    List<Doctor> getDoctorsSortedByFee();
    List<Appointment> getAppointmentsSortedByDate();
    void printAllAppointmentsWithIterator();
}