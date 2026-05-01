package com.airtribe.meditrack.service;

import com.airtribe.meditrack.entity.Doctor;
import com.airtribe.meditrack.entity.enums.Specialization;
import com.airtribe.meditrack.entity.enums.WeekDay;
import com.airtribe.meditrack.exception.InvalidDataException;
import com.airtribe.meditrack.interfaces.DoctorService;
import com.airtribe.meditrack.util.DataStore;
import com.airtribe.meditrack.util.IdGenerator;
import com.airtribe.meditrack.util.Validator;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DoctorServiceImpl implements DoctorService {

    private final DataStore<Doctor> doctorStore;
    private final IdGenerator idGenerator;

    public DoctorServiceImpl() {
        this.doctorStore = new DataStore<>("Doctor");
        this.idGenerator = IdGenerator.getInstance();
    }

    @Override
    public Doctor addDoctor(String name, LocalDate dateOfBirth, String email,
                            String phone, Specialization specialization, double consultationFee) {
        String id = idGenerator.generateDoctorId();
        Doctor doctor = Doctor.create(id, name, dateOfBirth, email, phone,
                specialization, consultationFee);
        doctorStore.save(id, doctor);
        return doctor;
    }

    @Override
    public Doctor addDoctorWithSchedule(String name, LocalDate dateOfBirth, String email,
                                        String phone, Specialization specialization,
                                        double consultationFee, Set<WeekDay> workingDays) {
        String id = idGenerator.generateDoctorId();
        Doctor doctor = Doctor.createWithSchedule(id, name, dateOfBirth, email, phone,
                specialization, consultationFee, workingDays);
        doctorStore.save(id, doctor);
        return doctor;
    }

    @Override
    public Doctor findById(String id) {
        Validator.requireNonBlank(id, "id");
        return doctorStore.findById(id)
                .orElseThrow(() -> new InvalidDataException("id",
                        "No doctor found with id: " + id));
    }

    @Override
    public List<Doctor> findByName(String name) {
        Validator.requireNonBlank(name, "name");
        return doctorStore.findAll().stream()
                .filter(d -> d.getName().toLowerCase()
                        .contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Doctor> findByAgeRange(int minAge, int maxAge) {
        if (minAge < 0 || maxAge < minAge) {
            throw new InvalidDataException("ageRange", "invalid age range");
        }
        return doctorStore.findAll().stream()
                .filter(d -> d.getAge() >= minAge && d.getAge() <= maxAge)
                .collect(Collectors.toList());
    }

    @Override
    public List<Doctor> searchBySpecialization(Specialization specialization) {
        Validator.requireNonNull(specialization, "specialization");
        return doctorStore.findAll().stream()
                .filter(d -> d.getSpecialization() == specialization)
                .collect(Collectors.toList());
    }

    @Override
    public List<Doctor> searchByFeeRange(double minFee, double maxFee) {
        if (minFee < 0 || maxFee < minFee) {
            throw new InvalidDataException("feeRange", "invalid fee range");
        }
        return doctorStore.findAll().stream()
                .filter(d -> d.getConsultationFee() >= minFee
                        && d.getConsultationFee() <= maxFee)
                .collect(Collectors.toList());
    }

    @Override
    public List<Doctor> searchAvailableOn(LocalDate date) {
        Validator.requireNonNull(date, "date");
        return doctorStore.findAll().stream()
                .filter(d -> d.isAvailableOn(date))
                .collect(Collectors.toList());
    }

    @Override
    public List<Doctor> getAllDoctors() {
        return doctorStore.findAll();
    }

    @Override
    public void importDoctor(Doctor doctor) {
        Validator.requireNonNull(doctor, "doctor");
        doctorStore.save(doctor.getId(), doctor);
    }

    @Override
    public void removeDoctor(String id) {
        Validator.requireNonBlank(id, "id");
        doctorStore.delete(id);
    }

    @Override
    public double getAverageConsultationFee() {
        return doctorStore.findAll().stream()
                .mapToDouble(Doctor::getConsultationFee)
                .average()
                .orElse(0.0);
    }

    @Override
    public int getTotalDoctors() {
        return doctorStore.count();
    }
}