package com.hostelhub.service;

/**
 * Standard checkout at end of semester:
 * 100% of security deposit refunded, no rent refund as semester is completed.
 */
public class StandardRefundPolicy implements RefundPolicy {
    @Override
    public double calculateRefund(double totalPaidFee, double securityDeposit, int monthsUsed, int totalSemesterMonths) {
        return securityDeposit;
    }

    @Override
    public String getPolicyName() {
        return "Standard End-of-Semester Refund (100% Deposit Refunded)";
    }
}
