package com.airtribe.meditrack.util;

import com.airtribe.meditrack.entity.Doctor;
import com.airtribe.meditrack.entity.EmergencyContact;
import com.airtribe.meditrack.entity.Patient;
import com.airtribe.meditrack.entity.enums.BloodGroup;
import com.airtribe.meditrack.entity.enums.Relationship;
import com.airtribe.meditrack.entity.enums.Specialization;
import com.airtribe.meditrack.exception.DataPersistenceException;
import com.airtribe.meditrack.interfaces.DoctorService;
import com.airtribe.meditrack.interfaces.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.airtribe.meditrack.constants.Constants.DOCTORS_FILE;
import static com.airtribe.meditrack.constants.Constants.PATIENTS_FILE;

/**
 * Handles CSV-based save and load of core entities.
 * Exceptions from parsing are chained into DataPersistenceException.
 *
 * doctors.csv  : id,name,dob,email,phone,specialization,consultationFee
 * patients.csv : id,name,dob,email,phone,bloodGroup,ecName,ecPhone,ecRelationship
 */
public final class CSVPersistence {

    private static final Logger logger = LoggerFactory.getLogger(CSVPersistence.class);

    private CSVPersistence() {}

    // ─── Save ─────────────────────────────────────────────────────────────────

    public static void saveDoctors(List<Doctor> doctors) throws DataPersistenceException {
        List<String[]> records = new ArrayList<>();
        for (Doctor d : doctors) {
            records.add(new String[]{
                    d.getId(), d.getName(), d.getDateOfBirth().toString(),
                    d.getEmail(), d.getPhone(),
                    d.getSpecialization().name(),
                    String.valueOf(d.getConsultationFee())
            });
        }
        CSVUtil.write(DOCTORS_FILE, records);
        logger.info("Saved {} doctor(s) to {}", doctors.size(), DOCTORS_FILE);
    }

    public static void savePatients(List<Patient> patients) throws DataPersistenceException {
        List<String[]> records = new ArrayList<>();
        for (Patient p : patients) {
            EmergencyContact ec = p.getEmergencyContact();
            records.add(new String[]{
                    p.getId(), p.getName(), p.getDateOfBirth().toString(),
                    p.getEmail(), p.getPhone(),
                    p.getBloodGroup().name(),
                    ec.getName(), ec.getPhone(), ec.getRelationship().name()
            });
        }
        CSVUtil.write(PATIENTS_FILE, records);
        logger.info("Saved {} patient(s) to {}", patients.size(), PATIENTS_FILE);
    }

    // ─── Load ─────────────────────────────────────────────────────────────────

    public static void loadDoctors(DoctorService doctorService) throws DataPersistenceException {
        List<String[]> records = CSVUtil.read(DOCTORS_FILE);
        int count = 0;
        for (String[] row : records) {
            if (row.length < 7) continue;
            try {
                String id            = row[0].trim();
                String name          = row[1].trim();
                LocalDate dob        = LocalDate.parse(row[2].trim());
                String email         = row[3].trim();
                String phone         = row[4].trim();
                Specialization spec  = Specialization.valueOf(row[5].trim());
                double fee           = Double.parseDouble(row[6].trim());
                doctorService.importDoctor(Doctor.create(id, name, dob, email, phone, spec, fee));
                count++;
            } catch (Exception e) {
                // Chain the root cause so callers know exactly which parse step failed
                throw new DataPersistenceException("loadDoctors",
                        "Failed to parse doctor row: " + String.join(",", row), e);
            }
        }
        logger.info("Loaded {} doctor(s) from {}", count, DOCTORS_FILE);
    }

    public static void loadPatients(PatientService patientService) throws DataPersistenceException {
        List<String[]> records = CSVUtil.read(PATIENTS_FILE);
        int count = 0;
        for (String[] row : records) {
            if (row.length < 9) continue;
            try {
                String id           = row[0].trim();
                String name         = row[1].trim();
                LocalDate dob       = LocalDate.parse(row[2].trim());
                String email        = row[3].trim();
                String phone        = row[4].trim();
                BloodGroup bg       = BloodGroup.valueOf(row[5].trim());
                String ecName       = row[6].trim();
                String ecPhone      = row[7].trim();
                Relationship rel    = Relationship.valueOf(row[8].trim());
                EmergencyContact ec = EmergencyContact.of(ecName, ecPhone, rel);
                patientService.importPatient(
                        Patient.create(id, name, dob, email, phone, bg, ec));
                count++;
            } catch (Exception e) {
                throw new DataPersistenceException("loadPatients",
                        "Failed to parse patient row: " + String.join(",", row), e);
            }
        }
        logger.info("Loaded {} patient(s) from {}", count, PATIENTS_FILE);
    }
}
