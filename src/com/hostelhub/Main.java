package com.hostelhub;

import com.hostelhub.exception.*;
import com.hostelhub.model.*;
import com.hostelhub.service.*;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Scanner;

/**
 * Interactive Terminal-based UI for HostelHub.
 * Fully modularized across 35+ classes, demonstrating:
 * - Real-world Staff Hierarchy (Pre-loaded Wardens & Floor Caretakers).
 * - Custom Exceptions & Robust Business Logic (30 km rule).
 * - Gate Passes, Complaints, and Fee Settlements.
 * - Strategy Pattern for Mid-Semester Refund Calculations.
 * - Transparent CSV File Persistence in the data/ folder.
 */
public class Main {
    private static HostelService service = new HostelService();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("      HOSTELHUB - SMART HOSTEL MANAGEMENT       ");
        System.out.println("=================================================");

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readIntInput("Enter your choice (1-12): ");

            switch (choice) {
                case 1:
                    handleCreateHostel();
                    break;
                case 2:
                    handleRegisterStudent();
                    break;
                case 3:
                    handleAllocateRoom();
                    break;
                case 4:
                    handleStaffManagement();
                    break;
                case 5:
                    handleGatePassOperations();
                    break;
                case 6:
                    handleComplaintOperations();
                    break;
                case 7:
                    handleFeeAndRefunds();
                    break;
                case 8:
                    handleViewHostels();
                    break;
                case 9:
                    handleViewStudents();
                    break;
                case 10:
                    handleSystemAuditReport();
                    break;
                case 11:
                    handleResetData();
                    break;
                case 12:
                    System.out.println("\nThank you for using HostelHub! Goodbye.");
                    running = false;
                    break;
                default:
                    System.out.println("[!] Invalid choice. Please enter a number between 1 and 12.");
            }
        }
        scanner.close();
    }

    private static void printMainMenu() {
        System.out.println("\n---------------- MAIN MENU ----------------");
        System.out.println("1.  Add / Configure a Hostel (Auto-Generate or Manual)");
        System.out.println("2.  Register a Student (Inputs Roll No & Distance)");
        System.out.println("3.  Allocate Room to Student (Custom Exceptions & 30km Rule)");
        System.out.println("4.  Staff Management (Pre-loaded Wardens, Floor Caretakers)");
        System.out.println("5.  Gate Pass Operations (Day/Night Outpass & Approvals)");
        System.out.println("6.  Complaints & Maintenance (Log & Resolve Grievances)");
        System.out.println("7.  Fees & Refund Calculator (Strategy Pattern: Mid-Sem Checkout)");
        System.out.println("8.  View All Hostels & Live Room Occupancy");
        System.out.println("9.  View All Students & Queue Status");
        System.out.println("10. Full System Audit & Export Report");
        System.out.println("11. Reset / Clear All Data (Start fresh)");
        System.out.println("12. Exit");
        System.out.println("-------------------------------------------");
    }

    // --- 1. HOSTEL SETUP ---
    private static void handleCreateHostel() {
        System.out.println("\n--- ADD NEW HOSTEL (Type 0 anytime to cancel/go back) ---");

        System.out.print("Enter Hostel ID (e.g., BH2, GH2) [0 to Cancel]: ");
        String id = scanner.nextLine().trim();
        if (id.equals("0")) return;

        if (service.findHostelById(id) != null) {
            System.out.println("[!] A hostel with ID '" + id + "' already exists!");
            return;
        }

        System.out.print("Enter Hostel Name [0 to Cancel]: ");
        String name = scanner.nextLine().trim();
        if (name.equals("0")) return;

        System.out.print("Allowed Gender (1 for MALE, 2 for FEMALE, 0 to Cancel): ");
        int genderChoice = readIntInput("");
        if (genderChoice == 0) return;
        Gender gender = (genderChoice == 2) ? Gender.FEMALE : Gender.MALE;

        int numFloors = readIntInput("How many floors does this hostel have? [0 to Cancel]: ");
        if (numFloors <= 0) return;

        Hostel hostel = new Hostel(id, name, gender);

        System.out.println("\nHow would you like to configure rooms?");
        System.out.println("  [1] Auto-Generate standard rooms for all " + numFloors + " floors (Fast)");
        System.out.println("  [2] Configure every room manually one-by-one");
        System.out.println("  [0] Cancel and go back");
        int setupMode = readIntInput("Select mode (0-2): ");
        if (setupMode == 0) return;

        if (setupMode == 1) {
            int roomsPerFloor = readIntInput("How many rooms per floor? (e.g., 5): ");
            int capacity = readIntInput("Seater capacity (2, 3, or 4): ");
            int acChoice = readIntInput("Room Type (1 for AC, 2 for Non-AC): ");
            RoomType roomType = (acChoice == 1) ? RoomType.AC : RoomType.NON_AC;
            int washChoice = readIntInput("Washroom (1 for Attached, 2 for Common): ");
            WashroomType washType = (washChoice == 1) ? WashroomType.ATTACHED : WashroomType.COMMON;

            for (int f = 1; f <= numFloors; f++) {
                Floor floor = new Floor(f);
                for (int r = 1; r <= roomsPerFloor; r++) {
                    String roomId = String.format("%d%02d", f, r);
                    floor.addRoom(new Room(roomId, f, capacity, roomType, washType));
                }
                hostel.addFloor(floor);
            }
            try {
                service.addHostel(hostel);
                System.out.printf("%n[SUCCESS] %s created with %d floors and %d rooms auto-generated!%n",
                        hostel.getName(), numFloors, (numFloors * roomsPerFloor));
            } catch (DuplicateRecordException e) {
                System.out.println("[ERROR] " + e.getMessage());
            }
            return;
        }

        // Manual mode
        for (int f = 1; f <= numFloors; f++) {
            Floor floor = new Floor(f);
            System.out.printf("%nConfiguring Floor %d:%n", f);
            int numRooms = readIntInput("  How many rooms on Floor " + f + "?: ");
            for (int r = 1; r <= numRooms; r++) {
                System.out.print("    Enter Room Number/ID (e.g., " + (f * 100 + r) + ") [0 to Stop]: ");
                String roomId = scanner.nextLine().trim();
                if (roomId.equals("0")) break;

                int capacity = readIntInput("    Capacity (2, 3, or 4): ");
                int acChoice = readIntInput("    Type (1 for AC, 2 for Non-AC): ");
                RoomType roomType = (acChoice == 1) ? RoomType.AC : RoomType.NON_AC;
                int washChoice = readIntInput("    Washroom (1 for Attached, 2 for Common): ");
                WashroomType washType = (washChoice == 1) ? WashroomType.ATTACHED : WashroomType.COMMON;

                floor.addRoom(new Room(roomId, f, capacity, roomType, washType));
            }
            hostel.addFloor(floor);
        }

        try {
            service.addHostel(hostel);
            System.out.println("\n[SUCCESS] " + hostel.getName() + " added successfully!");
        } catch (DuplicateRecordException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    // --- 2. STUDENT REGISTRATION ---
    private static void handleRegisterStudent() {
        System.out.println("\n--- REGISTER NEW STUDENT ---");
        System.out.print("Enter Roll Number (e.g., 2024CS15) [0 to Cancel]: ");
        String roll = scanner.nextLine().trim();
        if (roll.equals("0")) return;

        System.out.print("Enter Full Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter Department: ");
        String dept = scanner.nextLine().trim();

        int year = readIntInput("Enter Study Year (1-4): ");

        System.out.print("Enter Gender (1 for MALE, 2 for FEMALE): ");
        Gender gender = (readIntInput("") == 2) ? Gender.FEMALE : Gender.MALE;

        System.out.print("Enter Contact Phone: ");
        String phone = scanner.nextLine().trim();

        System.out.print("Enter Email Address: ");
        String email = scanner.nextLine().trim();

        double distance = readDoubleInput("Enter Home Distance from College (in Kilometers): ");

        Student student = new Student(roll, name, phone, email, gender, dept, year, distance);
        try {
            service.registerStudent(student);
            System.out.println("\n[SUCCESS] Student registered successfully!");
            if (student.isLocalite()) {
                System.out.printf("[RULE CHECK] Home distance is %.2f km (<= 30km). Placed in LOCALITE WAITLIST.%n", distance);
            } else {
                System.out.printf("[RULE CHECK] Home distance is %.2f km (> 30km). Placed in ELIGIBLE ALLOCATION QUEUE.%n", distance);
            }
        } catch (DuplicateRecordException e) {
            System.out.println("\n[ERROR] " + e.getMessage());
        }
    }

    // --- 3. ROOM ALLOCATION (DEMONSTRATING CUSTOM EXCEPTIONS) ---
    private static void handleAllocateRoom() {
        System.out.println("\n--- ALLOCATE ROOM ---");
        System.out.print("Enter Student Roll Number [0 to Cancel]: ");
        String roll = scanner.nextLine().trim();
        if (roll.equals("0")) return;

        System.out.print("Enter Target Hostel ID (e.g., BH1, GH1): ");
        String hostelId = scanner.nextLine().trim();

        System.out.print("Enter Target Room Number (e.g., 101, G101): ");
        String roomId = scanner.nextLine().trim();

        try {
            String result = service.allocateRoom(roll, hostelId, roomId);
            System.out.println("\n" + result);
        } catch (StudentNotFoundException | RoomFullException | IneligibleDistanceException e) {
            System.out.println("\n[ALLOCATION REJECTED] " + e.getMessage());
        } catch (HostelException e) {
            System.out.println("\n[ERROR] " + e.getMessage());
        }
    }

    // --- 4. STAFF MANAGEMENT (PRE-LOADED WARDENS & CARETAKERS) ---
    private static void handleStaffManagement() {
        System.out.println("\n========== STAFF MANAGEMENT (PRE-LOADED DATA ACTIVE) ==========");
        System.out.println("1. View All Staff Members & Assigned Duties");
        System.out.println("2. Register New Warden & Assign to Hostel");
        System.out.println("3. Register New Caretaker & Assign to Floor");
        System.out.println("0. Back to Main Menu");
        System.out.println("===============================================================");

        int choice = readIntInput("Select action (0-3): ");
        switch (choice) {
            case 1:
                for (Staff st : service.getAllStaff()) {
                    st.displayDetails();
                }
                break;
            case 2:
                registerWardenFlow();
                break;
            case 3:
                registerCaretakerFlow();
                break;
            default:
                break;
        }
    }

    private static void registerWardenFlow() {
        System.out.print("Enter Employee ID (e.g. EMP-WRD-03): ");
        String empId = scanner.nextLine().trim();
        System.out.print("Enter Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter Phone: ");
        String phone = scanner.nextLine().trim();
        System.out.print("Enter Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter Gender (1 for MALE, 2 for FEMALE): ");
        Gender gender = (readIntInput("") == 2) ? Gender.FEMALE : Gender.MALE;
        System.out.print("Enter Shift: ");
        String shift = scanner.nextLine().trim();
        System.out.print("Assign to Hostel ID (e.g. BH1): ");
        String hId = scanner.nextLine().trim();

        Warden w = new Warden(empId, name, phone, email, gender, empId, shift, null, false);
        try {
            service.registerStaff(w);
            if (!hId.isEmpty()) {
                System.out.println(service.assignWardenToHostel(empId, hId));
            }
        } catch (DuplicateRecordException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private static void registerCaretakerFlow() {
        System.out.print("Enter Employee ID (e.g. EMP-CT-06): ");
        String empId = scanner.nextLine().trim();
        System.out.print("Enter Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter Phone: ");
        String phone = scanner.nextLine().trim();
        System.out.print("Enter Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter Gender (1 for MALE, 2 for FEMALE): ");
        Gender gender = (readIntInput("") == 2) ? Gender.FEMALE : Gender.MALE;
        System.out.print("Enter Shift: ");
        String shift = scanner.nextLine().trim();
        System.out.print("Assign to Hostel ID: ");
        String hId = scanner.nextLine().trim();
        int floor = readIntInput("Assign to Floor Number: ");

        Caretaker ct = new Caretaker(empId, name, phone, email, gender, empId, shift, null, floor);
        try {
            service.registerStaff(ct);
            System.out.println(service.assignCaretakerToFloor(empId, hId, floor));
        } catch (DuplicateRecordException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    // --- 5. GATE PASS OPERATIONS ---
    private static void handleGatePassOperations() {
        System.out.println("\n========== GATE PASS (OUTPASS) PORTAL ==========");
        System.out.println("1. Apply for Day Out Pass");
        System.out.println("2. Apply for Night Out Pass (Overnight stay)");
        System.out.println("3. Warden Approval / Review Panel");
        System.out.println("4. View All Gate Passes");
        System.out.println("0. Back");
        System.out.println("================================================");

        int choice = readIntInput("Select option (0-4): ");
        switch (choice) {
            case 1:
                System.out.print("Enter Student Roll Number: ");
                String roll = scanner.nextLine().trim();
                System.out.print("Enter Outing Reason: ");
                String reason = scanner.nextLine().trim();
                String passId = "GP-DAY-" + (service.getAllGatePasses().size() + 101);
                DayOutPass dayPass = new DayOutPass(passId, roll, reason, LocalDateTime.now(), LocalDateTime.now().plusHours(4));
                service.applyGatePass(dayPass);
                System.out.println("[SUCCESS] Day Pass '" + passId + "' applied! Status: PENDING warden review.");
                break;
            case 2:
                System.out.print("Enter Student Roll Number: ");
                String nRoll = scanner.nextLine().trim();
                System.out.print("Enter Reason for Night Out: ");
                String nReason = scanner.nextLine().trim();
                System.out.print("Enter Destination Address: ");
                String dest = scanner.nextLine().trim();
                System.out.print("Enter Emergency Guardian Contact: ");
                String contact = scanner.nextLine().trim();
                String nPassId = "GP-NGT-" + (service.getAllGatePasses().size() + 101);
                NightOutPass nightPass = new NightOutPass(nPassId, nRoll, nReason, LocalDateTime.now(),
                        LocalDateTime.now().plusDays(2), dest, contact);
                nightPass.verifyParentConsent();
                service.applyGatePass(nightPass);
                System.out.println("[SUCCESS] Night Out Pass '" + nPassId + "' applied with Guardian verification! Status: PENDING.");
                break;
            case 3:
                System.out.print("Enter Gate Pass ID to Approve: ");
                String pId = scanner.nextLine().trim();
                System.out.print("Enter Warden Employee ID (e.g. EMP-WRD-01): ");
                String wId = scanner.nextLine().trim();
                if (service.approveGatePass(pId, wId)) {
                    System.out.println("[SUCCESS] Gate Pass '" + pId + "' approved by Warden " + wId + "!");
                } else {
                    System.out.println("[ERROR] Pass ID not found.");
                }
                break;
            case 4:
                System.out.println("\n--- ALL GATE PASSES ---");
                for (GatePass gp : service.getAllGatePasses()) {
                    System.out.printf("Pass ID: %-12s | Roll: %-10s | Type: %-10s | Status: %-10s | Reason: %s%n",
                            gp.getPassId(), gp.getStudentRollNumber(), gp.getPassType(), gp.getStatus(), gp.getReason());
                }
                break;
            default:
                break;
        }
    }

    // --- 6. COMPLAINTS & GRIEVANCES ---
    private static void handleComplaintOperations() {
        System.out.println("\n========== COMPLAINT & MAINTENANCE PORTAL ==========");
        System.out.println("1. File a Maintenance Complaint");
        System.out.println("2. Resolve / Update Complaint Status (Staff)");
        System.out.println("3. View All Complaints");
        System.out.println("0. Back");
        System.out.println("====================================================");

        int choice = readIntInput("Select option (0-3): ");
        switch (choice) {
            case 1:
                System.out.print("Enter Student Roll Number: ");
                String roll = scanner.nextLine().trim();
                System.out.print("Enter Hostel ID: ");
                String hId = scanner.nextLine().trim();
                System.out.print("Enter Room Number: ");
                String rId = scanner.nextLine().trim();

                System.out.println("Complaint Type: 1. Electrical, 2. Plumbing, 3. Cleanliness, 4. Mess Food, 5. Other");
                int tChoice = readIntInput("Select type (1-5): ");
                ComplaintType type = (tChoice == 1) ? ComplaintType.ELECTRICAL : (tChoice == 2) ? ComplaintType.PLUMBING :
                        (tChoice == 3) ? ComplaintType.CLEANLINESS : (tChoice == 4) ? ComplaintType.MESS_FOOD : ComplaintType.OTHER;

                System.out.println("Priority: 1. Low, 2. Medium, 3. High, 4. Urgent");
                int pChoice = readIntInput("Select priority (1-4): ");
                ComplaintPriority priority = (pChoice == 4) ? ComplaintPriority.URGENT : (pChoice == 3) ? ComplaintPriority.HIGH :
                        (pChoice == 2) ? ComplaintPriority.MEDIUM : ComplaintPriority.LOW;

                System.out.print("Enter Description of the issue: ");
                String desc = scanner.nextLine().trim();

                service.fileComplaint(roll, hId, rId, type, priority, desc);
                System.out.println("[SUCCESS] Complaint logged and dispatched to Floor Caretaker!");
                break;
            case 2:
                System.out.print("Enter Complaint ID (e.g. CMP-101): ");
                String cId = scanner.nextLine().trim();
                System.out.print("Enter Resolving Staff ID: ");
                String sId = scanner.nextLine().trim();
                System.out.print("Enter Resolution Action Notes: ");
                String notes = scanner.nextLine().trim();
                if (service.resolveComplaint(cId, sId, notes)) {
                    System.out.println("[SUCCESS] Complaint '" + cId + "' marked as RESOLVED!");
                } else {
                    System.out.println("[ERROR] Complaint ID not found.");
                }
                break;
            case 3:
                System.out.println("\n--- COMPLAINT LOGS ---");
                for (Complaint c : service.getAllComplaints()) {
                    System.out.printf("ID: %-8s | Room: %-6s | Type: %-12s | Priority: %-7s | Status: %-11s | Desc: %s%n",
                            c.getComplaintId(), c.getRoomId(), c.getType(), c.getPriority(), c.getStatus(), c.getDescription());
                }
                break;
            default:
                break;
        }
    }

    // --- 7. FEES & REFUNDS (STRATEGY PATTERN) ---
    private static void handleFeeAndRefunds() {
        System.out.println("\n========== FEE SETTLEMENT & REFUND ENGINE ==========");
        System.out.println("1. Pay Semester Fee (Generates Official Receipt)");
        System.out.println("2. Calculate Mid-Semester Vacating Refund (Strategy Pattern)");
        System.out.println("3. Calculate Standard End-of-Semester Refund");
        System.out.println("0. Back");
        System.out.println("====================================================");

        int choice = readIntInput("Select option (0-3): ");
        switch (choice) {
            case 1:
                System.out.print("Enter Student Roll Number: ");
                String roll = scanner.nextLine().trim();
                double amount = readDoubleInput("Enter Payment Amount (Rs.): ");
                System.out.println("Payment Mode: 1. UPI, 2. Card, 3. Net Banking, 4. Cash");
                int mChoice = readIntInput("Select mode (1-4): ");
                PaymentMode mode = (mChoice == 1) ? PaymentMode.UPI : (mChoice == 2) ? PaymentMode.CARD :
                        (mChoice == 3) ? PaymentMode.NET_BANKING : PaymentMode.CASH;

                try {
                    Receipt receipt = service.processFeePayment(roll, amount, mode, "Semester Fee");
                    System.out.println("\n" + receipt.printReceipt());
                } catch (StudentNotFoundException e) {
                    System.out.println("[ERROR] " + e.getMessage());
                }
                break;
            case 2:
                System.out.println("\n--- MID-SEMESTER VACATING REFUND CALCULATOR ---");
                double paidFee = readDoubleInput("Enter Total Semester Fee Paid (Rs.): ");
                double deposit = readDoubleInput("Enter Security Deposit Component (Rs.): ");
                int monthsUsed = readIntInput("How many months stayed so far? (e.g. 2): ");

                RefundPolicy midSemPolicy = new MidSemesterRefundPolicy();
                double refund = service.calculateVacatingRefund(paidFee, deposit, monthsUsed, midSemPolicy);

                System.out.println("\nPolicy Applied: " + midSemPolicy.getPolicyName());
                System.out.printf("Total Refund Payable to Student: Rs. %.2f%n", refund);
                break;
            case 3:
                System.out.println("\n--- STANDARD END-OF-SEMESTER REFUND ---");
                double sDeposit = readDoubleInput("Enter Security Deposit Component (Rs.): ");
                RefundPolicy standardPolicy = new StandardRefundPolicy();
                double sRefund = service.calculateVacatingRefund(0, sDeposit, 6, standardPolicy);
                System.out.println("Policy Applied: " + standardPolicy.getPolicyName());
                System.out.printf("Total Security Deposit Refund: Rs. %.2f%n", sRefund);
                break;
            default:
                break;
        }
    }

    // --- 8. VIEW HOSTELS ---
    private static void handleViewHostels() {
        System.out.println("\n--- ALL HOSTEL BUILDINGS & ROOM OCCUPANCY ---");
        for (Hostel h : service.getAllHostels()) {
            h.displayHostelSummary();
            System.out.println();
        }
    }

    // --- 9. VIEW STUDENTS ---
    private static void handleViewStudents() {
        System.out.println("\n--- REGISTERED STUDENTS & QUEUE STATUS ---");
        for (Student s : service.getAllStudents()) {
            s.displayDetails();
        }
        System.out.printf("%nSummary: %d Total Students | %d in Priority Queue (>30km) | %d in Localite Waitlist (<=30km)%n",
                service.getAllStudents().size(),
                service.getAllocationQueue().size(),
                service.getWaitlistLocalites().size());
    }

    // --- 10. SYSTEM AUDIT REPORT ---
    private static void handleSystemAuditReport() {
        System.out.println("\nGenerating System Audit Report...\n");
        String report = service.generateSystemAuditReport();
        System.out.println(report);

        String exportPath = "data" + File.separator + "system_report.txt";
        boolean saved = service.exportAuditReportToFile(exportPath);
        if (saved) {
            System.out.println("[SUCCESS] Report exported to: " + exportPath);
            System.out.println("Also check the 'data/' folder to inspect: staff.csv, hostels.csv, rooms.csv, students.csv!");
        }
    }

    // --- 11. RESET DATA ---
    private static void handleResetData() {
        System.out.print("Type 'YES' to clear all data and reseed default records: ");
        if (scanner.nextLine().trim().equalsIgnoreCase("YES")) {
            service.clearAllData();
            service = new HostelService(); // re-seeds
            System.out.println("[SUCCESS] System reset to default seeded state!");
        }
    }

    // --- HELPER INPUT METHODS ---
    private static int readIntInput(String prompt) {
        while (true) {
            if (!prompt.isEmpty()) System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("[!] Enter a valid number: ");
            }
        }
    }

    private static double readDoubleInput(String prompt) {
        while (true) {
            if (!prompt.isEmpty()) System.out.print(prompt);
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("[!] Enter a valid decimal number: ");
            }
        }
    }
}
