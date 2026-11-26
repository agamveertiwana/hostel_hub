package com.hostelhub.service;

/**
 * Strategy interface for calculating refund upon hostel check-out or cancellation.
 * Demonstrates the Strategy Design Pattern and Interface Abstraction.
 */
public interface RefundPolicy {
    double calculateRefund(double totalPaidFee, double securityDeposit, int monthsUsed, int totalSemesterMonths);
    String getPolicyName();
}
