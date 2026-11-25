package com.hostelhub.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Printable invoice / receipt for fee settlements.
 */
public class Receipt implements Serializable {
    private static final long serialVersionUID = 1L;

    private String receiptNumber;
    private String transactionId;
    private String studentRollNumber;
    private String studentName;
    private String hostelName;
    private String roomId;
    private double totalAmount;
    private LocalDateTime issuedDate;

    public Receipt(String receiptNumber, String transactionId, String studentRollNumber,
                   String studentName, String hostelName, String roomId, double totalAmount) {
        this.receiptNumber = receiptNumber;
        this.transactionId = transactionId;
        this.studentRollNumber = studentRollNumber;
        this.studentName = studentName;
        this.hostelName = hostelName;
        this.roomId = roomId;
        this.totalAmount = totalAmount;
        this.issuedDate = LocalDateTime.now();
    }

    public String printReceipt() {
        return String.format(
                "=========================================\n" +
                "           HOSTELHUB FEE RECEIPT         \n" +
                "=========================================\n" +
                "Receipt No  : %s\n" +
                "Date & Time : %s\n" +
                "Roll Number : %s\n" +
                "Student     : %s\n" +
                "Hostel/Room : %s (Room %s)\n" +
                "Amount Paid : Rs. %.2f\n" +
                "Txn ID      : %s\n" +
                "Status      : PAID & VERIFIED\n" +
                "=========================================\n",
                receiptNumber, issuedDate, studentRollNumber, studentName, hostelName, roomId, totalAmount, transactionId);
    }

    public String getReceiptNumber() { return receiptNumber; }
    public String getTransactionId() { return transactionId; }
    public String getStudentRollNumber() { return studentRollNumber; }
    public double getTotalAmount() { return totalAmount; }
}
