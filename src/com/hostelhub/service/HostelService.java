package com.hostelhub.service;

import com.hostelhub.model.*;

import java.io.*;
import java.util.*;

/**
 * Service managing all runtime data, file persistence, and business operations for HostelHub.
 */
public class HostelService {
    private static final String DATA_FILE = "data" + File.separator + "hostelhub_state.dat";

    private List<Hostel> hostels;
    private Map<String, Student> studentsByRollNumber;
    private Map<String, Staff> staffByEmployeeId;
    private List<Student> waitlistLocalites; // Distance <= 30 km
    private Queue<Student> allocationQueue;  // Distance > 30 km (FCFS)

    public HostelService() {
        this.hostels = new ArrayList<>();
        this.studentsByRollNumber = new LinkedHashMap<>();
        this.staffByEmployeeId = new LinkedHashMap<>();
        this.waitlistLocalites = new ArrayList<>();
        this.allocationQueue = new LinkedList<>();
        // Auto-load previously saved state from disk if it exists
        loadDataFromDisk();
    }

    // --- HOSTEL OPERATIONS ---

    public void addHostel(Hostel hostel) {
        if (findHostelById(hostel.getHostelId()) == null) {
            hostels.add(hostel);
            saveDataToDisk();
        }
    }

    public Hostel findHostelById(String hostelId) {
        if (hostelId == null) return null;
        for (Hostel h : hostels) {
            if (h.getHostelId().equalsIgnoreCase(hostelId.trim())) {
                return h;
            }
        }
        return null;
    }

    public List<Hostel> getAllHostels() {
        return hostels;
    }

    public Room findRoomAcrossHostels(String hostelId, String roomId) {
        Hostel hostel = findHostelById(hostelId);
        if (hostel != null) {
            return hostel.findRoom(roomId);
        }
        return null;
    }

    // --- STAFF OPERATIONS ---

    public boolean registerStaff(Staff staff) {
        String empKey = staff.getEmployeeId().trim().toUpperCase();
        if (staffByEmployeeId.containsKey(empKey)) {
            return false;
        }
        staffByEmployeeId.put(empKey, staff);
        saveDataToDisk();
        return true;
    }

    public Staff findStaffByEmployeeId(String employeeId) {
        if (employeeId == null) return null;
        return staffByEmployeeId.get(employeeId.trim().toUpperCase());
    }

    public Collection<Staff> getAllStaff() {
        return staffByEmployeeId.values();
    }

    public String assignWardenToHostel(String employeeId, String hostelId) {
        Staff staff = findStaffByEmployeeId(employeeId);
        if (staff == null) {
            return "ERROR: Staff with Employee ID '" + employeeId + "' does not exist!";
        }
        if (!(staff instanceof Warden)) {
            return "ERROR: " + staff.getName() + " is a " + staff.getRole() + ", not a Warden!";
        }

        Hostel hostel = findHostelById(hostelId);
        if (hostel == null) {
            return "ERROR: Hostel ID '" + hostelId + "' does not exist!";
        }

        Warden warden = (Warden) staff;
        warden.setAssignedHostelName(hostel.getName());
        hostel.setWardenEmployeeId(warden.getEmployeeId());
        saveDataToDisk();
        return "SUCCESS: Assigned Warden " + warden.getName() + " (" + warden.getEmployeeId() + ") to " + hostel.getName() + "!";
    }

    public String assignCaretakerToFloor(String employeeId, String hostelId, int floorNumber) {
        Staff staff = findStaffByEmployeeId(employeeId);
        if (staff == null) {
            return "ERROR: Staff with Employee ID '" + employeeId + "' does not exist!";
        }
        if (!(staff instanceof Caretaker)) {
            return "ERROR: " + staff.getName() + " is a " + staff.getRole() + ", not a Caretaker!";
        }

        Hostel hostel = findHostelById(hostelId);
        if (hostel == null) {
            return "ERROR: Hostel ID '" + hostelId + "' does not exist!";
        }

        Floor floor = hostel.getFloor(floorNumber);
        if (floor == null) {
            return "ERROR: Floor " + floorNumber + " does not exist in " + hostel.getName() + "!";
        }

        Caretaker caretaker = (Caretaker) staff;
        caretaker.setAssignedHostelName(hostel.getName());
        caretaker.setAssignedFloorNumber(floorNumber);
        floor.setCaretakerEmployeeId(caretaker.getEmployeeId());
        saveDataToDisk();
        return "SUCCESS: Assigned Caretaker " + caretaker.getName() + " (" + caretaker.getEmployeeId()
                + ") to " + hostel.getName() + " - Floor " + floorNumber + "!";
    }

    // --- STUDENT OPERATIONS ---

    public boolean registerStudent(Student student) {
        String rollKey = student.getRollNumber().trim().toUpperCase();
        if (studentsByRollNumber.containsKey(rollKey)) {
            return false;
        }

        studentsByRollNumber.put(rollKey, student);

        if (student.isLocalite()) {
            waitlistLocalites.add(student);
        } else {
            allocationQueue.add(student);
        }
        saveDataToDisk();
        return true;
    }

    public Student findStudentByRoll(String rollNumber) {
        if (rollNumber == null) return null;
        return studentsByRollNumber.get(rollNumber.trim().toUpperCase());
    }

    public Collection<Student> getAllStudents() {
        return studentsByRollNumber.values();
    }

    public List<Student> getWaitlistLocalites() {
        return waitlistLocalites;
    }

    public Queue<Student> getAllocationQueue() {
        return allocationQueue;
    }

    // --- ALLOCATION LOGIC ---

    public String allocateRoom(String rollNumber, String hostelId, String roomId) {
        Student student = findStudentByRoll(rollNumber);
        if (student == null) {
            return "ERROR: Student with Roll Number '" + rollNumber + "' was not found!";
        }

        if (student.hasRoomAllocated()) {
            return "ERROR: Student is already allocated to Room " + student.getAllocatedRoomId() + " in " + student.getAllocatedHostelName() + "!";
        }

        Hostel hostel = findHostelById(hostelId);
        if (hostel == null) {
            return "ERROR: Hostel ID '" + hostelId + "' does not exist!";
        }

        // Gender validation
        if (student.getGender() != hostel.getAllowedGender()) {
            return "ERROR: Gender mismatch! " + student.getName() + " is " + student.getGender()
                    + ", but " + hostel.getName() + " is strictly for " + hostel.getAllowedGender() + " students!";
        }

        Room room = hostel.findRoom(roomId);
        if (room == null) {
            return "ERROR: Room '" + roomId + "' does not exist in " + hostel.getName() + "!";
        }

        if (room.isFull()) {
            return "ERROR: Room '" + roomId + "' is completely full (Capacity: " + room.getCapacity() + ")!";
        }

        // 30 km rule check
        if (student.isLocalite() && !allocationQueue.isEmpty()) {
            return "NOTICE: Student lives <= 30 km (" + student.getDistanceFromCollege() + " km). "
                    + "There are still " + allocationQueue.size() + " outstation students in the priority queue. "
                    + "Rooms are reserved for > 30 km students first!";
        }

        boolean success = room.allocateStudent(student.getRollNumber());
        if (success) {
            student.setAllocatedHostelName(hostel.getName());
            student.setAllocatedRoomId(room.getRoomId());
            allocationQueue.remove(student);
            waitlistLocalites.remove(student);
            saveDataToDisk();
            return "SUCCESS: Allocated Room " + room.getRoomId() + " in " + hostel.getName()
                    + " to " + student.getName() + " (Fee: Rs. " + String.format("%.0f", room.calculateSemesterFee()) + ")!";
        }

        return "ERROR: Allocation failed.";
    }

    // --- SAMPLE DATA LOAD ---
    public void loadSampleData() {
        // Sample Staff
        if (findStaffByEmployeeId("EMP-WRD-01") == null) {
            Warden w1 = new Warden("W01", "Dr. Rajesh Sharma", "9876543210", "rajesh.wrd@college.edu",
                    Gender.MALE, "EMP-WRD-01", "Day Shift", "Aryabhatta Boys Hostel", false);
            registerStaff(w1);
        }

        if (findStaffByEmployeeId("EMP-CT-01") == null) {
            Caretaker ct1 = new Caretaker("C01", "Suresh Kumar", "9123456780", "suresh.ct@college.edu",
                    Gender.MALE, "EMP-CT-01", "Morning", "Aryabhatta Boys Hostel", 1);
            registerStaff(ct1);
        }

        if (findStaffByEmployeeId("EMP-CT-02") == null) {
            Caretaker ct2 = new Caretaker("C02", "Ramesh Yadav", "9123456782", "ramesh.ct@college.edu",
                    Gender.MALE, "EMP-CT-02", "Evening", "Aryabhatta Boys Hostel", 2);
            registerStaff(ct2);
        }

        // Sample Hostel 1: Boys
        if (findHostelById("BH1") == null) {
            Hostel bh1 = new Hostel("BH1", "Aryabhatta Boys Hostel", Gender.MALE);
            bh1.setWardenEmployeeId("EMP-WRD-01");

            Floor f1 = new Floor(1);
            f1.addRoom(new Room("101", 1, 2, RoomType.AC, WashroomType.ATTACHED));
            f1.addRoom(new Room("102", 1, 3, RoomType.NON_AC, WashroomType.COMMON));
            f1.setCaretakerEmployeeId("EMP-CT-01");
            bh1.addFloor(f1);

            Floor f2 = new Floor(2);
            f2.addRoom(new Room("201", 2, 2, RoomType.AC, WashroomType.ATTACHED));
            f2.addRoom(new Room("202", 2, 4, RoomType.NON_AC, WashroomType.COMMON));
            f2.setCaretakerEmployeeId("EMP-CT-02");
            bh1.addFloor(f2);
            addHostel(bh1);
        }

        // Sample Hostel 2: Girls
        if (findHostelById("GH1") == null) {
            Hostel gh1 = new Hostel("GH1", "Gargi Girls Hostel", Gender.FEMALE);
            Floor gf1 = new Floor(1);
            gf1.addRoom(new Room("G101", 1, 2, RoomType.AC, WashroomType.ATTACHED));
            gf1.addRoom(new Room("G102", 1, 3, RoomType.NON_AC, WashroomType.COMMON));
            gf1.setCaretakerEmployeeId("EMP-CT-03");
            gh1.addFloor(gf1);
            addHostel(gh1);
        }

        // Sample Students
        if (findStudentByRoll("2024CS01") == null) {
            Student s1 = new Student("2024CS01", "Aman Verma", "9988776655", "aman@student.edu",
                    Gender.MALE, "Computer Science", 1, 145.0);
            registerStudent(s1);
            allocateRoom("2024CS01", "BH1", "101");
        }

        if (findStudentByRoll("2024EC01") == null) {
            Student s2 = new Student("2024EC01", "Priya Sharma", "9876543211", "priya@student.edu",
                    Gender.FEMALE, "Electronics", 1, 85.0);
            registerStudent(s2);
            allocateRoom("2024EC01", "GH1", "G101");
        }

        if (findStudentByRoll("2024ME01") == null) {
            Student s3 = new Student("2024ME01", "Vikram Singh", "9123456781", "vikram@student.edu",
                    Gender.MALE, "Mechanical", 1, 210.0);
            registerStudent(s3);
        }

        if (findStudentByRoll("2024CS02") == null) {
            Student s4 = new Student("2024CS02", "Rohit Mehta", "9871234560", "rohit@student.edu",
                    Gender.MALE, "Civil", 1, 18.0);
            registerStudent(s4);
        }

        saveDataToDisk();
    }

    // --- SYSTEM AUDIT REPORT ---
    public String generateSystemAuditReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================\n");
        sb.append("                 HOSTELHUB - COMPREHENSIVE AUDIT REPORT                 \n");
        sb.append("========================================================================\n\n");

        int totalCapacity = 0;
        int totalOccupied = 0;
        for (Hostel h : hostels) {
            totalCapacity += h.getTotalCapacity();
            totalOccupied += h.getCurrentOccupancy();
        }
        int totalAvailable = totalCapacity - totalOccupied;

        sb.append(String.format("Total Hostels: %d | Total Beds: %d | Occupied: %d | Available Beds: %d%n",
                hostels.size(), totalCapacity, totalOccupied, totalAvailable));
        sb.append(String.format("Total Staff: %d | Total Students: %d (Allocated: %d | Queue: %d | Waitlist: %d)%n%n",
                staffByEmployeeId.size(),
                studentsByRollNumber.size(),
                (studentsByRollNumber.size() - allocationQueue.size() - waitlistLocalites.size()),
                allocationQueue.size(),
                waitlistLocalites.size()));

        sb.append("---------------------------- STAFF ROSTER ------------------------------\n");
        if (staffByEmployeeId.isEmpty()) {
            sb.append("No staff registered yet.\n\n");
        } else {
            for (Staff st : staffByEmployeeId.values()) {
                if (st instanceof Warden) {
                    Warden w = (Warden) st;
                    sb.append(String.format("ID: %-10s | %-16s | Role: %-12s | Assigned Hostel: %s | Shift: %s%n",
                            w.getEmployeeId(), w.getName(), w.getRole(),
                            (w.getAssignedHostelName() == null ? "None" : w.getAssignedHostelName()), w.getShift()));
                } else if (st instanceof Caretaker) {
                    Caretaker ct = (Caretaker) st;
                    sb.append(String.format("ID: %-10s | %-16s | Role: %-12s | Assigned: %s (Floor %d) | Shift: %s%n",
                            ct.getEmployeeId(), ct.getName(), ct.getRole(),
                            (ct.getAssignedHostelName() == null ? "None" : ct.getAssignedHostelName()),
                            ct.getAssignedFloorNumber(), ct.getShift()));
                }
            }
            sb.append("\n");
        }

        sb.append("--------------------------- HOSTEL DETAILS -----------------------------\n");
        if (hostels.isEmpty()) {
            sb.append("No hostels found.\n\n");
        } else {
            for (Hostel h : hostels) {
                sb.append(String.format("Hostel [%s] %s (%s) - Warden: %s | Beds: %d/%d (Available: %d)%n",
                        h.getHostelId(), h.getName(), h.getAllowedGender(),
                        (h.getWardenEmployeeId() == null ? "Unassigned" : h.getWardenEmployeeId()),
                        h.getCurrentOccupancy(), h.getTotalCapacity(), h.getAvailableBeds()));
                for (Floor f : h.getFloors()) {
                    sb.append(String.format("   Floor %d (Rooms: %d, Caretaker: %s)%n",
                            f.getFloorNumber(), f.getRooms().size(),
                            (f.getCaretakerEmployeeId() == null ? "Unassigned" : f.getCaretakerEmployeeId())));
                    for (Room r : f.getRooms()) {
                        sb.append(String.format("      Room %s: %d-Seater | %s | %s | Occupied: %d/%d (Free: %d) | Fee: Rs. %.0f | Students: %s%n",
                                r.getRoomId(), r.getCapacity(), r.getRoomType(), r.getWashroomType(),
                                (r.getCapacity() - r.getAvailableBeds()), r.getCapacity(), r.getAvailableBeds(),
                                r.calculateSemesterFee(),
                                r.getStudentRollNumbers().isEmpty() ? "None" : r.getStudentRollNumbers().toString()));
                    }
                }
                sb.append("\n");
            }
        }

        sb.append("--------------------------- STUDENT ROSTER -----------------------------\n");
        if (studentsByRollNumber.isEmpty()) {
            sb.append("No students registered.\n");
        } else {
            for (Student s : studentsByRollNumber.values()) {
                sb.append(String.format("Roll: %-10s | Name: %-16s | Gender: %-6s | Dist: %6.1f km | Status: %s%n",
                        s.getRollNumber(), s.getName(), s.getGender(), s.getDistanceFromCollege(),
                        s.hasRoomAllocated() ? "Allocated in " + s.getAllocatedHostelName() + " (Room " + s.getAllocatedRoomId() + ")"
                                : (s.isLocalite() ? "Localite Waitlist (<=30km)" : "Eligible Priority Queue (>30km)")));
            }
        }
        sb.append("========================================================================\n");
        return sb.toString();
    }

    public boolean exportAuditReportToFile(String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.print(generateSystemAuditReport());
            return true;
        } catch (IOException e) {
            System.err.println("Error saving report: " + e.getMessage());
            return false;
        }
    }

    // --- PERSISTENCE ---
    public synchronized void saveDataToDisk() {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(hostels);
            oos.writeObject(new ArrayList<>(studentsByRollNumber.values()));
            oos.writeObject(new ArrayList<>(staffByEmployeeId.values()));
            oos.writeObject(waitlistLocalites);
            oos.writeObject(new ArrayList<>(allocationQueue));
        } catch (IOException e) {
            // Log if needed
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void loadDataFromDisk() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            List<Hostel> loadedHostels = (List<Hostel>) ois.readObject();
            List<Student> loadedStudents = (List<Student>) ois.readObject();
            List<Staff> loadedStaff = (List<Staff>) ois.readObject();
            List<Student> loadedWaitlist = (List<Student>) ois.readObject();
            List<Student> loadedQueue = (List<Student>) ois.readObject();

            this.hostels = (loadedHostels != null) ? loadedHostels : new ArrayList<>();
            this.studentsByRollNumber = new LinkedHashMap<>();
            if (loadedStudents != null) {
                for (Student s : loadedStudents) {
                    this.studentsByRollNumber.put(s.getRollNumber().toUpperCase(), s);
                }
            }
            this.staffByEmployeeId = new LinkedHashMap<>();
            if (loadedStaff != null) {
                for (Staff st : loadedStaff) {
                    this.staffByEmployeeId.put(st.getEmployeeId().toUpperCase(), st);
                }
            }
            this.waitlistLocalites = (loadedWaitlist != null) ? loadedWaitlist : new ArrayList<>();
            this.allocationQueue = new LinkedList<>();
            if (loadedQueue != null) {
                this.allocationQueue.addAll(loadedQueue);
            }
        } catch (Exception e) {
            // Fallback fresh
        }
    }

    public void clearAllData() {
        hostels.clear();
        studentsByRollNumber.clear();
        staffByEmployeeId.clear();
        waitlistLocalites.clear();
        allocationQueue.clear();
        File file = new File(DATA_FILE);
        if (file.exists()) {
            file.delete();
        }
    }
}
