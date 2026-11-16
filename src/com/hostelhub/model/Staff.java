package com.hostelhub.model;

/**
 * Abstract Staff class representing hostel staff members.
 * Demonstrates Multilevel Inheritance (Person -> Staff -> Warden/Caretaker).
 */
public abstract class Staff extends Person {
    private static final long serialVersionUID = 1L;

    private String employeeId;
    private String shift; // "Morning", "Evening", "Night"

    public Staff(String id, String name, String phone, String email, Gender gender,
                 String employeeId, String shift) {
        super(id, name, phone, email, gender);
        this.employeeId = employeeId;
        this.shift = shift;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }
}
