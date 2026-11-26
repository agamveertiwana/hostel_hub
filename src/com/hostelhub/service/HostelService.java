package com.hostelhub.service;

import com.hostelhub.exception.*;
import com.hostelhub.model.*;
import com.hostelhub.storage.DataInitializer;
import com.hostelhub.storage.FileStorageService;
import com.hostelhub.storage.StorageEngine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Core business service orchestrating all operations:
 * - Room Allocation with 30 km Rule & Custom Exceptions
 * - Staff Management (Wardens & Caretakers)
 * - Complaints & Maintenance
 * - Gate Passes (Day out / Night out)
 * - Fee Payments & Refunds (Strategy Pattern)
 * - Transparent CSV File Persistence
 */
public class HostelService {
    private StorageEngine storage;
    private List<Hostel> hostels;
    private Map<String, Student> studentsByRollNumber;
    private Map<String, Staff> staffByEmployeeId;
    private List<Complaint> complaints;
    private List<GatePass> gatePasses;
    private List<PaymentTransaction> payments;

    private List<Student> waitlistLocalites; // Distance <= 30 km
    private Queue<Student> allocationQueue;  // Distance > 30 km (FCFS)

    public HostelService() {
        this.storage = new FileStorageService();
        this.hostels = new ArrayList<>();
        this.studentsByRollNumber = new LinkedHashMap<>();
        this.staffByEmployeeId = new LinkedHashMap<>();
        this.complaints = new ArrayList<>();
        this.gatePasses = new ArrayList<>();
        this.payments = new ArrayList<>();
        this.waitlistLocalites = new ArrayList<>();
        this.allocationQueue = new LinkedList<>();

        // Ensure default data exists on disk
        DataInitializer.initializeDefaultDataIfEmpty(storage);

        // Load all data from transparent CSV files
        loadAllData();
    }

    public void loadAllData() {
        this.staffByEmployeeId.clear();
        for (Staff s : storage.loadStaff()) {
            this.staffByEmployeeId.put(s.getEmployeeId().toUpperCase(), s);
        }

        this.hostels = storage.loadHostels();

        this.studentsByRollNumber.clear();
        this.waitlistLocalites.clear();
        this.allocationQueue.clear();
        for (Student s : storage.loadStudents()) {
            this.studentsByRollNumber.put(s.getRollNumber().toUpperCase(), s);
            if (!s.hasRoomAllocated()) {
                if (s.isLocalite()) {
                    this.waitlistLocalites.add(s);
                } else {
                    this.allocationQueue.add(s);
                }
            }
        }

        this.complaints = storage.loadComplaints();
        this.gatePasses = storage.loadGatePasses();
        this.payments = storage.loadPayments();
    }

    public void persistAll() {
        storage.saveHostels(hostels);
        storage.saveStaff(staffByEmployeeId.values());
        storage.saveStudents(studentsByRollNumber.values());
        storage.saveComplaints(complaints);
        storage.saveGatePasses(gatePasses);
        storage.savePayments(payments);
    }

    // --- HOSTEL & ROOM OPERATIONS ---

    public void addHostel(Hostel hostel) throws DuplicateRecordException {
        if (findHostelById(hostel.getHostelId()) != null) {
            throw new DuplicateRecordException("Hostel", hostel.getHostelId());
        }
        hostels.add(hostel);
        persistAll();
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

    // --- STAFF OPERATIONS ---

    public void registerStaff(Staff staff) throws DuplicateRecordException {
        String empKey = staff.getEmployeeId().trim().toUpperCase();
        if (staffByEmployeeId.containsKey(empKey)) {
            throw new DuplicateRecordException("Staff", empKey);
        }
        staffByEmployeeId.put(empKey, staff);
        persistAll();
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
        if (staff == null) return "ERROR: Staff ID '" + employeeId + "' not found!";
        if (!(staff instanceof Warden)) return "ERROR: " + staff.getName() + " is not a Warden!";

        Hostel hostel = findHostelById(hostelId);
        if (hostel == null) return "ERROR: Hostel ID '" + hostelId + "' not found!";

        Warden w = (Warden) staff;
        w.setAssignedHostelName(hostel.getName());
        hostel.setWardenEmployeeId(w.getEmployeeId());
        persistAll();
        return "SUCCESS: Assigned Warden " + w.getName() + " to " + hostel.getName() + "!";
    }

    public String assignCaretakerToFloor(String employeeId, String hostelId, int floorNumber) {
        Staff staff = findStaffByEmployeeId(employeeId);
        if (staff == null) return "ERROR: Staff ID '" + employeeId + "' not found!";
        if (!(staff instanceof Caretaker)) return "ERROR: " + staff.getName() + " is not a Caretaker!";

        Hostel hostel = findHostelById(hostelId);
        if (hostel == null) return "ERROR: Hostel ID '" + hostelId + "' not found!";

        Floor floor = hostel.getFloor(floorNumber);
        if (floor == null) return "ERROR: Floor " + floorNumber + " not found in " + hostel.getName() + "!";

        Caretaker ct = (Caretaker) staff;
        ct.setAssignedHostelName(hostel.getName());
        ct.setAssignedFloorNumber(floorNumber);
        floor.setCaretakerEmployeeId(ct.getEmployeeId());
        persistAll();
        return "SUCCESS: Assigned Caretaker " + ct.getName() + " to " + hostel.getName() + " (Floor " + floorNumber + ")!";
    }

    // --- STUDENT OPERATIONS & 30KM PRIORITY ---

    public boolean registerStudent(Student student) throws DuplicateRecordException {
        String roll = student.getRollNumber().trim().toUpperCase();
        if (studentsByRollNumber.containsKey(roll)) {
            throw new DuplicateRecordException("Student", roll);
        }

        studentsByRollNumber.put(roll, student);
        if (student.isLocalite()) {
            waitlistLocalites.add(student);
        } else {
            allocationQueue.add(student);
        }
        persistAll();
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

    // --- ALLOCATION LOGIC WITH CUSTOM EXCEPTIONS ---

    public String allocateRoom(String rollNumber, String hostelId, String roomId)
            throws StudentNotFoundException, RoomFullException, IneligibleDistanceException, HostelException {

        Student student = findStudentByRoll(rollNumber);
        if (student == null) {
            throw new StudentNotFoundException(rollNumber);
        }

        if (student.hasRoomAllocated()) {
            throw new HostelException("Student is already allocated to Room " + student.getAllocatedRoomId() + " in " + student.getAllocatedHostelName() + "!");
        }

        Hostel hostel = findHostelById(hostelId);
        if (hostel == null) {
            throw new HostelException("Hostel ID '" + hostelId + "' does not exist!");
        }

        if (student.getGender() != hostel.getAllowedGender()) {
            throw new HostelException("Gender mismatch! " + student.getName() + " is " + student.getGender()
                    + ", but " + hostel.getName() + " is for " + hostel.getAllowedGender() + " students only!");
        }

        Room room = hostel.findRoom(roomId);
        if (room == null) {
            throw new HostelException("Room '" + roomId + "' does not exist in " + hostel.getName() + "!");
        }

        if (room.isFull()) {
            throw new RoomFullException(roomId, room.getCapacity());
        }

        // 30 km rule check
        if (student.isLocalite() && !allocationQueue.isEmpty()) {
            throw new IneligibleDistanceException(student.getRollNumber(), student.getDistanceFromCollege(), 30.0);
        }

        boolean success = room.allocateStudent(student.getRollNumber());
        if (success) {
            student.setAllocatedHostelName(hostel.getName());
            student.setAllocatedRoomId(room.getRoomId());
            allocationQueue.remove(student);
            waitlistLocalites.remove(student);
            persistAll();
            return "SUCCESS: Allocated Room " + room.getRoomId() + " in " + hostel.getName()
                    + " to " + student.getName() + " (Fee: Rs. " + String.format("%.0f", room.calculateSemesterFee()) + ")!";
        }

        return "ERROR: Allocation failed.";
    }

    // --- COMPLAINTS & GRIEVANCE OPERATIONS ---

    public void fileComplaint(String roll, String hostelId, String roomId, ComplaintType type, ComplaintPriority priority, String description) {
        String cid = "CMP-" + (complaints.size() + 101);
        Complaint c = new Complaint(cid, roll, hostelId, roomId, type, priority, description);
        complaints.add(c);
        persistAll();
    }

    public List<Complaint> getAllComplaints() {
        return complaints;
    }

    public boolean resolveComplaint(String complaintId, String staffId, String notes) {
        for (Complaint c : complaints) {
            if (c.getComplaintId().equalsIgnoreCase(complaintId)) {
                c.resolve(staffId, notes);
                persistAll();
                return true;
            }
        }
        return false;
    }

    // --- GATE PASS OPERATIONS ---

    public void applyGatePass(GatePass pass) {
        gatePasses.add(pass);
        persistAll();
    }

    public List<GatePass> getAllGatePasses() {
        return gatePasses;
    }

    public boolean approveGatePass(String passId, String wardenId) {
        for (GatePass gp : gatePasses) {
            if (gp.getPassId().equalsIgnoreCase(passId)) {
                gp.approve(wardenId);
                persistAll();
                return true;
            }
        }
        return false;
    }

    // --- FEE PAYMENTS & REFUNDS (STRATEGY PATTERN) ---

    public Receipt processFeePayment(String roll, double amount, PaymentMode mode, String note) throws StudentNotFoundException {
        Student student = findStudentByRoll(roll);
        if (student == null) throw new StudentNotFoundException(roll);

        String txnId = "TXN-" + System.currentTimeMillis() % 100000;
        PaymentTransaction txn = new PaymentTransaction(txnId, roll, amount, mode, note);
        payments.add(txn);
        persistAll();

        String receiptNo = "REC-" + (payments.size() + 500);
        return new Receipt(receiptNo, txnId, roll, student.getName(),
                student.getAllocatedHostelName() == null ? "HostelHub" : student.getAllocatedHostelName(),
                student.getAllocatedRoomId() == null ? "N/A" : student.getAllocatedRoomId(), amount);
    }

    public double calculateVacatingRefund(double totalPaidFee, double securityDeposit, int monthsUsed, RefundPolicy policy) {
        return policy.calculateRefund(totalPaidFee, securityDeposit, monthsUsed, 6);
    }

    // --- AUDIT REPORT & TEXT EXPORT ---

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
        sb.append(String.format("Total Staff: %d | Total Students: %d (Allocated: %d | Queue: %d | Waitlist: %d)%n",
                staffByEmployeeId.size(), studentsByRollNumber.size(),
                (studentsByRollNumber.size() - allocationQueue.size() - waitlistLocalites.size()),
                allocationQueue.size(), waitlistLocalites.size()));
        sb.append(String.format("Total Complaints: %d | Gate Passes: %d | Payments Processed: %d%n%n",
                complaints.size(), gatePasses.size(), payments.size()));

        sb.append("---------------------------- STAFF ROSTER ------------------------------\n");
        for (Staff st : staffByEmployeeId.values()) {
            if (st instanceof Warden) {
                Warden w = (Warden) st;
                sb.append(String.format("ID: %-10s | %-16s | Role: %-12s | Hostel: %-22s | Shift: %s%n",
                        w.getEmployeeId(), w.getName(), w.getRole(),
                        (w.getAssignedHostelName() == null ? "Unassigned" : w.getAssignedHostelName()), w.getShift()));
            } else if (st instanceof Caretaker) {
                Caretaker ct = (Caretaker) st;
                sb.append(String.format("ID: %-10s | %-16s | Role: %-12s | Assigned: %s (Floor %d) | Shift: %s%n",
                        ct.getEmployeeId(), ct.getName(), ct.getRole(),
                        (ct.getAssignedHostelName() == null ? "Unassigned" : ct.getAssignedHostelName()),
                        ct.getAssignedFloorNumber(), ct.getShift()));
            }
        }
        sb.append("\n");

        sb.append("--------------------------- HOSTEL DETAILS -----------------------------\n");
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

        sb.append("--------------------------- STUDENT ROSTER -----------------------------\n");
        for (Student s : studentsByRollNumber.values()) {
            sb.append(String.format("Roll: %-10s | Name: %-16s | Gender: %-6s | Dist: %6.1f km | Status: %s%n",
                    s.getRollNumber(), s.getName(), s.getGender(), s.getDistanceFromCollege(),
                    s.hasRoomAllocated() ? "Allocated in " + s.getAllocatedHostelName() + " (Room " + s.getAllocatedRoomId() + ")"
                            : (s.isLocalite() ? "Localite Waitlist (<=30km)" : "Eligible Priority Queue (>30km)")));
        }
        sb.append("========================================================================\n");
        return sb.toString();
    }

    public boolean exportAuditReportToFile(String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.print(generateSystemAuditReport());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void clearAllData() {
        hostels.clear();
        studentsByRollNumber.clear();
        staffByEmployeeId.clear();
        complaints.clear();
        gatePasses.clear();
        payments.clear();
        waitlistLocalites.clear();
        allocationQueue.clear();

        File dir = new File("data");
        if (dir.exists()) {
            for (File f : dir.listFiles()) {
                if (f.getName().endsWith(".csv") || f.getName().endsWith(".dat")) {
                    f.delete();
                }
            }
        }
    }
}
