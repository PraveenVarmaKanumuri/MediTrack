package com.airtribe.meditrack.entity;

import com.airtribe.meditrack.exception.InvalidDataException;
import com.airtribe.meditrack.interfaces.DateTimeProvider;
import com.airtribe.meditrack.util.SystemDateTimeProvider;

import java.time.LocalDateTime;

public abstract class MedicalEntity {

    private final String id;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final DateTimeProvider dateTimeProvider;

    protected MedicalEntity(String id) {
        this(id, SystemDateTimeProvider.getInstance());
    }

    protected MedicalEntity(String id, DateTimeProvider dateTimeProvider) {
        if (id == null || id.isBlank()) {
            throw new InvalidDataException("id", "cannot be null or empty");
        }
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