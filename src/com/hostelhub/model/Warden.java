package com.hostelhub.model;

/**
 * Represents the Warden or Assistant Warden in charge of a hostel.
 */
public class Warden extends Staff {
    private static final long serialVersionUID = 1L;

    private String assignedHostelName;
    private boolean isAssistant;

    public Warden(String id, String name, String phone, String email, Gender gender,
                  String employeeId, String shift, String assignedHostelName, boolean isAssistant) {
        super(id, name, phone, email, gender, employeeId, shift);
        this.assignedHostelName = assignedHostelName;
        this.isAssistant = isAssistant;
    }

    public boolean canApproveGatePass() {
        return true;
    }

    @Override
    public Role getRole() {
        return isAssistant ? Role.ASSISTANT_WARDEN : Role.WARDEN;
    }

    @Override
    public void displayDetails() {
        System.out.println("-------------------------------------------------");
        System.out.println("Staff Profile : " + getName() + " [" + getRole() + "]");
        System.out.println("Employee ID   : " + getEmployeeId());
        System.out.println("Assigned Hostel: " + assignedHostelName);
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

    public boolean isAssistant() {
        return isAssistant;
    }

    public void setAssistant(boolean assistant) {
        isAssistant = assistant;
    }
}
