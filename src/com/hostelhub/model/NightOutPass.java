package com.hostelhub.model;

import java.time.LocalDateTime;

/**
 * Gate pass for overnight stay outside the hostel (requires parent consent & destination address).
 */
public class NightOutPass extends GatePass {
    private static final long serialVersionUID = 1L;

    private String destinationAddress;
    private String emergencyContactNumber;
    private boolean parentConsentVerified;

    public NightOutPass(String passId, String studentRollNumber, String reason,
                        LocalDateTime outTime, LocalDateTime expectedInTime,
                        String destinationAddress, String emergencyContactNumber) {
        super(passId, studentRollNumber, reason, outTime, expectedInTime);
        this.destinationAddress = destinationAddress;
        this.emergencyContactNumber = emergencyContactNumber;
        this.parentConsentVerified = false;
    }

    @Override
    public String getPassType() {
        return "NIGHT_OUT";
    }

    @Override
    public boolean requiresParentApproval() {
        return true;
    }

    public void verifyParentConsent() {
        this.parentConsentVerified = true;
    }

    public String getDestinationAddress() { return destinationAddress; }
    public String getEmergencyContactNumber() { return emergencyContactNumber; }
    public boolean isParentConsentVerified() { return parentConsentVerified; }
}
