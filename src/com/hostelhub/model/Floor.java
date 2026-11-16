package com.hostelhub.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single floor inside a hostel.
 * Demonstrates Composition: Floor HAS-A collection of Rooms.
 */
public class Floor implements Serializable {
    private static final long serialVersionUID = 1L;

    private int floorNumber;
    private List<Room> rooms;
    private String caretakerEmployeeId;

    public Floor(int floorNumber) {
        this.floorNumber = floorNumber;
        this.rooms = new ArrayList<>();
        this.caretakerEmployeeId = null;
    }

    public void addRoom(Room room) {
        this.rooms.add(room);
    }

    public Room findRoom(String roomId) {
        for (Room r : rooms) {
            if (r.getRoomId().equalsIgnoreCase(roomId)) {
                return r;
            }
        }
        return null;
    }

    public List<Room> getAvailableRooms() {
        List<Room> available = new ArrayList<>();
        for (Room r : rooms) {
            if (!r.isFull()) {
                available.add(r);
            }
        }
        return available;
    }

    public int getTotalCapacity() {
        int total = 0;
        for (Room r : rooms) {
            total += r.getCapacity();
        }
        return total;
    }

    public int getCurrentOccupancy() {
        int current = 0;
        for (Room r : rooms) {
            current += (r.getCapacity() - r.getAvailableBeds());
        }
        return current;
    }

    public int getAvailableBeds() {
        return getTotalCapacity() - getCurrentOccupancy();
    }

    // Getters and Setters
    public int getFloorNumber() {
        return floorNumber;
    }

    public List<Room> getRooms() {
        return new ArrayList<>(rooms);
    }

    public String getCaretakerEmployeeId() {
        return caretakerEmployeeId;
    }

    public void setCaretakerEmployeeId(String caretakerEmployeeId) {
        this.caretakerEmployeeId = caretakerEmployeeId;
    }
}
