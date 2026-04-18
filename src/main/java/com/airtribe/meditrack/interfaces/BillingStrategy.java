package com.airtribe.meditrack.interfaces;

import com.airtribe.meditrack.entity.Bill;

public interface BillingStrategy {
    double calculate(Bill bill);
    String getStrategyName();
}