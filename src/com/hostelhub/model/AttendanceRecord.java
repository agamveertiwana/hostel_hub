package com.hostelhub.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Tracks student daily attendance marked by floor caretakers.
 */
public class AttendanceRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private String recordId;
    private String studentRollNumber;
    private String hostelId;
    private int floorNumber;
    private LocalDate date;
    private AttendanceStatus status;
    private String markedByCaretakerId;

    public AttendanceRecord(String recordId, String studentRollNumber, String hostelId,
                            int floorNumber, LocalDate date, AttendanceStatus status, String markedByCaretakerId) {
        this.recordId = recordId;
        this.studentRollNumber = studentRollNumber;
        this.hostelId = hostelId;
        this.floorNumber = floorNumber;
        this.date = date;
        this.status = status;
        this.markedByCaretakerId = markedByCaretakerId;
    }

    public String getRecordId() { return recordId; }
    public String getStudentRollNumber() { return studentRollNumber; }
    public String getHostelId() { return hostelId; }
    public int getFloorNumber() { return floorNumber; }
    public LocalDate getDate() { return date; }
    public AttendanceStatus getStatus() { return status; }
    public String getMarkedByCaretakerId() { return markedByCaretakerId; }
}
