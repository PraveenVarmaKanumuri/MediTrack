package com.airtribe.meditrack.util;

import com.airtribe.meditrack.entity.Bill;
import com.airtribe.meditrack.entity.enums.BillType;
import com.airtribe.meditrack.interfaces.BillingStrategy;
import com.airtribe.meditrack.strategy.EmergencyBillingStrategy;
import com.airtribe.meditrack.strategy.InsuranceBillingStrategy;
import com.airtribe.meditrack.strategy.SeniorCitizenBillingStrategy;
import com.airtribe.meditrack.strategy.StandardBillingStrategy;

public class BillFactory {

    private BillFactory() {}

    public static Bill create(BillType type, String billId, String patientId,
                              String appointmentId, double consultationFee) {
        BillingStrategy strategy = resolveStrategy(type, 0);
        return new Bill.Builder(billId, patientId, appointmentId, consultationFee)
                .billingStrategy(strategy)
                .build();
    }

    public static Bill createInsuranceBill(String billId, String patientId,
                                           String appointmentId, double consultationFee,
                                           double coveragePercent) {
        return new Bill.Builder(billId, patientId, appointmentId, consultationFee)
                .billingStrategy(new InsuranceBillingStrategy(coveragePercent))
                .build();
    }

    public static Bill createWithDiscount(BillType type, String billId, String patientId,
                                          String appointmentId, double consultationFee,
                                          double discountPercent) {
        BillingStrategy strategy = resolveStrategy(type, 0);
        return new Bill.Builder(billId, patientId, appointmentId, consultationFee)
                .billingStrategy(strategy)
                .discountPercent(discountPercent)
                .build();
    }

    private static BillingStrategy resolveStrategy(BillType type, double coveragePercent) {
        return switch (type) {
            case STANDARD -> new StandardBillingStrategy();
            case INSURANCE -> new InsuranceBillingStrategy(coveragePercent);
            case SENIOR_CITIZEN -> new SeniorCitizenBillingStrategy();
            case EMERGENCY -> new EmergencyBillingStrategy();
        };
    }
}