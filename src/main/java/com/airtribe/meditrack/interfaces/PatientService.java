package com.airtribe.meditrack.interfaces;

import com.airtribe.meditrack.entity.EmergencyContact;
import com.airtribe.meditrack.entity.Patient;
import com.airtribe.meditrack.entity.enums.BloodGroup;

import java.time.LocalDate;
import java.util.List;

public interface PatientService extends Searchable<Patient> {

    Patient registerPatient(String name, LocalDate dateOfBirth, String email,
                            String phone, BloodGroup bloodGroup,
                            EmergencyContact emergencyContact);

    Patient searchPatient(String id);

    List<Patient> searchPatient(String name, boolean byName);

    List<Patient> searchPatient(int minAge, int maxAge);

    List<Patient> searchByBloodGroup(BloodGroup bloodGroup);

    List<Patient> getAllPatients();

    void importPatient(Patient patient);

    void removePatient(String id);

    void addMedicalHistory(String patientId, String entry);

    void updateEmergencyContact(String patientId, EmergencyContact emergencyContact);

    Patient getPatientCopy(String patientId, boolean deep);

    int getTotalPatients();
}