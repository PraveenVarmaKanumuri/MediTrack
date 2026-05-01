
package com.airtribe.meditrack.service;

import com.airtribe.meditrack.entity.Appointment;
import com.airtribe.meditrack.entity.Doctor;
import com.airtribe.meditrack.entity.enums.AppointmentStatus;
import com.airtribe.meditrack.entity.enums.Specialization;
import com.airtribe.meditrack.interfaces.AnalyticsService;
import com.airtribe.meditrack.interfaces.AppointmentService;
import com.airtribe.meditrack.interfaces.DoctorService;
import com.airtribe.meditrack.interfaces.PatientService;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnalyticsServiceImpl implements AnalyticsService {

    private final DoctorService doctorService;
    private final PatientService patientService;
    private final AppointmentService appointmentService;

    public AnalyticsServiceImpl(DoctorService doctorService,
                            PatientService patientService,
                            AppointmentService appointmentService) {
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.appointmentService = appointmentService;
    }

    // Filter doctors by specialization
    public List<Doctor> getDoctorsBySpecialization(Specialization specialization) {
        return doctorService.getAllDoctors().stream()
                .filter(d -> d.getSpecialization() == specialization)
                .collect(Collectors.toList());
    }

    // Average consultation fee across all doctors
    public double getAverageConsultationFee() {
        return doctorService.getAllDoctors().stream()
                .mapToDouble(Doctor::getConsultationFee)
                .average()
                .orElse(0.0);
    }

    // Average consultation fee by specialization
    public Map<Specialization, Double> getAverageFeeBySpecialization() {
        return doctorService.getAllDoctors().stream()
                .collect(Collectors.groupingBy(
                        Doctor::getSpecialization,
                        Collectors.averagingDouble(Doctor::getConsultationFee)
                ));
    }

    // Appointments per doctor — doctorId -> count
    public Map<String, Long> getAppointmentsPerDoctor() {
        return appointmentService.getAllAppointments().stream()
                .collect(Collectors.groupingBy(
                        Appointment::getDoctorId,
                        Collectors.counting()
                ));
    }

    // Appointments per status
    public Map<AppointmentStatus, Long> getAppointmentsByStatus() {
        return appointmentService.getAllAppointments().stream()
                .collect(Collectors.groupingBy(
                        Appointment::getStatus,
                        Collectors.counting()
                ));
    }

    // Top N doctors by consultation fee
    public List<Doctor> getTopDoctorsByFee(int n) {
        return doctorService.getAllDoctors().stream()
                .sorted(Comparator.comparingDouble(Doctor::getConsultationFee).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    // Total revenue from all completed appointments
    public double getTotalRevenue() {
        return appointmentService.getAllAppointments().stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED
                        || a.getStatus() == AppointmentStatus.PAID)
                .mapToDouble(a -> {
                    try {
                        return doctorService.findById(a.getDoctorId()).getConsultationFee();
                    } catch (Exception e) {
                        return 0.0;
                    }
                })
                .sum();
    }

    // Patients with most appointments
    public Map<String, Long> getAppointmentsPerPatient() {
        return appointmentService.getAllAppointments().stream()
                .collect(Collectors.groupingBy(
                        Appointment::getPatientId,
                        Collectors.counting()
                ));
    }

    public void printSummary() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("           MEDITRACK ANALYTICS SUMMARY");
        System.out.println("=".repeat(50));
        System.out.println("Total Doctors    : " + doctorService.getTotalDoctors());
        System.out.println("Total Patients   : " + patientService.getTotalPatients());
        System.out.println("Total Appointments: " + appointmentService.getTotalAppointments());
        System.out.printf("Avg Consult Fee  : %.2f%n", getAverageConsultationFee());

        System.out.println("\nAppointments by Status:");
        getAppointmentsByStatus().forEach((status, count) ->
                System.out.println("   " + status.getDisplayName() + ": " + count));

        System.out.println("\nAppointments per Doctor:");
        getAppointmentsPerDoctor().forEach((doctorId, count) ->
                System.out.println("   Doctor " + doctorId + ": " + count + " appointments"));

        System.out.println("=".repeat(50));
    }

    public List<Doctor> getDoctorsSortedByName() {
        return doctorService.getAllDoctors().stream()
                .sorted(Comparator.comparing(Doctor::getName))
                .collect(Collectors.toList());
    }

    public List<Doctor> getDoctorsSortedByFee() {
        return doctorService.getAllDoctors().stream()
                .sorted(Comparator.comparingDouble(Doctor::getConsultationFee))
                .collect(Collectors.toList());
    }

    public List<Appointment> getAppointmentsSortedByDate() {
        return appointmentService.getAllAppointments().stream()
                .sorted(Comparator.comparing(Appointment::getAppointmentDate))
                .collect(Collectors.toList());
    }

    public void printAllAppointmentsWithIterator() {
        List<Appointment> appointments = appointmentService.getAllAppointments();
        java.util.Iterator<Appointment> iterator = appointments.iterator();
        System.out.println("\n--- All Appointments (via Iterator) ---");
        while (iterator.hasNext()) {
            Appointment a = iterator.next();
            System.out.println(a);
        }
    }
}