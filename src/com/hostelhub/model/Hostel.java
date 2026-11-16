package com.hostelhub.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete hostel building.
 * Demonstrates Multilevel Composition: Hostel HAS Floors, Floor HAS Rooms.
 */
public class Hostel implements Serializable {
    private static final long serialVersionUID = 1L;

    private String hostelId;
    private String name;
    private Gender allowedGender; // MALE or FEMALE
    private List<Floor> floors;
    private String wardenEmployeeId;

    public Hostel(String hostelId, String name, Gender allowedGender) {
        this.hostelId = hostelId;
        this.name = name;
        this.allowedGender = allowedGender;
        this.floors = new ArrayList<>();
        this.wardenEmployeeId = null;
    }

    public void addFloor(Floor floor) {
        this.floors.add(floor);
    }

    public Floor getFloor(int floorNumber) {
        for (Floor f : floors) {
            if (f.getFloorNumber() == floorNumber) {
                return f;
            }
        }
        return null;
    }

    public Room findRoom(String roomId) {
        for (Floor f : floors) {
            Room r = f.findRoom(roomId);
            if (r != null) {
                return r;
            }
        }
        return null;
    }

    public int getTotalRooms() {
        int count = 0;
        for (Floor f : floors) {
            count += f.getRooms().size();
        }
        return count;
    }

    public int getTotalCapacity() {
        int total = 0;
        for (Floor f : floors) {
            total += f.getTotalCapacity();
        }
        return total;
    }

    public int getCurrentOccupancy() {
        int total = 0;
        for (Floor f : floors) {
            total += f.getCurrentOccupancy();
        }
        return total;
    }

    public int getAvailableBeds() {
        return getTotalCapacity() - getCurrentOccupancy();
    }

    public void displayHostelSummary() {
        System.out.println("=================================================");
        System.out.printf("HOSTEL: %s [%s]%n", name, allowedGender);
        System.out.println("Hostel ID    : " + hostelId);
        System.out.println("Total Floors : " + floors.size());
        System.out.println("Total Rooms  : " + getTotalRooms());
        System.out.printf("Occupancy    : %d / %d Beds (Available: %d)%n",
                getCurrentOccupancy(), getTotalCapacity(), getAvailableBeds());
        System.out.println("=================================================");
        for (Floor f : floors) {
            System.out.printf("--- Floor %d (Caretaker: %s) ---%n",
                    f.getFloorNumber(), (f.getCaretakerEmployeeId() == null ? "Unassigned" : f.getCaretakerEmployeeId()));
            for (Room r : f.getRooms()) {
                r.displayRoomDetails();
            }
        }
        System.out.println("=================================================");
    }

    // Getters and Setters
    public String getHostelId() {
        return hostelId;
    }

    public String getName() {
        return name;
    }

    public Gender getAllowedGender() {
        return allowedGender;
    }

    public List<Floor> getFloors() {
        return new ArrayList<>(floors);
    }

    public String getWardenEmployeeId() {
        return wardenEmployeeId;
    }

    public void setWardenEmployeeId(String wardenEmployeeId) {
        this.wardenEmployeeId = wardenEmployeeId;
    }
}
