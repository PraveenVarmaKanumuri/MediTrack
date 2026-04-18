package com.airtribe.meditrack.entity;

import com.airtribe.meditrack.exception.InvalidDataException;
import com.airtribe.meditrack.interfaces.BillingStrategy;
import com.airtribe.meditrack.interfaces.Payable;
import com.airtribe.meditrack.strategy.StandardBillingStrategy;

public class Bill extends MedicalEntity implements Payable {

    private final String patientId;
    private final String appointmentId;
    private final double consultationFee;
    private double additionalCharges;
    private double discountPercent;
    private String notes;
    private boolean paid;
    private BillingStrategy billingStrategy;

    // Private constructor — only Builder creates Bill
    private Bill(Builder builder) {
        super(builder.id);
        this.patientId = builder.patientId;
        this.appointmentId = builder.appointmentId;
        this.consultationFee = builder.consultationFee;
        this.additionalCharges = builder.additionalCharges;
        this.discountPercent = builder.discountPercent;
        this.notes = builder.notes;
        this.paid = false;
        this.billingStrategy = builder.billingStrategy;
    }

    // Builder
    public static class Builder {
        // Required fields
        private final String id;
        private final String patientId;
        private final String appointmentId;
        private final double consultationFee;

        // Optional fields with defaults
        private double additionalCharges = 0.0;
        private double discountPercent = 0.0;
        private String notes = "";
        private BillingStrategy billingStrategy = new StandardBillingStrategy();

        public Builder(String id, String patientId, String appointmentId, double consultationFee) {
            if (id == null || id.isBlank()) {
                throw new InvalidDataException("id", "cannot be null or empty");
            }
            if (patientId == null || patientId.isBlank()) {
                throw new InvalidDataException("patientId", "cannot be null or empty");
            }
            if (appointmentId == null || appointmentId.isBlank()) {
                throw new InvalidDataException("appointmentId", "cannot be null or empty");
            }
            if (consultationFee < 0) {
                throw new InvalidDataException("consultationFee", "cannot be negative");
            }
            this.id = id;
            this.patientId = patientId;
            this.appointmentId = appointmentId;
            this.consultationFee = consultationFee;
        }

        public Builder additionalCharges(double additionalCharges) {
            if (additionalCharges < 0) {
                throw new InvalidDataException("additionalCharges", "cannot be negative");
            }
            this.additionalCharges = additionalCharges;
            return this;
        }

        public Builder discountPercent(double discountPercent) {
            if (discountPercent < 0 || discountPercent > 100) {
                throw new InvalidDataException("discountPercent", "must be between 0 and 100");
            }
            this.discountPercent = discountPercent;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes == null ? "" : notes;
            return this;
        }

        public Builder billingStrategy(BillingStrategy billingStrategy) {
            if (billingStrategy == null) {
                throw new InvalidDataException("billingStrategy", "cannot be null");
            }
            this.billingStrategy = billingStrategy;
            return this;
        }

        public Bill build() {
            return new Bill(this);
        }
    }

    @Override
    public String getEntityType() { return "Bill"; }

    // Payable implementation
    @Override
    public double calculateTotal() {
        return billingStrategy.calculate(this);
    }

    @Override
    public void applyDiscount(double discountPercent) {
        if (discountPercent < 0 || discountPercent > 100) {
            throw new InvalidDataException("discountPercent", "must be between 0 and 100");
        }
        this.discountPercent = discountPercent;
        markUpdated();
    }

    @Override
    public boolean isPaid() { return paid; }

    // Domain methods
    public void markAsPaid() {
        this.paid = true;
        markUpdated();
    }

    public void switchStrategy(BillingStrategy billingStrategy) {
        if (billingStrategy == null) {
            throw new InvalidDataException("billingStrategy", "cannot be null");
        }
        this.billingStrategy = billingStrategy;
        markUpdated();
    }

    public void addAdditionalCharges(double amount) {
        if (amount < 0) {
            throw new InvalidDataException("additionalCharges", "cannot be negative");
        }
        this.additionalCharges += amount;
        markUpdated();
    }

    // Generate immutable summary — call this when finalizing payment
    public BillSummary generateSummary() {
        return new BillSummary(
                getId(),
                patientId,
                appointmentId,
                consultationFee,
                additionalCharges,
                discountPercent,
                calculateTotal(),
                billingStrategy.getStrategyName(),
                paid
        );
    }

    // Getters — needed by BillingStrategy
    public String getPatientId() { return patientId; }
    public String getAppointmentId() { return appointmentId; }
    public double getConsultationFee() { return consultationFee; }
    public double getAdditionalCharges() { return additionalCharges; }
    public double getDiscountPercent() { return discountPercent; }
    public String getNotes() { return notes; }
    public BillingStrategy getBillingStrategy() { return billingStrategy; }

    @Override
    public String toString() {
        return String.format("Bill[%s] Patient: %s | Total: %.2f | Strategy: %s | Paid: %s",
                getId(), patientId, calculateTotal(),
                billingStrategy.getStrategyName(), paid ? "Yes" : "No");
    }
}