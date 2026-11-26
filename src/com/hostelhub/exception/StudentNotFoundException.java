package com.hostelhub.exception;

/**
 * Thrown when an operation is requested for a student roll number that does not exist.
 */
public class StudentNotFoundException extends HostelException {
    private static final long serialVersionUID = 1L;

    public StudentNotFoundException(String rollNumber) {
        super(String.format("Student with Roll Number '%s' was not found in the system.", rollNumber));
    }
}
