package com.airtribe.meditrack.strategy;

import com.airtribe.meditrack.entity.Bill;
import com.airtribe.meditrack.interfaces.BillingStrategy;

public class InsuranceBillingStrategy implements BillingStrategy {

    private final double coveragePercent;

    public InsuranceBillingStrategy(double coveragePercent) {
        this.coveragePercent = coveragePercent;
    }

    @Override
    public double calculate(Bill bill) {
        double base = bill.getConsultationFee() + bill.getAdditionalCharges();
        double afterDiscount = base - (base * bill.getDiscountPercent() / 100);
        return afterDiscount - (afterDiscount * coveragePercent / 100);
    }

    @Override
    public String getStrategyName() {
        return String.format("Insurance Billing (%.0f%% coverage)", coveragePercent);
    }
}