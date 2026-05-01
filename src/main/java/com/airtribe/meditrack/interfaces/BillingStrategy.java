package com.airtribe.meditrack.interfaces;

import com.airtribe.meditrack.entity.Bill;

import java.io.Serializable;

/**
 * Strategy interface for bill amount calculation.
 *
 * <p>Implementations (Standard, SeniorCitizen, Insurance, Emergency) encapsulate
 * pricing rules. The interface extends {@link java.io.Serializable} so that a
 * {@link com.airtribe.meditrack.entity.Bill} carrying a strategy can be serialized.
 */
public interface BillingStrategy extends Serializable {
    double calculate(Bill bill);
    String getStrategyName();
}