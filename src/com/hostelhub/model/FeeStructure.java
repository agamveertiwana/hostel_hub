package com.hostelhub.model;

import java.io.Serializable;

/**
 * Encapsulates the fee breakdown components for a hostel room.
 */
public class FeeStructure implements Serializable {
    private static final long serialVersionUID = 1L;

    private double baseRoomRent;
    private double messFee;
    private double securityDeposit;
    private double acSurcharge;
    private double attachedWashroomSurcharge;

    public FeeStructure(double baseRoomRent, double messFee, double securityDeposit,
                        double acSurcharge, double attachedWashroomSurcharge) {
        this.baseRoomRent = baseRoomRent;
        this.messFee = messFee;
        this.securityDeposit = securityDeposit;
        this.acSurcharge = acSurcharge;
        this.attachedWashroomSurcharge = attachedWashroomSurcharge;
    }

    public double calculateTotalSemesterFee(boolean isAc, boolean isAttachedWashroom) {
        double total = baseRoomRent + messFee + securityDeposit;
        if (isAc) total += acSurcharge;
        if (isAttachedWashroom) total += attachedWashroomSurcharge;
        return total;
    }

    public double getBaseRoomRent() { return baseRoomRent; }
    public double getMessFee() { return messFee; }
    public double getSecurityDeposit() { return securityDeposit; }
    public double getAcSurcharge() { return acSurcharge; }
    public double getAttachedWashroomSurcharge() { return attachedWashroomSurcharge; }
}
