package com.hostelhub.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Abstract base class for Outpass / Gate Pass requests.
 * Demonstrates Abstraction and Inheritance.
 */
public abstract class GatePass implements Serializable {
    private static final long serialVersionUID = 1L;

    private String passId;
    private String studentRollNumber;
    private String reason;
    private LocalDateTime appliedTime;
    private LocalDateTime outTime;
    private LocalDateTime expectedInTime;
    private LocalDateTime actualInTime;
    private GatePassStatus status;
    private String approvedByWardenId;

    public GatePass(String passId, String studentRollNumber, String reason,
                    LocalDateTime outTime, LocalDateTime expectedInTime) {
        this.passId = passId;
        this.studentRollNumber = studentRollNumber;
        this.reason = reason;
        this.appliedTime = LocalDateTime.now();
        this.outTime = outTime;
        this.expectedInTime = expectedInTime;
        this.status = GatePassStatus.PENDING;
        this.approvedByWardenId = null;
    }

    public abstract String getPassType();
    public abstract boolean requiresParentApproval();

    public void approve(String wardenId) {
        this.status = GatePassStatus.APPROVED;
        this.approvedByWardenId = wardenId;
    }

    public void reject(String wardenId) {
        this.status = GatePassStatus.REJECTED;
        this.approvedByWardenId = wardenId;
    }

    public void recordReturn() {
        this.actualInTime = LocalDateTime.now();
        this.status = GatePassStatus.RETURNED;
    }

    // Getters and Setters
    public String getPassId() { return passId; }
    public String getStudentRollNumber() { return studentRollNumber; }
    public String getReason() { return reason; }
    public LocalDateTime getAppliedTime() { return appliedTime; }
    public LocalDateTime getOutTime() { return outTime; }
    public LocalDateTime getExpectedInTime() { return expectedInTime; }
    public LocalDateTime getActualInTime() { return actualInTime; }
    public GatePassStatus getStatus() { return status; }
    public String getApprovedByWardenId() { return approvedByWardenId; }
}
