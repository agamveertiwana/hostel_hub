package com.hostelhub.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a student grievance or maintenance issue.
 */
public class Complaint implements Serializable {
    private static final long serialVersionUID = 1L;

    private String complaintId;
    private String studentRollNumber;
    private String hostelId;
    private String roomId;
    private ComplaintType type;
    private ComplaintPriority priority;
    private String description;
    private ComplaintStatus status;
    private LocalDateTime filedTime;
    private String resolvedByStaffId;
    private String resolutionNotes;

    public Complaint(String complaintId, String studentRollNumber, String hostelId, String roomId,
                     ComplaintType type, ComplaintPriority priority, String description) {
        this.complaintId = complaintId;
        this.studentRollNumber = studentRollNumber;
        this.hostelId = hostelId;
        this.roomId = roomId;
        this.type = type;
        this.priority = priority;
        this.description = description;
        this.status = ComplaintStatus.OPEN;
        this.filedTime = LocalDateTime.now();
        this.resolvedByStaffId = null;
        this.resolutionNotes = null;
    }

    public void markInProgress(String staffId) {
        this.status = ComplaintStatus.IN_PROGRESS;
        this.resolvedByStaffId = staffId;
    }

    public void resolve(String staffId, String notes) {
        this.status = ComplaintStatus.RESOLVED;
        this.resolvedByStaffId = staffId;
        this.resolutionNotes = notes;
    }

    public void reject(String staffId, String reason) {
        this.status = ComplaintStatus.REJECTED;
        this.resolvedByStaffId = staffId;
        this.resolutionNotes = reason;
    }

    // Getters
    public String getComplaintId() { return complaintId; }
    public String getStudentRollNumber() { return studentRollNumber; }
    public String getHostelId() { return hostelId; }
    public String getRoomId() { return roomId; }
    public ComplaintType getType() { return type; }
    public ComplaintPriority getPriority() { return priority; }
    public String getDescription() { return description; }
    public ComplaintStatus getStatus() { return status; }
    public LocalDateTime getFiledTime() { return filedTime; }
    public String getResolvedByStaffId() { return resolvedByStaffId; }
    public String getResolutionNotes() { return resolutionNotes; }
}
