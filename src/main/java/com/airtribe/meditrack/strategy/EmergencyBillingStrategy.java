package com.airtribe.meditrack.strategy;

import com.airtribe.meditrack.entity.Bill;
import com.airtribe.meditrack.interfaces.BillingStrategy;

public class EmergencyBillingStrategy implements BillingStrategy {

    private static final double EMERGENCY_SURCHARGE = 25.0;

    @Override
    public double calculate(Bill bill) {
        double base = bill.getConsultationFee() + bill.getAdditionalCharges();
        double withSurcharge = base + (base * EMERGENCY_SURCHARGE / 100);
        return withSurcharge - (withSurcharge * bill.getDiscountPercent() / 100);
    }
    @Override
    public String getStrategyName() {
        return String.format("Emergency Billing (%.0f%% surcharge)", EMERGENCY_SURCHARGE);
    }
}