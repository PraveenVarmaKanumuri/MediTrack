package com.airtribe.meditrack.template;

import com.airtribe.meditrack.entity.Bill;
import com.airtribe.meditrack.entity.BillSummary;

public abstract class BillGenerationTemplate {

    public final BillSummary generate(Bill bill) {
        validateBill(bill);
        applyPreProcessing(bill);
        double total = calculateTotal(bill);
        printBillDetails(bill, total);
        BillSummary summary = finalize(bill);
        postProcess(summary);
        return summary;
    }
    private void validateBill(Bill bill) {
        if (bill == null) {
            throw new IllegalArgumentException("Bill cannot be null");
        }
        if (bill.isPaid()) {
            throw new IllegalStateException("Bill is already paid");
        }
    }

    protected void applyPreProcessing(Bill bill) {
    }

    protected double calculateTotal(Bill bill) {
        return bill.calculateTotal();
    }

    private void printBillDetails(Bill bill, double total) {
        System.out.println("\n--- Bill Details ---");
        System.out.println("Bill ID     : " + bill.getId());
        System.out.println("Patient ID  : " + bill.getPatientId());
        System.out.printf("Total       : %.2f%n", total);
        System.out.printf("Tax         : %.2f%n", bill.calculateTax());
        System.out.println("Strategy    : " + bill.getBillingStrategy().getStrategyName());
    }

    private BillSummary finalize(Bill bill) {
        return bill.generateBill();
    }

    protected void postProcess(BillSummary summary) {
        System.out.println("\n[Bill generated successfully]");
        System.out.println(summary);
    }
}