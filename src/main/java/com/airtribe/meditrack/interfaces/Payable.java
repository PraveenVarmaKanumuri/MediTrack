package com.airtribe.meditrack.interfaces;

import com.airtribe.meditrack.entity.BillSummary;

public interface Payable {

    double calculateTotal();

    void applyDiscount(double discountPercent);

    boolean isPaid();

    BillSummary generateBill();

    double calculateTax();

    default String getPaymentSummary() {
        return String.format("Total: %.2f | Tax: %.2f | Paid: %s",
                calculateTotal(), calculateTax(), isPaid() ? "Yes" : "No");
    }


}