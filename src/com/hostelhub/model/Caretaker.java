package com.hostelhub.model;

/**
 * Floor-level caretaker responsible for a specific floor in a hostel.
 */
public class Caretaker extends Staff {
    private static final long serialVersionUID = 1L;

    private String assignedHostelName;
    private int assignedFloorNumber;

    public Caretaker(String id, String name, String phone, String email, Gender gender,
                     String employeeId, String shift, String assignedHostelName, int assignedFloorNumber) {
        super(id, name, phone, email, gender, employeeId, shift);
        this.assignedHostelName = assignedHostelName;
        this.assignedFloorNumber = assignedFloorNumber;
    }

    @Override
    public Role getRole() {
        return Role.CARETAKER;
    }

    @Override
    public void displayDetails() {
        System.out.println("-------------------------------------------------");
        System.out.println("Staff Profile : " + getName() + " [" + getRole() + "]");
        System.out.println("Employee ID   : " + getEmployeeId());
        System.out.println("Assigned Hostel: " + assignedHostelName + " (Floor " + assignedFloorNumber + ")");
        System.out.println("Shift Time    : " + getShift());
        System.out.println("Contact       : " + getPhone() + " | " + getEmail());
        System.out.println("-------------------------------------------------");
    }

    public String getAssignedHostelName() {
        return assignedHostelName;
    }

    public void setAssignedHostelName(String assignedHostelName) {
        this.assignedHostelName = assignedHostelName;
    }

    public int getAssignedFloorNumber() {
        return assignedFloorNumber;
    }

    public void setAssignedFloorNumber(int assignedFloorNumber) {
        this.assignedFloorNumber = assignedFloorNumber;
    }
}
