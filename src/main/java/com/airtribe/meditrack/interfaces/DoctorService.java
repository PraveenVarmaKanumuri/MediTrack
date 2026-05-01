package com.airtribe.meditrack.interfaces;

import com.airtribe.meditrack.entity.Doctor;
import com.airtribe.meditrack.entity.enums.Specialization;
import com.airtribe.meditrack.entity.enums.WeekDay;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface DoctorService extends Searchable<Doctor> {

    Doctor addDoctor(String name, LocalDate dateOfBirth, String email,
                     String phone, Specialization specialization, double consultationFee);

    Doctor addDoctorWithSchedule(String name, LocalDate dateOfBirth, String email,
                                 String phone, Specialization specialization,
                                 double consultationFee, Set<WeekDay> workingDays);

    List<Doctor> searchBySpecialization(Specialization specialization);

    List<Doctor> searchByFeeRange(double minFee, double maxFee);

    List<Doctor> searchAvailableOn(LocalDate date);

    List<Doctor> getAllDoctors();

    void importDoctor(Doctor doctor);

    void removeDoctor(String id);

    double getAverageConsultationFee();

    int getTotalDoctors();
}