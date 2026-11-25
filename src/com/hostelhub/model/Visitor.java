package com.hostelhub.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Visitor log for parents, guardians, or guests visiting hostel residents.
 */
public class Visitor implements Serializable {
    private static final long serialVersionUID = 1L;

    private String visitorId;
    private String visitorName;
    private String phone;
    private String relation;
    private String studentRollNumber;
    private String hostelId;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    public Visitor(String visitorId, String visitorName, String phone, String relation,
                   String studentRollNumber, String hostelId) {
        this.visitorId = visitorId;
        this.visitorName = visitorName;
        this.phone = phone;
        this.relation = relation;
        this.studentRollNumber = studentRollNumber;
        this.hostelId = hostelId;
        this.checkInTime = LocalDateTime.now();
        this.checkOutTime = null;
    }

    public void checkOut() {
        this.checkOutTime = LocalDateTime.now();
    }

    public String getVisitorId() { return visitorId; }
    public String getVisitorName() { return visitorName; }
    public String getPhone() { return phone; }
    public String getRelation() { return relation; }
    public String getStudentRollNumber() { return studentRollNumber; }
    public String getHostelId() { return hostelId; }
    public LocalDateTime getCheckInTime() { return checkInTime; }
    public LocalDateTime getCheckOutTime() { return checkOutTime; }
}
