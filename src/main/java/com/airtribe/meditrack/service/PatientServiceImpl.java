package com.airtribe.meditrack.service;

import com.airtribe.meditrack.entity.EmergencyContact;
import com.airtribe.meditrack.entity.Patient;
import com.airtribe.meditrack.entity.enums.BloodGroup;
import com.airtribe.meditrack.exception.InvalidDataException;
import com.airtribe.meditrack.interfaces.PatientService;
import com.airtribe.meditrack.util.DataStore;
import com.airtribe.meditrack.util.IdGenerator;
import com.airtribe.meditrack.util.Validator;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class PatientServiceImpl implements PatientService {

    private final DataStore<Patient> patientStore;
    private final IdGenerator idGenerator;

    public PatientServiceImpl() {
        this.patientStore = new DataStore<>("Patient");
        this.idGenerator = IdGenerator.getInstance();
    }

    @Override
    public Patient registerPatient(String name, LocalDate dateOfBirth, String email,
                                   String phone, BloodGroup bloodGroup,
                                   EmergencyContact emergencyContact) {
        String id = idGenerator.generatePatientId();
        Patient patient = Patient.create(id, name, dateOfBirth, email, phone,
                bloodGroup, emergencyContact);
        patientStore.save(id, patient);
        return patient;
    }

    @Override
    public Patient findById(String id) {
        Validator.requireNonBlank(id, "id");
        return patientStore.findById(id)
                .orElseThrow(() -> new InvalidDataException("id",
                        "No patient found with id: " + id));
    }

    @Override
    public List<Patient> findByName(String name) {
        Validator.requireNonBlank(name, "name");
        return patientStore.findAll().stream()
                .filter(p -> p.getName().toLowerCase()
                        .contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Patient> findByAgeRange(int minAge, int maxAge) {
        if (minAge < 0 || maxAge < minAge) {
            throw new InvalidDataException("ageRange", "invalid age range");
        }
        return patientStore.findAll().stream()
                .filter(p -> p.getAge() >= minAge && p.getAge() <= maxAge)
                .collect(Collectors.toList());
    }

    @Override
    public Patient searchPatient(String id) {
        return findById(id);
    }

    @Override
    public List<Patient> searchPatient(String name, boolean byName) {
        return findByName(name);
    }

    @Override
    public List<Patient> searchPatient(int minAge, int maxAge) {
        return findByAgeRange(minAge, maxAge);
    }

    @Override
    public List<Patient> searchByBloodGroup(BloodGroup bloodGroup) {
        Validator.requireNonNull(bloodGroup, "bloodGroup");
        return patientStore.findAll().stream()
                .filter(p -> p.getBloodGroup() == bloodGroup)
                .collect(Collectors.toList());
    }

    @Override
    public List<Patient> getAllPatients() {
        return patientStore.findAll();
    }

    @Override
    public void importPatient(Patient patient) {
        Validator.requireNonNull(patient, "patient");
        patientStore.save(patient.getId(), patient);
    }

    @Override
    public void removePatient(String id) {
        Validator.requireNonBlank(id, "id");
        patientStore.delete(id);
    }

    @Override
    public void addMedicalHistory(String patientId, String entry) {
        Patient patient = findById(patientId);
        patient.addMedicalHistory(entry);
    }

    @Override
    public void updateEmergencyContact(String patientId, EmergencyContact emergencyContact) {
        Patient patient = findById(patientId);
        patient.updateEmergencyContact(emergencyContact);
    }

    @Override
    public Patient getPatientCopy(String patientId, boolean deep) {
        Patient patient = findById(patientId);
        return deep ? patient.deepClone() : patient.clone();
    }

    @Override
    public int getTotalPatients() {
        return patientStore.count();
    }
}