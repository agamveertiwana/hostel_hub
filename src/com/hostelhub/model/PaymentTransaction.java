package com.hostelhub.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Tracks a fee payment transaction made by a student.
 */
public class PaymentTransaction implements Serializable {
    private static final long serialVersionUID = 1L;

    private String transactionId;
    private String studentRollNumber;
    private double amountPaid;
    private PaymentMode paymentMode;
    private PaymentStatus status;
    private LocalDateTime transactionDate;
    private String referenceNote;

    public PaymentTransaction(String transactionId, String studentRollNumber, double amountPaid,
                              PaymentMode paymentMode, String referenceNote) {
        this.transactionId = transactionId;
        this.studentRollNumber = studentRollNumber;
        this.amountPaid = amountPaid;
        this.paymentMode = paymentMode;
        this.status = PaymentStatus.SUCCESS;
        this.transactionDate = LocalDateTime.now();
        this.referenceNote = referenceNote;
    }

    public String getTransactionId() { return transactionId; }
    public String getStudentRollNumber() { return studentRollNumber; }
    public double getAmountPaid() { return amountPaid; }
    public PaymentMode getPaymentMode() { return paymentMode; }
    public PaymentStatus getStatus() { return status; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public String getReferenceNote() { return referenceNote; }
}
