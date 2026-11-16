package com.hostelhub;

import com.hostelhub.model.*;
import com.hostelhub.service.HostelService;

import java.io.File;
import java.util.Scanner;

/**
 * Interactive Terminal-based UI for HostelHub.
 * Features:
 * - Dynamic Hostel & Room configuration.
 * - Dynamic Staff Management (Warden & Floor-level Caretakers).
 * - Student registration with 30km distance priority check.
 * - Automatic data persistence to disk.
 * - Full system audit report export.
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
            int choice = readIntInput("Enter your choice (1-10): ");

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
                    handleViewHostels();
                    break;
                case 6:
                    handleViewStudents();
                    break;
                case 7:
                    handleLoadSampleData();
                    break;
                case 8:
                    handleSystemAuditReport();
                    break;
                case 9:
                    handleResetData();
                    break;
                case 10:
                    System.out.println("\nThank you for using HostelHub! Goodbye.");
                    running = false;
                    break;
                default:
                    System.out.println("[!] Invalid choice. Please enter a number between 1 and 10.");
            }
        }
        scanner.close();
    }

    private static void printMainMenu() {
        System.out.println("\n---------------- MAIN MENU ----------------");
        System.out.println("1.  Add / Configure a Hostel (Auto-Generate or Manual)");
        System.out.println("2.  Register a Student (Inputs Roll No & Distance)");
        System.out.println("3.  Allocate Room to Student (Checks 30km Rule, Gender, Capacity)");
        System.out.println("4.  Staff Management (Wardens, Floor Caretakers & Duties)");
        System.out.println("5.  View All Hostels & Live Room Occupancy");
        System.out.println("6.  View All Students & Queue Status");
        System.out.println("7.  Load Demo Data (Hostels + Staff + Students)");
        System.out.println("8.  Full System Audit & Export Report");
        System.out.println("9.  Reset / Clear All Data (Start fresh)");
        System.out.println("10. Exit");
        System.out.println("-------------------------------------------");
    }

    // --- 1. DYNAMIC HOSTEL CREATION ---
    private static void handleCreateHostel() {
        System.out.println("\n--- ADD NEW HOSTEL (Type 0 anytime to cancel/go back) ---");

        System.out.print("Enter Hostel ID (e.g., BH1, GH1, H1) [0 to Cancel]: ");
        String id = scanner.nextLine().trim();
        if (id.equals("0")) {
            System.out.println("[Cancelled] Returned to Main Menu.");
            return;
        }

        if (service.findHostelById(id) != null) {
            System.out.println("[!] A hostel with ID '" + id + "' already exists! Please choose another ID.");
            return;
        }

        System.out.print("Enter Hostel Name [0 to Cancel]: ");
        String name = scanner.nextLine().trim();
        if (name.equals("0")) {
            System.out.println("[Cancelled] Returned to Main Menu.");
            return;
        }

        System.out.print("Allowed Gender (1 for MALE, 2 for FEMALE, 0 to Cancel): ");
        int genderChoice = readIntInput("");
        if (genderChoice == 0) {
            System.out.println("[Cancelled] Returned to Main Menu.");
            return;
        }
        Gender gender = (genderChoice == 2) ? Gender.FEMALE : Gender.MALE;

        int numFloors = readIntInput("How many floors does this hostel have? [0 to Cancel]: ");
        if (numFloors <= 0) {
            System.out.println("[Cancelled] Returned to Main Menu.");
            return;
        }

        Hostel hostel = new Hostel(id, name, gender);

        System.out.println("\nHow would you like to configure rooms?");
        System.out.println("  [1] Auto-Generate standard rooms for all " + numFloors + " floors (Fast)");
        System.out.println("  [2] Configure every room manually one-by-one");
        System.out.println("  [0] Cancel and go back");
        int setupMode = readIntInput("Select mode (0-2): ");

        if (setupMode == 0) {
            System.out.println("[Cancelled] Returned to Main Menu.");
            return;
        }

        if (setupMode == 1) {
            int roomsPerFloor = readIntInput("How many rooms per floor? (e.g., 5 or 10): ");
            int capacity = readIntInput("Seater capacity for all rooms (2, 3, or 4): ");
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
            service.addHostel(hostel);
            System.out.printf("%n[SUCCESS] %s (ID: %s) created with %d floors and %d total rooms auto-generated!%n",
                    hostel.getName(), hostel.getHostelId(), numFloors, (numFloors * roomsPerFloor));
            return;
        }

        // Manual mode
        for (int f = 1; f <= numFloors; f++) {
            Floor floor = new Floor(f);
            System.out.printf("%nConfiguring Floor %d (Type 0 for rooms to skip/finish floor):%n", f);
            int numRooms = readIntInput("  How many rooms on Floor " + f + "?: ");
            if (numRooms == 0) continue;

            for (int r = 1; r <= numRooms; r++) {
                System.out.printf("  --- Room %d of %d on Floor %d ---%n", r, numRooms, f);
                System.out.print("    Enter Room Number/ID (e.g., " + (f * 100 + r) + ") [0 to Stop]: ");
                String roomId = scanner.nextLine().trim();
                if (roomId.equals("0")) {
                    System.out.println("    [Stopped manual room entry for this floor]");
                    break;
                }

                int capacity = readIntInput("    Enter Seater Capacity (2, 3, or 4): ");
                int acChoice = readIntInput("    Room Type (1 for AC, 2 for Non-AC): ");
                RoomType roomType = (acChoice == 1) ? RoomType.AC : RoomType.NON_AC;

                int washChoice = readIntInput("    Washroom (1 for Attached, 2 for Common): ");
                WashroomType washroomType = (washChoice == 1) ? WashroomType.ATTACHED : WashroomType.COMMON;

                Room room = new Room(roomId, f, capacity, roomType, washroomType);
                floor.addRoom(room);
            }
            hostel.addFloor(floor);
        }

        service.addHostel(hostel);
        System.out.println("\n[SUCCESS] " + hostel.getName() + " (ID: " + hostel.getHostelId() + ") added successfully!");
    }

    // --- 2. DYNAMIC STUDENT REGISTRATION ---
    private static void handleRegisterStudent() {
        System.out.println("\n--- REGISTER NEW STUDENT (Type 0 anytime to cancel) ---");

        System.out.print("Enter Roll Number (e.g., 2024CS10) [0 to Cancel]: ");
        String roll = scanner.nextLine().trim();
        if (roll.equals("0")) {
            System.out.println("[Cancelled] Returned to Main Menu.");
            return;
        }

        if (service.findStudentByRoll(roll) != null) {
            System.out.println("[!] A student with Roll Number '" + roll + "' is already registered!");
            return;
        }

        System.out.print("Enter Full Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter Department (e.g., CSE, ECE, MECH): ");
        String dept = scanner.nextLine().trim();

        int year = readIntInput("Enter Study Year (1-4): ");

        System.out.print("Enter Gender (1 for MALE, 2 for FEMALE): ");
        int genderChoice = readIntInput("");
        Gender gender = (genderChoice == 2) ? Gender.FEMALE : Gender.MALE;

        System.out.print("Enter Contact Phone: ");
        String phone = scanner.nextLine().trim();

        System.out.print("Enter Email Address: ");
        String email = scanner.nextLine().trim();

        double distance = readDoubleInput("Enter Home Distance from College (in Kilometers): ");

        Student student = new Student(roll, name, phone, email, gender, dept, year, distance);
        boolean registered = service.registerStudent(student);

        if (registered) {
            System.out.println("\n[SUCCESS] Student registered successfully with Roll No: " + roll);
            if (student.isLocalite()) {
                System.out.printf("[RULE CHECK] Home distance is %.2f km (<= 30km). Placed in LOCALITE WAITLIST.%n", distance);
            } else {
                System.out.printf("[RULE CHECK] Home distance is %.2f km (> 30km). Placed in ELIGIBLE ALLOCATION QUEUE.%n", distance);
            }
        }
    }

    // --- 3. DYNAMIC ROOM ALLOCATION ---
    private static void handleAllocateRoom() {
        System.out.println("\n--- ALLOCATE ROOM ---");
        System.out.print("Enter Student Roll Number [0 to Cancel]: ");
        String roll = scanner.nextLine().trim();
        if (roll.equals("0")) return;

        System.out.print("Enter Target Hostel ID (e.g., BH1, GH1): ");
        String hostelId = scanner.nextLine().trim();

        System.out.print("Enter Target Room Number (e.g., 101, 201): ");
        String roomId = scanner.nextLine().trim();

        String result = service.allocateRoom(roll, hostelId, roomId);
        System.out.println("\n" + result);
    }

    // --- 4. STAFF MANAGEMENT (Wardens & Caretakers) ---
    private static void handleStaffManagement() {
        System.out.println("\n========== STAFF MANAGEMENT ==========");
        System.out.println("1. Register a Warden & Assign to Hostel");
        System.out.println("2. Register a Caretaker & Assign to Floor");
        System.out.println("3. View All Staff Members & Current Duties");
        System.out.println("0. Back to Main Menu");
        System.out.println("======================================");

        int choice = readIntInput("Select staff action (0-3): ");
        switch (choice) {
            case 1:
                registerWardenFlow();
                break;
            case 2:
                registerCaretakerFlow();
                break;
            case 3:
                viewStaffList();
                break;
            default:
                System.out.println("[Back] Returned to Main Menu.");
        }
    }

    private static void registerWardenFlow() {
        System.out.println("\n--- REGISTER WARDEN ---");
        System.out.print("Enter Employee ID (e.g. EMP-WRD-02) [0 to Cancel]: ");
        String empId = scanner.nextLine().trim();
        if (empId.equals("0")) return;

        System.out.print("Enter Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter Phone: ");
        String phone = scanner.nextLine().trim();

        System.out.print("Enter Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter Gender (1 for MALE, 2 for FEMALE): ");
        Gender gender = (readIntInput("") == 2) ? Gender.FEMALE : Gender.MALE;

        System.out.print("Enter Shift (e.g. Morning, Evening, Full-time): ");
        String shift = scanner.nextLine().trim();

        System.out.print("Is this an Assistant Warden? (1 for Yes, 2 for Chief Warden): ");
        boolean isAssistant = (readIntInput("") == 1);

        System.out.print("Assign to Hostel ID (e.g. BH1, GH1) [or leave blank for later]: ");
        String hostelId = scanner.nextLine().trim();

        Warden warden = new Warden(empId, name, phone, email, gender, empId, shift, null, isAssistant);
        service.registerStaff(warden);

        if (!hostelId.isEmpty()) {
            String assignResult = service.assignWardenToHostel(empId, hostelId);
            System.out.println(assignResult);
        } else {
            System.out.println("[SUCCESS] Warden registered successfully (Unassigned).");
        }
    }

    private static void registerCaretakerFlow() {
        System.out.println("\n--- REGISTER FLOOR CARETAKER ---");
        System.out.print("Enter Employee ID (e.g. EMP-CT-04) [0 to Cancel]: ");
        String empId = scanner.nextLine().trim();
        if (empId.equals("0")) return;

        System.out.print("Enter Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter Phone: ");
        String phone = scanner.nextLine().trim();

        System.out.print("Enter Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter Gender (1 for MALE, 2 for FEMALE): ");
        Gender gender = (readIntInput("") == 2) ? Gender.FEMALE : Gender.MALE;

        System.out.print("Enter Shift (e.g. Day Shift, Night Shift): ");
        String shift = scanner.nextLine().trim();

        System.out.print("Assign to Hostel ID (e.g. BH1, GH1): ");
        String hostelId = scanner.nextLine().trim();

        int floorNum = readIntInput("Assign to Floor Number (e.g. 1, 2, 3): ");

        Caretaker caretaker = new Caretaker(empId, name, phone, email, gender, empId, shift, null, floorNum);
        service.registerStaff(caretaker);

        String assignResult = service.assignCaretakerToFloor(empId, hostelId, floorNum);
        System.out.println(assignResult);
    }

    private static void viewStaffList() {
        System.out.println("\n--- ALL REGISTERED STAFF ---");
        if (service.getAllStaff().isEmpty()) {
            System.out.println("No staff registered yet. Select Option 7 to load demo staff or register one.");
            return;
        }

        for (Staff st : service.getAllStaff()) {
            st.displayDetails();
        }
    }

    // --- 5. VIEW HOSTELS ---
    private static void handleViewHostels() {
        System.out.println("\n--- ALL HOSTEL BUILDINGS & ROOM OCCUPANCY ---");
        if (service.getAllHostels().isEmpty()) {
            System.out.println("[NOTICE] No hostels found! Select Option 1 to create one, or Option 7 to load demo hostels.");
            return;
        }

        for (Hostel h : service.getAllHostels()) {
            h.displayHostelSummary();
            System.out.println();
        }
    }

    // --- 6. VIEW STUDENTS ---
    private static void handleViewStudents() {
        System.out.println("\n--- REGISTERED STUDENTS & QUEUE STATUS ---");
        if (service.getAllStudents().isEmpty()) {
            System.out.println("[NOTICE] No students registered yet! Select Option 2 to register, or Option 7 to load demo students.");
            return;
        }

        for (Student s : service.getAllStudents()) {
            s.displayDetails();
        }

        System.out.printf("%nSummary: %d Total Students | %d in Priority Queue (>30km) | %d in Localite Waitlist (<=30km)%n",
                service.getAllStudents().size(),
                service.getAllocationQueue().size(),
                service.getWaitlistLocalites().size());
    }

    // --- 7. LOAD SAMPLE DATA ---
    private static void handleLoadSampleData() {
        service.loadSampleData();
        System.out.println("\n=================================================");
        System.out.println("  [SUCCESS] Demo Data Loaded (Existing Data Preserved)!");
        System.out.println("=================================================");
        System.out.println("  - Staff Loaded   : Dr. Rajesh Sharma (Warden), Suresh & Ramesh (Caretakers)");
        System.out.println("  - Hostels Loaded : BH1 (Aryabhatta Boys) & GH1 (Gargi Girls)");
        System.out.println("  - Students Loaded: 2024CS01, 2024EC01, 2024ME01, 2024CS02");
        System.out.println("-------------------------------------------------");
        System.out.println("Press 8 anytime to view the complete system audit report.");
        System.out.println("=================================================");
    }

    // --- 8. COMPLETE SYSTEM AUDIT & EXPORT ---
    private static void handleSystemAuditReport() {
        System.out.println("\nGenerating System Audit Report...\n");
        String report = service.generateSystemAuditReport();
        System.out.println(report);

        String exportPath = "data" + File.separator + "system_report.txt";
        boolean saved = service.exportAuditReportToFile(exportPath);
        if (saved) {
            System.out.println("[SUCCESS] Report also exported to: " + exportPath);
        }
    }

    // --- 9. RESET DATA ---
    private static void handleResetData() {
        System.out.print("Are you sure you want to clear ALL data? (Type 'YES' to confirm): ");
        String confirm = scanner.nextLine().trim();
        if (confirm.equalsIgnoreCase("YES")) {
            service.clearAllData();
            System.out.println("[SUCCESS] All data cleared. System reset to fresh state.");
        } else {
            System.out.println("[Cancelled] Reset aborted.");
        }
    }

    // --- HELPER INPUT METHODS ---
    private static int readIntInput(String prompt) {
        while (true) {
            if (!prompt.isEmpty()) {
                System.out.print(prompt);
            }
            try {
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("[!] Please enter a valid number: ");
            }
        }
    }

    private static double readDoubleInput(String prompt) {
        while (true) {
            if (!prompt.isEmpty()) {
                System.out.print(prompt);
            }
            try {
                String input = scanner.nextLine().trim();
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.print("[!] Please enter a valid decimal number: ");
            }
        }
    }
}
