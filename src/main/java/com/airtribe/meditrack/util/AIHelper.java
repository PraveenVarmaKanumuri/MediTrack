package com.airtribe.meditrack.util;

import com.airtribe.meditrack.entity.Appointment;
import com.airtribe.meditrack.entity.Doctor;
import com.airtribe.meditrack.entity.enums.AppointmentStatus;
import com.airtribe.meditrack.entity.enums.Specialization;
import com.airtribe.meditrack.interfaces.AppointmentService;
import com.airtribe.meditrack.interfaces.DoctorService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AIHelper {

    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    private static final Map<String, Specialization> SYMPTOM_MAP = new HashMap<>();

    static {
        // Cardiology
        SYMPTOM_MAP.put("chest pain", Specialization.CARDIOLOGY);
        SYMPTOM_MAP.put("heart palpitations", Specialization.CARDIOLOGY);
        SYMPTOM_MAP.put("shortness of breath", Specialization.CARDIOLOGY);
        SYMPTOM_MAP.put("high blood pressure", Specialization.CARDIOLOGY);

        // Neurology
        SYMPTOM_MAP.put("headache", Specialization.NEUROLOGY);
        SYMPTOM_MAP.put("migraine", Specialization.NEUROLOGY);
        SYMPTOM_MAP.put("dizziness", Specialization.NEUROLOGY);
        SYMPTOM_MAP.put("seizure", Specialization.NEUROLOGY);
        SYMPTOM_MAP.put("memory loss", Specialization.NEUROLOGY);

        // Orthopedics
        SYMPTOM_MAP.put("joint pain", Specialization.ORTHOPEDICS);
        SYMPTOM_MAP.put("back pain", Specialization.ORTHOPEDICS);
        SYMPTOM_MAP.put("fracture", Specialization.ORTHOPEDICS);
        SYMPTOM_MAP.put("knee pain", Specialization.ORTHOPEDICS);

        // Pediatrics
        SYMPTOM_MAP.put("child fever", Specialization.PEDIATRICS);
        SYMPTOM_MAP.put("child cough", Specialization.PEDIATRICS);
        SYMPTOM_MAP.put("child rash", Specialization.PEDIATRICS);

        // Dermatology
        SYMPTOM_MAP.put("skin rash", Specialization.DERMATOLOGY);
        SYMPTOM_MAP.put("acne", Specialization.DERMATOLOGY);
        SYMPTOM_MAP.put("eczema", Specialization.DERMATOLOGY);
        SYMPTOM_MAP.put("hair loss", Specialization.DERMATOLOGY);

        // General Medicine
        SYMPTOM_MAP.put("fever", Specialization.GENERAL_MEDICINE);
        SYMPTOM_MAP.put("cold", Specialization.GENERAL_MEDICINE);
        SYMPTOM_MAP.put("cough", Specialization.GENERAL_MEDICINE);
        SYMPTOM_MAP.put("fatigue", Specialization.GENERAL_MEDICINE);
        SYMPTOM_MAP.put("diabetes", Specialization.GENERAL_MEDICINE);

        // Psychiatry
        SYMPTOM_MAP.put("anxiety", Specialization.PSYCHIATRY);
        SYMPTOM_MAP.put("depression", Specialization.PSYCHIATRY);
        SYMPTOM_MAP.put("insomnia", Specialization.PSYCHIATRY);
        SYMPTOM_MAP.put("stress", Specialization.PSYCHIATRY);

        // Oncology
        SYMPTOM_MAP.put("tumor", Specialization.ONCOLOGY);
        SYMPTOM_MAP.put("cancer", Specialization.ONCOLOGY);
        SYMPTOM_MAP.put("lump", Specialization.ONCOLOGY);
    }

    public AIHelper(DoctorService doctorService, AppointmentService appointmentService) {
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
    }

    public List<Doctor> recommendDoctors(String symptoms) {
        if (symptoms == null || symptoms.isBlank()) {
            return new ArrayList<>();
        }
        Specialization matched = detectSpecialization(symptoms);
        System.out.println("[AI] Recommended specialization: " + matched.getDisplayName());
        return doctorService.searchBySpecialization(matched);
    }


    public Specialization detectSpecialization(String symptoms) {
        if (symptoms == null || symptoms.isBlank()) {
            return Specialization.GENERAL_MEDICINE;
        }
        String lower = symptoms.toLowerCase();
        return SYMPTOM_MAP.entrySet().stream()
                .filter(e -> lower.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(Specialization.GENERAL_MEDICINE);
    }

    public List<LocalTime> suggestAppointmentSlots(String doctorId, LocalDate date) {
        List<LocalTime> allSlots = generateSlots();

        // Get already booked slots for this doctor on this date
        List<LocalTime> bookedSlots = appointmentService.findByDoctorId(doctorId)
                .stream()
                .filter(a -> a.getAppointmentDate().equals(date)
                        && a.getStatus() != AppointmentStatus.CANCELLED)
                .map(Appointment::getAppointmentTime)
                .collect(Collectors.toList());


        List<LocalTime> availableSlots = allSlots.stream()
                .filter(slot -> !bookedSlots.contains(slot))
                .collect(Collectors.toList());

        System.out.println("[AI] Available slots for Doctor " + doctorId + " on " + date + ":");
        if (availableSlots.isEmpty()) {
            System.out.println("   No slots available");
        } else {
            availableSlots.forEach(slot -> System.out.println("   " + slot));
        }

        return availableSlots;
    }

    private List<LocalTime> generateSlots() {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(17, 0);
        while (start.isBefore(end)) {
            slots.add(start);
            start = start.plusMinutes(30);
        }
        return slots;
    }
}