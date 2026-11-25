package com.hostelhub.storage;

import com.hostelhub.model.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Concrete file storage implementation saving all system records into human-readable CSV files in the data/ folder.
 */
public class FileStorageService implements StorageEngine {
    private static final String DATA_DIR = "data";
    private static final String STAFF_FILE = DATA_DIR + File.separator + "staff.csv";
    private static final String HOSTELS_FILE = DATA_DIR + File.separator + "hostels.csv";
    private static final String ROOMS_FILE = DATA_DIR + File.separator + "rooms.csv";
    private static final String STUDENTS_FILE = DATA_DIR + File.separator + "students.csv";
    private static final String COMPLAINTS_FILE = DATA_DIR + File.separator + "complaints.csv";
    private static final String GATEPASS_FILE = DATA_DIR + File.separator + "gatepasses.csv";
    private static final String PAYMENTS_FILE = DATA_DIR + File.separator + "payments.csv";

    public FileStorageService() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    // --- STAFF PERSISTENCE ---
    @Override
    public void saveStaff(Collection<Staff> staffMembers) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(STAFF_FILE))) {
            writer.println("EmployeeId,Name,Phone,Email,Gender,Shift,Role,AssignedHostel,AssignedFloor,IsAssistant");
            for (Staff s : staffMembers) {
                if (s instanceof Warden) {
                    Warden w = (Warden) s;
                    writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%d,%b%n",
                            w.getEmployeeId(), escapeCsv(w.getName()), w.getPhone(), w.getEmail(),
                            w.getGender(), w.getShift(), w.getRole(),
                            escapeCsv(w.getAssignedHostelName() == null ? "" : w.getAssignedHostelName()),
                            0, w.isAssistant());
                } else if (s instanceof Caretaker) {
                    Caretaker ct = (Caretaker) s;
                    writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%d,%b%n",
                            ct.getEmployeeId(), escapeCsv(ct.getName()), ct.getPhone(), ct.getEmail(),
                            ct.getGender(), ct.getShift(), ct.getRole(),
                            escapeCsv(ct.getAssignedHostelName() == null ? "" : ct.getAssignedHostelName()),
                            ct.getAssignedFloorNumber(), false);
                }
            }
        } catch (IOException e) {
            System.err.println("[Storage Error] Saving staff: " + e.getMessage());
        }
    }

    @Override
    public List<Staff> loadStaff() {
        List<Staff> staffList = new ArrayList<>();
        File file = new File(STAFF_FILE);
        if (!file.exists()) return staffList;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // Header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 10) {
                    String empId = parts[0].trim();
                    String name = parts[1].trim();
                    String phone = parts[2].trim();
                    String email = parts[3].trim();
                    Gender gender = Gender.valueOf(parts[4].trim());
                    String shift = parts[5].trim();
                    String roleStr = parts[6].trim();
                    String assignedHostel = parts[7].trim().isEmpty() ? null : parts[7].trim();
                    int floorNum = Integer.parseInt(parts[8].trim());
                    boolean isAssistant = Boolean.parseBoolean(parts[9].trim());

                    if (roleStr.contains("WARDEN")) {
                        staffList.add(new Warden(empId, name, phone, email, gender, empId, shift, assignedHostel, isAssistant));
                    } else if (roleStr.contains("CARETAKER")) {
                        staffList.add(new Caretaker(empId, name, phone, email, gender, empId, shift, assignedHostel, floorNum));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[Storage Notice] Staff load fallback: " + e.getMessage());
        }
        return staffList;
    }

    // --- HOSTEL & ROOM PERSISTENCE ---
    @Override
    public void saveHostels(Collection<Hostel> hostels) {
        try (PrintWriter hWriter = new PrintWriter(new FileWriter(HOSTELS_FILE));
             PrintWriter rWriter = new PrintWriter(new FileWriter(ROOMS_FILE))) {

            hWriter.println("HostelId,Name,AllowedGender,WardenId,TotalFloors");
            rWriter.println("HostelId,FloorNumber,RoomId,Capacity,RoomType,WashroomType,CaretakerId,AllocatedRolls");

            for (Hostel h : hostels) {
                hWriter.printf("%s,%s,%s,%s,%d%n",
                        h.getHostelId(), escapeCsv(h.getName()), h.getAllowedGender(),
                        h.getWardenEmployeeId() == null ? "" : h.getWardenEmployeeId(),
                        h.getFloors().size());

                for (Floor f : h.getFloors()) {
                    String ctId = f.getCaretakerEmployeeId() == null ? "" : f.getCaretakerEmployeeId();
                    for (Room r : f.getRooms()) {
                        String allocated = String.join(";", r.getStudentRollNumbers());
                        rWriter.printf("%s,%d,%s,%d,%s,%s,%s,%s%n",
                                h.getHostelId(), f.getFloorNumber(), r.getRoomId(),
                                r.getCapacity(), r.getRoomType(), r.getWashroomType(),
                                ctId, allocated);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[Storage Error] Saving hostels/rooms: " + e.getMessage());
        }
    }

    @Override
    public List<Hostel> loadHostels() {
        Map<String, Hostel> hostelMap = new LinkedHashMap<>();
        File hFile = new File(HOSTELS_FILE);
        if (!hFile.exists()) return new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(hFile))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 4) {
                    String hId = parts[0].trim();
                    String name = parts[1].trim();
                    Gender gender = Gender.valueOf(parts[2].trim());
                    String wardenId = parts[3].trim().isEmpty() ? null : parts[3].trim();

                    Hostel hostel = new Hostel(hId, name, gender);
                    hostel.setWardenEmployeeId(wardenId);
                    hostelMap.put(hId.toUpperCase(), hostel);
                }
            }
        } catch (Exception e) {
            System.err.println("[Storage Notice] Hostel load fallback: " + e.getMessage());
        }

        // Load Rooms
        File rFile = new File(ROOMS_FILE);
        if (rFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(rFile))) {
                String line = reader.readLine(); // Header
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",", -1);
                    if (parts.length >= 7) {
                        String hId = parts[0].trim().toUpperCase();
                        int floorNum = Integer.parseInt(parts[1].trim());
                        String roomId = parts[2].trim();
                        int capacity = Integer.parseInt(parts[3].trim());
                        RoomType rType = RoomType.valueOf(parts[4].trim());
                        WashroomType wType = WashroomType.valueOf(parts[5].trim());
                        String ctId = parts[6].trim().isEmpty() ? null : parts[6].trim();
                        String allocatedRolls = (parts.length >= 8) ? parts[7].trim() : "";

                        Hostel h = hostelMap.get(hId);
                        if (h != null) {
                            Floor floor = h.getFloor(floorNum);
                            if (floor == null) {
                                floor = new Floor(floorNum);
                                h.addFloor(floor);
                            }
                            if (ctId != null) floor.setCaretakerEmployeeId(ctId);

                            Room room = new Room(roomId, floorNum, capacity, rType, wType);
                            if (!allocatedRolls.isEmpty()) {
                                for (String roll : allocatedRolls.split(";")) {
                                    if (!roll.trim().isEmpty()) {
                                        room.allocateStudent(roll.trim());
                                    }
                                }
                            }
                            floor.addRoom(room);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("[Storage Notice] Room load fallback: " + e.getMessage());
            }
        }

        return new ArrayList<>(hostelMap.values());
    }

    // --- STUDENT PERSISTENCE ---
    @Override
    public void saveStudents(Collection<Student> students) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(STUDENTS_FILE))) {
            writer.println("RollNumber,Name,Phone,Email,Gender,Department,Year,Distance,AllocatedHostel,AllocatedRoom");
            for (Student s : students) {
                writer.printf("%s,%s,%s,%s,%s,%s,%d,%.2f,%s,%s%n",
                        s.getRollNumber(), escapeCsv(s.getName()), s.getPhone(), s.getEmail(),
                        s.getGender(), s.getDepartment(), s.getYear(), s.getDistanceFromCollege(),
                        s.getAllocatedHostelName() == null ? "" : escapeCsv(s.getAllocatedHostelName()),
                        s.getAllocatedRoomId() == null ? "" : s.getAllocatedRoomId());
            }
        } catch (IOException e) {
            System.err.println("[Storage Error] Saving students: " + e.getMessage());
        }
    }

    @Override
    public List<Student> loadStudents() {
        List<Student> list = new ArrayList<>();
        File file = new File(STUDENTS_FILE);
        if (!file.exists()) return list;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // Header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 8) {
                    String roll = parts[0].trim();
                    String name = parts[1].trim();
                    String phone = parts[2].trim();
                    String email = parts[3].trim();
                    Gender gender = Gender.valueOf(parts[4].trim());
                    String dept = parts[5].trim();
                    int year = Integer.parseInt(parts[6].trim());
                    double distance = Double.parseDouble(parts[7].trim());
                    String hName = (parts.length >= 9 && !parts[8].trim().isEmpty()) ? parts[8].trim() : null;
                    String rId = (parts.length >= 10 && !parts[9].trim().isEmpty()) ? parts[9].trim() : null;

                    Student s = new Student(roll, name, phone, email, gender, dept, year, distance);
                    s.setAllocatedHostelName(hName);
                    s.setAllocatedRoomId(rId);
                    list.add(s);
                }
            }
        } catch (Exception e) {
            System.err.println("[Storage Notice] Student load fallback: " + e.getMessage());
        }
        return list;
    }

    // --- COMPLAINTS PERSISTENCE ---
    @Override
    public void saveComplaints(Collection<Complaint> complaints) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(COMPLAINTS_FILE))) {
            writer.println("ComplaintId,StudentRoll,HostelId,RoomId,Type,Priority,Status,Description,ResolvedBy,Notes");
            for (Complaint c : complaints) {
                writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                        c.getComplaintId(), c.getStudentRollNumber(), c.getHostelId(), c.getRoomId(),
                        c.getType(), c.getPriority(), c.getStatus(),
                        escapeCsv(c.getDescription()),
                        c.getResolvedByStaffId() == null ? "" : c.getResolvedByStaffId(),
                        escapeCsv(c.getResolutionNotes() == null ? "" : c.getResolutionNotes()));
            }
        } catch (IOException e) {
            System.err.println("[Storage Error] Saving complaints: " + e.getMessage());
        }
    }

    @Override
    public List<Complaint> loadComplaints() {
        List<Complaint> list = new ArrayList<>();
        File file = new File(COMPLAINTS_FILE);
        if (!file.exists()) return list;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] p = line.split(",", -1);
                if (p.length >= 8) {
                    Complaint c = new Complaint(p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(),
                            ComplaintType.valueOf(p[4].trim()), ComplaintPriority.valueOf(p[5].trim()), p[7].trim());
                    String status = p[6].trim();
                    if (status.equalsIgnoreCase("RESOLVED")) {
                        c.resolve(p.length >= 9 ? p[8].trim() : "", p.length >= 10 ? p[9].trim() : "");
                    }
                    list.add(c);
                }
            }
        } catch (Exception e) {
            // fallback
        }
        return list;
    }

    // --- GATEPASS PERSISTENCE ---
    @Override
    public void saveGatePasses(Collection<GatePass> gatePasses) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(GATEPASS_FILE))) {
            writer.println("PassId,RollNumber,PassType,Reason,Status,ApprovedBy");
            for (GatePass gp : gatePasses) {
                writer.printf("%s,%s,%s,%s,%s,%s%n",
                        gp.getPassId(), gp.getStudentRollNumber(), gp.getPassType(),
                        escapeCsv(gp.getReason()), gp.getStatus(),
                        gp.getApprovedByWardenId() == null ? "" : gp.getApprovedByWardenId());
            }
        } catch (IOException e) {
            System.err.println("[Storage Error] Saving gatepasses: " + e.getMessage());
        }
    }

    @Override
    public List<GatePass> loadGatePasses() {
        List<GatePass> list = new ArrayList<>();
        File file = new File(GATEPASS_FILE);
        if (!file.exists()) return list;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] p = line.split(",", -1);
                if (p.length >= 5) {
                    String passId = p[0].trim();
                    String roll = p[1].trim();
                    String type = p[2].trim();
                    String reason = p[3].trim();
                    String status = p[4].trim();
                    String approvedBy = p.length >= 6 ? p[5].trim() : null;

                    GatePass pass;
                    if (type.equalsIgnoreCase("NIGHT_OUT")) {
                        pass = new NightOutPass(passId, roll, reason, LocalDateTime.now(), LocalDateTime.now().plusDays(1), "Home Address", "9999999999");
                    } else {
                        pass = new DayOutPass(passId, roll, reason, LocalDateTime.now(), LocalDateTime.now().plusHours(4));
                    }
                    if (status.equalsIgnoreCase("APPROVED")) {
                        pass.approve(approvedBy);
                    }
                    list.add(pass);
                }
            }
        } catch (Exception e) {
            // fallback
        }
        return list;
    }

    // --- PAYMENTS PERSISTENCE ---
    @Override
    public void savePayments(Collection<PaymentTransaction> payments) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(PAYMENTS_FILE))) {
            writer.println("TxnId,RollNumber,Amount,Mode,Status,Note");
            for (PaymentTransaction pt : payments) {
                writer.printf("%s,%s,%.2f,%s,%s,%s%n",
                        pt.getTransactionId(), pt.getStudentRollNumber(), pt.getAmountPaid(),
                        pt.getPaymentMode(), pt.getStatus(), escapeCsv(pt.getReferenceNote()));
            }
        } catch (IOException e) {
            System.err.println("[Storage Error] Saving payments: " + e.getMessage());
        }
    }

    @Override
    public List<PaymentTransaction> loadPayments() {
        List<PaymentTransaction> list = new ArrayList<>();
        File file = new File(PAYMENTS_FILE);
        if (!file.exists()) return list;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] p = line.split(",", -1);
                if (p.length >= 5) {
                    list.add(new PaymentTransaction(p[0].trim(), p[1].trim(), Double.parseDouble(p[2].trim()),
                            PaymentMode.valueOf(p[3].trim()), p.length >= 6 ? p[5].trim() : "Semester Fee"));
                }
            }
        } catch (Exception e) {
            // fallback
        }
        return list;
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        return val.replace(",", ";").replace("\n", " ");
    }
}
