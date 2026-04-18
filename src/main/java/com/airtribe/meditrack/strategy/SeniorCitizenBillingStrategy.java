package com.airtribe.meditrack.strategy;

import com.airtribe.meditrack.constants.Constants;
import com.airtribe.meditrack.entity.Bill;
import com.airtribe.meditrack.interfaces.BillingStrategy;

public class SeniorCitizenBillingStrategy implements BillingStrategy {

    private static final double SENIOR_DISCOUNT = 15.0;

    @Override
    public double calculate(Bill bill) {
        double base = bill.getConsultationFee() + bill.getAdditionalCharges();
        // Apply senior discount on top of any existing discount
        double totalDiscountPercent = Math.min(bill.getDiscountPercent() + SENIOR_DISCOUNT, 100.0);
        double afterDiscount = base - (base * totalDiscountPercent / 100);
        double tax = afterDiscount * Constants.TAX_RATE;
        return afterDiscount + tax;
    }

    @Override
    public String getStrategyName() {
        return String.format("Senior Citizen Billing (%.0f%% discount)", SENIOR_DISCOUNT);
    }
}