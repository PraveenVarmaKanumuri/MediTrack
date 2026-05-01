package com.airtribe.meditrack.entity;

import com.airtribe.meditrack.exception.InvalidDataException;
import com.airtribe.meditrack.interfaces.DateTimeProvider;
import com.airtribe.meditrack.util.SystemDateTimeProvider;
import com.airtribe.meditrack.util.Validator;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base class for all domain entities in MediTrack.
 *
 * <p>Provides a universally unique {@code id}, creation/update audit timestamps via
 * an injected {@link com.airtribe.meditrack.interfaces.DateTimeProvider}, and Java
 * serialization support. Subclasses must implement {@link #getEntityType()}.
 */
public abstract class MedicalEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final DateTimeProvider dateTimeProvider;

    protected MedicalEntity(String id) {
        this(id, SystemDateTimeProvider.getInstance());
    }

    protected MedicalEntity(String id, DateTimeProvider dateTimeProvider) {
        Validator.requireNonBlank(id, "id");
        Validator.requireNonNull(dateTimeProvider, "dateTimeProvider");
        this.id = id;
        this.dateTimeProvider = dateTimeProvider;
        this.createdAt = dateTimeProvider.now();
        this.updatedAt = dateTimeProvider.now();
    }

    public abstract String getEntityType();

    protected void markUpdated() {
        this.updatedAt = dateTimeProvider.now();
    }

    public String getId() { return id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public String getAuditInfo() {
        return String.format("Created: %s | Last Updated: %s", createdAt, updatedAt);
    }
}