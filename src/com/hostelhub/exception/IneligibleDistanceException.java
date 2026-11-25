package com.hostelhub.exception;

/**
 * Thrown when an allocation rule violation occurs regarding student distance.
 */
public class IneligibleDistanceException extends HostelException {
    private static final long serialVersionUID = 1L;

    public IneligibleDistanceException(String rollNumber, double distance, double threshold) {
        super(String.format("Student '%s' lives %.2f km away (<= %.2f km threshold). Priority belongs to outstation applicants.",
                rollNumber, distance, threshold));
    }
}
