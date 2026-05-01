package com.airtribe.meditrack.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Immutable, serializable snapshot of a generated bill.
 *
 * <p>Created exclusively by {@link Bill#generateSummary()} (package-private constructor).
 * Captures all monetary values at the moment of generation so that subsequent strategy
 * changes on the originating {@link Bill} do not alter the record.
 */
public final class BillSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String billId;
    private final String patientId;
    private final String appointmentId;
    private final double consultationFee;
    private final double additionalCharges;
    private final double discountPercent;
    private final double totalAmount;
    private final String billingStrategy;
    private final boolean paid;
    private final LocalDateTime generatedAt;
    private final double tax;
    // Package-private constructor — only Bill.generateSummary() creates this
    BillSummary(String billId, String patientId, String appointmentId,
                double consultationFee, double additionalCharges, double discountPercent,
                double totalAmount,double tax, String billingStrategy, boolean paid) {
        this.billId = billId;
        this.patientId = patientId;
        this.appointmentId = appointmentId;
        this.consultationFee = consultationFee;
        this.additionalCharges = additionalCharges;
        this.discountPercent = discountPercent;
        this.totalAmount = totalAmount;
        this.billingStrategy = billingStrategy;
        this.paid = paid;
        this.generatedAt = LocalDateTime.now();
        this.tax = tax;
    }

    // Getters only — no setters, no mutation
    public String getBillId() { return billId; }
    public String getPatientId() { return patientId; }
    public String getAppointmentId() { return appointmentId; }
    public double getConsultationFee() { return consultationFee; }
    public double getAdditionalCharges() { return additionalCharges; }
    public double getDiscountPercent() { return discountPercent; }
    public double getTax() { return tax; }
    public double getTotalAmount() { return totalAmount; }
    public String getBillingStrategy() { return billingStrategy; }
    public boolean isPaid() { return paid; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }

    @Override
    public String toString() {
        return String.format(
                "=== Bill Summary ===%n" +
                        "Bill ID       : %s%n" +
                        "Patient ID    : %s%n" +
                        "Appointment ID: %s%n" +
                        "Consultation  : %.2f%n" +
                        "Extra Charges : %.2f%n" +
                        "Discount      : %.0f%%%n" +
                        "Tax           : %.2f%n" +
                        "Total         : %.2f%n" +
                        "Strategy      : %s%n" +
                        "Paid          : %s%n" +
                        "Generated At  : %s",
                billId, patientId, appointmentId,
                consultationFee, additionalCharges, discountPercent,
                tax, totalAmount, billingStrategy,
                paid ? "Yes" : "No", generatedAt);
    }
}