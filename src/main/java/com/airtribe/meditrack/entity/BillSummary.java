package com.airtribe.meditrack.entity;

import java.time.LocalDateTime;

public final class BillSummary {

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

    // Package-private constructor — only Bill.generateSummary() creates this
    BillSummary(String billId, String patientId, String appointmentId,
                double consultationFee, double additionalCharges, double discountPercent,
                double totalAmount, String billingStrategy, boolean paid) {
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
    }

    // Getters only — no setters, no mutation
    public String getBillId() { return billId; }
    public String getPatientId() { return patientId; }
    public String getAppointmentId() { return appointmentId; }
    public double getConsultationFee() { return consultationFee; }
    public double getAdditionalCharges() { return additionalCharges; }
    public double getDiscountPercent() { return discountPercent; }
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
                        "Total         : %.2f%n" +
                        "Strategy      : %s%n" +
                        "Paid          : %s%n" +
                        "Generated At  : %s",
                billId, patientId, appointmentId,
                consultationFee, additionalCharges, discountPercent,
                totalAmount, billingStrategy,
                paid ? "Yes" : "No", generatedAt);
    }
}