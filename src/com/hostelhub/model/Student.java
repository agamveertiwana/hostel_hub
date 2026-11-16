package com.hostelhub.model;

/**
 * Represents a student residing or applying for a hostel.
 * Demonstrates Inheritance from Person.
 * Roll Number acts as the unique student ID.
 */
public class Student extends Person {
    private static final long serialVersionUID = 1L;

    private String rollNumber;
    private String department;
    private int year;
    private double distanceFromCollege; // in Kilometers
    private String allocatedHostelName;
    private String allocatedRoomId;

    public Student(String rollNumber, String name, String phone, String email, Gender gender,
                   String department, int year, double distanceFromCollege) {
        super(rollNumber, name, phone, email, gender); // Roll number acts as unique ID
        this.rollNumber = rollNumber;
        this.department = department;
        this.year = year;
        this.distanceFromCollege = distanceFromCollege;
        this.allocatedHostelName = null;
        this.allocatedRoomId = null;
    }

    /**
     * Business Rule: Students living <= 30 km are considered localites.
     */
    public boolean isLocalite() {
        return this.distanceFromCollege <= 30.0;
    }

    public boolean hasRoomAllocated() {
        return this.allocatedRoomId != null && !this.allocatedRoomId.isEmpty();
    }

    @Override
    public Role getRole() {
        return Role.STUDENT;
    }

    @Override
    public void displayDetails() {
        System.out.println("-------------------------------------------------");
        System.out.println("Student Profile: " + getName() + " (" + getRole() + ")");
        System.out.println("Roll Number   : " + rollNumber);
        System.out.println("Department    : " + department + " (Year " + year + ")");
        System.out.println("Gender        : " + getGender());
        System.out.println("Contact       : " + getPhone() + " | " + getEmail());
        System.out.printf("Home Distance : %.2f km %s%n", distanceFromCollege, (isLocalite() ? "[Localite <= 30km]" : "[Eligible > 30km]"));
        System.out.println("Hostel Status : " + (hasRoomAllocated() ? allocatedHostelName + " - Room " + allocatedRoomId : "Not Allocated (Waitlisted)"));
        System.out.println("-------------------------------------------------");
    }

    // Getters and Setters
    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
        setId(rollNumber);
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getDistanceFromCollege() {
        return distanceFromCollege;
    }

    public void setDistanceFromCollege(double distanceFromCollege) {
        this.distanceFromCollege = distanceFromCollege;
    }

    public String getAllocatedHostelName() {
        return allocatedHostelName;
    }

    public void setAllocatedHostelName(String allocatedHostelName) {
        this.allocatedHostelName = allocatedHostelName;
    }

    public String getAllocatedRoomId() {
        return allocatedRoomId;
    }

    public void setAllocatedRoomId(String allocatedRoomId) {
        this.allocatedRoomId = allocatedRoomId;
    }
}
