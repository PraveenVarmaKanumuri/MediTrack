package com.airtribe.meditrack.strategy;

import com.airtribe.meditrack.entity.Bill;
import com.airtribe.meditrack.interfaces.BillingStrategy;

public class SeniorCitizenBillingStrategy implements BillingStrategy {

    private static final double SENIOR_DISCOUNT = 15.0;

    @Override
    public double calculate(Bill bill) {
        double base = bill.getConsultationFee() + bill.getAdditionalCharges();
        double totalDiscountPercent = Math.min(bill.getDiscountPercent() + SENIOR_DISCOUNT, 100.0);
        return base - (base * totalDiscountPercent / 100);
    }

    @Override
    public String getStrategyName() {
        return String.format("Senior Citizen Billing (%.0f%% discount)", SENIOR_DISCOUNT);
    }
}