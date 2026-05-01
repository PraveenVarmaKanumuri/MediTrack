package com.airtribe.meditrack.template;

import com.airtribe.meditrack.entity.Bill;
import com.airtribe.meditrack.entity.BillSummary;

public class StandardBillGeneration extends BillGenerationTemplate {

    @Override
    protected void applyPreProcessing(Bill bill) {
        System.out.println("[Standard] No pre-processing required");
    }

    @Override
    protected void postProcess(BillSummary summary) {
        System.out.println("[Standard] Bill finalized — please collect payment at counter");
        super.postProcess(summary);
    }
}