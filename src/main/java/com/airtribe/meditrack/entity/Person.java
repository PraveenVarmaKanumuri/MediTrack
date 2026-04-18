package com.airtribe.meditrack.entity;

import com.airtribe.meditrack.exception.InvalidDataException;
import com.airtribe.meditrack.util.DateUtil;

import java.time.LocalDate;

public abstract class Person extends MedicalEntity {

    private String name;
    private LocalDate dateOfBirth;
    private String email;
    private String phone;

    protected Person(String id, String name, LocalDate dateOfBirth, String email, String phone) {
        super(id);
        if (name == null || name.isBlank()) {
            throw new InvalidDataException("name", "cannot be null or empty");
        }
        if (dateOfBirth == null || dateOfBirth.isAfter(LocalDate.now())) {
            throw new InvalidDataException("dateOfBirth", "cannot be null or in the future");
        }
        if (email == null || !email.contains("@")) {
            throw new InvalidDataException("email", "invalid email format");
        }
        if (phone == null || phone.isBlank()) {
            throw new InvalidDataException("phone", "cannot be null or empty");
        }
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.phone = phone;
    }

    public abstract String getRole();

    @Override
    public String getEntityType() {
        return getRole();
    }

    public void updateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidDataException("name", "cannot be null or empty");
        }
        this.name = name;
        markUpdated();
    }

    public void updateContactInfo(String email, String phone) {
        if (email == null || !email.contains("@")) {
            throw new InvalidDataException("email", "invalid email format");
        }
        if (phone == null || phone.isBlank()) {
            throw new InvalidDataException("phone", "cannot be null or empty");
        }
        this.email = email;
        this.phone = phone;
        markUpdated();
    }

    public void updateDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth == null || dateOfBirth.isAfter(LocalDate.now())) {
            throw new InvalidDataException("dateOfBirth", "cannot be null or in the future");
        }
        this.dateOfBirth = dateOfBirth;
        markUpdated();
    }

    public String getDisplayInfo() {
        return String.format("[%s] %s | DOB: %s | Age: %d | Email: %s | Phone: %s",
                getRole(),
                name,
                DateUtil.format(dateOfBirth),
                DateUtil.calculateAge(dateOfBirth),
                email,
                phone);
    }

    public String getName() { return name; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public int getAge() { return DateUtil.calculateAge(dateOfBirth); }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return getId().equals(person.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        return getDisplayInfo();
    }
}