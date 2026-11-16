package com.hostelhub.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a hostel room with capacity, facilities, and dynamic fee calculation.
 */
public class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    private String roomId;
    private int floorNumber;
    private int capacity; // e.g. 2, 3, or 4 seater
    private RoomType roomType; // AC or NON_AC
    private WashroomType washroomType; // ATTACHED or COMMON
    private List<String> studentRollNumbers; // Allocated students

    public Room(String roomId, int floorNumber, int capacity, RoomType roomType, WashroomType washroomType) {
        this.roomId = roomId;
        this.floorNumber = floorNumber;
        this.capacity = capacity;
        this.roomType = roomType;
        this.washroomType = washroomType;
        this.studentRollNumbers = new ArrayList<>();
    }

    public boolean isFull() {
        return studentRollNumbers.size() >= capacity;
    }

    public int getAvailableBeds() {
        return capacity - studentRollNumbers.size();
    }

    public boolean allocateStudent(String rollNumber) {
        if (isFull()) {
            return false;
        }
        if (!studentRollNumbers.contains(rollNumber)) {
            studentRollNumbers.add(rollNumber);
            return true;
        }
        return false;
    }

    public boolean removeStudent(String rollNumber) {
        return studentRollNumbers.remove(rollNumber);
    }

    /**
     * Dynamic formula-based fee calculation per student per semester:
     * - Base rent according to capacity (2-seater > 3-seater > 4-seater)
     * - AC surcharge (+15,000)
     * - Attached washroom surcharge (+8,000)
     */
    public double calculateSemesterFee() {
        double baseFee;
        switch (capacity) {
            case 2:
                baseFee = 50000.0;
                break;
            case 3:
                baseFee = 40000.0;
                break;
            case 4:
                baseFee = 32000.0;
                break;
            default:
                baseFee = 35000.0;
                break;
        }

        if (roomType == RoomType.AC) {
            baseFee += 15000.0;
        }

        if (washroomType == WashroomType.ATTACHED) {
            baseFee += 8000.0;
        }

        return baseFee;
    }

    public void displayRoomDetails() {
        System.out.printf("  [Room %s] Floor: %d | %d-Seater | %s | %s | Occupancy: %d/%d (Beds Free: %d) | Fee: Rs. %.0f%n",
                roomId, floorNumber, capacity, roomType, washroomType,
                studentRollNumbers.size(), capacity, getAvailableBeds(), calculateSemesterFee());
    }

    // Getters and Setters
    public String getRoomId() {
        return roomId;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public int getCapacity() {
        return capacity;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public WashroomType getWashroomType() {
        return washroomType;
    }

    public List<String> getStudentRollNumbers() {
        return new ArrayList<>(studentRollNumbers);
    }
}
