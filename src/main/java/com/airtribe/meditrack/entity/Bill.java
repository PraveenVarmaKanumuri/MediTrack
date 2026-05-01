package com.airtribe.meditrack.entity;

import com.airtribe.meditrack.constants.Constants;
import com.airtribe.meditrack.interfaces.BillingStrategy;
import com.airtribe.meditrack.interfaces.Payable;
import com.airtribe.meditrack.strategy.StandardBillingStrategy;
import com.airtribe.meditrack.util.Validator;

/**
 * A bill associated with a completed appointment, built via the inner {@link Builder}.
 *
 * <p>The total amount is calculated by the injected {@link com.airtribe.meditrack.interfaces.BillingStrategy},
 * which can be swapped at runtime via {@link #switchStrategy}. Tax is applied on top of the
 * strategy-calculated subtotal. Call {@link #generateSummary()} to produce an immutable
 * {@link BillSummary} snapshot suitable for display or serialization.
 */
public class Bill extends MedicalEntity implements Payable {

    private final String patientId;
    private final String appointmentId;
    private final double consultationFee;
    private double additionalCharges;
    private double discountPercent;
    private String notes;
    private boolean paid;
    private BillingStrategy billingStrategy;

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

    public static class Builder {
        private final String id;
        private final String patientId;
        private final String appointmentId;
        private final double consultationFee;
        private double additionalCharges = 0.0;
        private double discountPercent = 0.0;
        private String notes = "";
        private BillingStrategy billingStrategy = new StandardBillingStrategy();

        public Builder(String id, String patientId, String appointmentId, double consultationFee) {
            Validator.requireNonBlank(id, "id");
            Validator.requireNonBlank(patientId, "patientId");
            Validator.requireNonBlank(appointmentId, "appointmentId");
            Validator.requirePositive(consultationFee, "consultationFee");
            this.id = id;
            this.patientId = patientId;
            this.appointmentId = appointmentId;
            this.consultationFee = consultationFee;
        }

        public Builder additionalCharges(double additionalCharges) {
            Validator.requirePositive(additionalCharges, "additionalCharges");
            this.additionalCharges = additionalCharges;
            return this;
        }

        public Builder discountPercent(double discountPercent) {
            Validator.requireInRange(discountPercent, 0, 100, "discountPercent");
            this.discountPercent = discountPercent;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes == null ? "" : notes;
            return this;
        }

        public Builder billingStrategy(BillingStrategy billingStrategy) {
            Validator.requireNonNull(billingStrategy, "billingStrategy");
            this.billingStrategy = billingStrategy;
            return this;
        }

        public Bill build() {
            return new Bill(this);
        }
    }

    @Override
    public String getEntityType() { return "Bill"; }

    @Override
    public double calculateTotal() {
        return billingStrategy.calculate(this) + calculateTax();
    }

    @Override
    public double calculateTax() {
        // Call billingStrategy directly (not calculateTotal) to avoid circular recursion
        return billingStrategy.calculate(this) * Constants.TAX_RATE;
    }

    @Override
    public BillSummary generateBill() {
        markAsPaid();
        return generateSummary();
    }

    @Override
    public void applyDiscount(double discountPercent) {
        Validator.requireInRange(discountPercent, 0, 100, "discountPercent");
        this.discountPercent = discountPercent;
        markUpdated();
    }

    @Override
    public boolean isPaid() { return paid; }

    public void markAsPaid() {
        this.paid = true;
        markUpdated();
    }

    public void switchStrategy(BillingStrategy billingStrategy) {
        Validator.requireNonNull(billingStrategy, "billingStrategy");
        this.billingStrategy = billingStrategy;
        markUpdated();
    }

    public void addAdditionalCharges(double amount) {
        Validator.requirePositive(amount, "additionalCharges");
        this.additionalCharges += amount;
        markUpdated();
    }

    public BillSummary generateSummary() {
        return new BillSummary(
                getId(), patientId, appointmentId,
                consultationFee, additionalCharges, discountPercent,
                calculateTotal(), calculateTax(),billingStrategy.getStrategyName(), paid);
    }

    public String getPatientId() { return patientId; }
    public String getAppointmentId() { return appointmentId; }
    public double getConsultationFee() { return consultationFee; }
    public double getAdditionalCharges() { return additionalCharges; }
    public double getDiscountPercent() { return discountPercent; }
    public String getNotes() { return notes; }
    public BillingStrategy getBillingStrategy() { return billingStrategy; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bill that = (Bill) o;
        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        return String.format("Bill[%s] Patient: %s | Total: %.2f | Strategy: %s | Paid: %s",
                getId(), patientId, calculateTotal(),
                billingStrategy.getStrategyName(), paid ? "Yes" : "No");
    }
}