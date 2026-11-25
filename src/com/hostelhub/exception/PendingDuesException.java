package com.hostelhub.exception;

/**
 * Thrown when a student attempts to vacate or checkout while having unpaid hostel dues.
 */
public class PendingDuesException extends HostelException {
    private static final long serialVersionUID = 1L;

    public PendingDuesException(String rollNumber, double pendingAmount) {
        super(String.format("Cannot checkout student '%s'. Pending dues of Rs. %.2f must be cleared first.",
                rollNumber, pendingAmount));
    }
}
