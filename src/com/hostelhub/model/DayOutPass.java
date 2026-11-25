package com.hostelhub.model;

import java.time.LocalDateTime;

/**
 * Gate pass for same-day outing (must return before hostel curfew).
 */
public class DayOutPass extends GatePass {
    private static final long serialVersionUID = 1L;

    public DayOutPass(String passId, String studentRollNumber, String reason,
                      LocalDateTime outTime, LocalDateTime expectedInTime) {
        super(passId, studentRollNumber, reason, outTime, expectedInTime);
    }

    @Override
    public String getPassType() {
        return "DAY_OUT";
    }

    @Override
    public boolean requiresParentApproval() {
        return false;
    }
}
