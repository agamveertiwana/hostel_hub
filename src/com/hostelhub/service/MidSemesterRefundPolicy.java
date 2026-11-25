package com.hostelhub.service;

/**
 * Mid-semester vacating refund:
 * Prorates unused months of rent, deducts a 15% cancellation penalty on remaining rent,
 * and refunds the security deposit.
 */
public class MidSemesterRefundPolicy implements RefundPolicy {
    private double cancellationPenaltyRate; // e.g. 0.15 (15%)

    public MidSemesterRefundPolicy() {
        this.cancellationPenaltyRate = 0.15;
    }

    public MidSemesterRefundPolicy(double penaltyRate) {
        this.cancellationPenaltyRate = penaltyRate;
    }

    @Override
    public double calculateRefund(double totalPaidFee, double securityDeposit, int monthsUsed, int totalSemesterMonths) {
        if (totalSemesterMonths <= 0) return 0.0;

        double rentPortion = Math.max(0, totalPaidFee - securityDeposit);
        double monthlyRent = rentPortion / totalSemesterMonths;
        int remainingMonths = Math.max(0, totalSemesterMonths - monthsUsed);

        double unusedRent = remainingMonths * monthlyRent;
        double penalty = unusedRent * cancellationPenaltyRate;
        double refundableRent = Math.max(0, unusedRent - penalty);

        return refundableRent + securityDeposit;
    }

    @Override
    public String getPolicyName() {
        return "Mid-Semester Prorated Refund (Remaining Rent minus 15% Penalty + Full Deposit)";
    }
}
