package com.airtribe.meditrack.entity;

import com.airtribe.meditrack.exception.InvalidDataException;
import com.airtribe.meditrack.util.DateUtil;
import com.airtribe.meditrack.util.Validator;

import java.time.LocalDate;

/**
 * Abstract person entity with personal details shared by {@link Doctor} and {@link Patient}.
 *
 * <p>Equality is keyed on {@code id} so that the same person object is equal across
 * collection lookups regardless of mutable field changes.
 */
public abstract class Person extends MedicalEntity {

    private String name;
    private LocalDate dateOfBirth;
    private String email;
    private String phone;

    protected Person(String id, String name, LocalDate dateOfBirth, String email, String phone) {
        super(id);
        Validator.requireNonBlank(name, "name");
        Validator.requirePastDate(dateOfBirth, "dateOfBirth");
        Validator.requireValidEmail(email, "email");
        Validator.requireNonBlank(phone, "phone");
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
        Validator.requireNonBlank(name, "name");
        this.name = name;
        markUpdated();
    }

    public void updateContactInfo(String email, String phone) {
        Validator.requireValidEmail(email, "email");
        Validator.requireNonBlank(phone, "phone");
        this.email = email;
        this.phone = phone;
        markUpdated();
    }

    public void updateDateOfBirth(LocalDate dateOfBirth) {
        Validator.requirePastDate(dateOfBirth, "dateOfBirth");
        this.dateOfBirth = dateOfBirth;
        markUpdated();
    }

    public String getDisplayInfo() {
        return String.format("[%s] ID: %s | %s | DOB: %s | Age: %d | Email: %s | Phone: %s",
                getRole(),
                getId(),
                getName(),
                DateUtil.format(getDateOfBirth()),
                getAge(),
                getEmail(),
                getPhone());
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