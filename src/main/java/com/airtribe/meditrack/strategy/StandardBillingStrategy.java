package com.airtribe.meditrack.strategy;

import com.airtribe.meditrack.constants.Constants;
import com.airtribe.meditrack.entity.Bill;
import com.airtribe.meditrack.interfaces.BillingStrategy;

public class StandardBillingStrategy implements BillingStrategy {

    @Override
    public double calculate(Bill bill) {
        double base = bill.getConsultationFee() + bill.getAdditionalCharges();
        return base - (base * bill.getDiscountPercent() / 100);
    }

    @Override
    public String getStrategyName() { return "Standard Billing"; }
}