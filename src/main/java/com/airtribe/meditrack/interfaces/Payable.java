package com.airtribe.meditrack.interfaces;

public interface Payable {

    double calculateTotal();

    void applyDiscount(double discountPercent);

    boolean isPaid();
}