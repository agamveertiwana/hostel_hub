package com.hostelhub.storage;

import com.hostelhub.model.*;
import java.io.File;
import java.util.*;

/**
 * Initializes and seeds default data (Wardens, Caretakers, Hostels, Rooms, Students)
 * on first startup if persistent files do not exist yet.
 */
public class DataInitializer {
    public static void initializeDefaultDataIfEmpty(StorageEngine storage) {
        List<Staff> existingStaff = storage.loadStaff();
        List<Hostel> existingHostels = storage.loadHostels();

        // If staff or hostels do not exist on disk, seed them
        if (existingStaff.isEmpty()) {
            List<Staff> defaultStaff = createDefaultStaff();
            storage.saveStaff(defaultStaff);
        }

        if (existingHostels.isEmpty()) {
            List<Hostel> defaultHostels = createDefaultHostels();
            storage.saveHostels(defaultHostels);
        }

        List<Student> existingStudents = storage.loadStudents();
        if (existingStudents.isEmpty()) {
            List<Student> defaultStudents = createDefaultStudents();
            storage.saveStudents(defaultStudents);
        }
    }

    public static List<Staff> createDefaultStaff() {
        List<Staff> staffList = new ArrayList<>();

        // Wardens
        Warden chiefWarden = new Warden("W01", "Dr. Rajesh Sharma", "9876543210", "rajesh.wrd@college.edu",
                Gender.MALE, "EMP-WRD-01", "Morning Shift", "Aryabhatta Boys Hostel", false);
        Warden assistantWarden = new Warden("W02", "Dr. Priya Saxena", "9876543211", "priya.wrd@college.edu",
                Gender.FEMALE, "EMP-WRD-02", "Evening Shift", "Gargi Girls Hostel", true);

        // Caretakers for Boys Hostel
        Caretaker ct1 = new Caretaker("C01", "Suresh Kumar", "9123456780", "suresh.ct@college.edu",
                Gender.MALE, "EMP-CT-01", "Morning", "Aryabhatta Boys Hostel", 1);
        Caretaker ct2 = new Caretaker("C02", "Ramesh Yadav", "9123456782", "ramesh.ct@college.edu",
                Gender.MALE, "EMP-CT-02", "Evening", "Aryabhatta Boys Hostel", 2);
        Caretaker ct3 = new Caretaker("C03", "Mahesh Verma", "9123456783", "mahesh.ct@college.edu",
                Gender.MALE, "EMP-CT-03", "Night", "Aryabhatta Boys Hostel", 3);

        // Caretakers for Girls Hostel
        Caretaker ct4 = new Caretaker("C04", "Anita Devi", "9123456784", "anita.ct@college.edu",
                Gender.FEMALE, "EMP-CT-04", "Morning", "Gargi Girls Hostel", 1);
        Caretaker ct5 = new Caretaker("C05", "Rekha Sharma", "9123456785", "rekha.ct@college.edu",
                Gender.FEMALE, "EMP-CT-05", "Evening", "Gargi Girls Hostel", 2);

        staffList.add(chiefWarden);
        staffList.add(assistantWarden);
        staffList.add(ct1);
        staffList.add(ct2);
        staffList.add(ct3);
        staffList.add(ct4);
        staffList.add(ct5);

        return staffList;
    }

    public static List<Hostel> createDefaultHostels() {
        List<Hostel> hostels = new ArrayList<>();

        // 1. Aryabhatta Boys Hostel
        Hostel bh1 = new Hostel("BH1", "Aryabhatta Boys Hostel", Gender.MALE);
        bh1.setWardenEmployeeId("EMP-WRD-01");

        Floor f1 = new Floor(1);
        f1.setCaretakerEmployeeId("EMP-CT-01");
        f1.addRoom(new Room("101", 1, 2, RoomType.AC, WashroomType.ATTACHED));
        f1.addRoom(new Room("102", 1, 3, RoomType.NON_AC, WashroomType.COMMON));
        f1.addRoom(new Room("103", 1, 4, RoomType.NON_AC, WashroomType.COMMON));
        bh1.addFloor(f1);

        Floor f2 = new Floor(2);
        f2.setCaretakerEmployeeId("EMP-CT-02");
        f2.addRoom(new Room("201", 2, 2, RoomType.AC, WashroomType.ATTACHED));
        f2.addRoom(new Room("202", 2, 3, RoomType.AC, WashroomType.COMMON));
        f2.addRoom(new Room("203", 2, 4, RoomType.NON_AC, WashroomType.COMMON));
        bh1.addFloor(f2);
        hostels.add(bh1);

        // 2. Gargi Girls Hostel
        Hostel gh1 = new Hostel("GH1", "Gargi Girls Hostel", Gender.FEMALE);
        gh1.setWardenEmployeeId("EMP-WRD-02");

        Floor gf1 = new Floor(1);
        gf1.setCaretakerEmployeeId("EMP-CT-04");
        gf1.addRoom(new Room("G101", 1, 2, RoomType.AC, WashroomType.ATTACHED));
        gf1.addRoom(new Room("G102", 1, 3, RoomType.NON_AC, WashroomType.COMMON));
        gh1.addFloor(gf1);

        Floor gf2 = new Floor(2);
        gf2.setCaretakerEmployeeId("EMP-CT-05");
        gf2.addRoom(new Room("G201", 2, 2, RoomType.AC, WashroomType.ATTACHED));
        gf2.addRoom(new Room("G202", 2, 3, RoomType.AC, WashroomType.ATTACHED));
        gh1.addFloor(gf2);
        hostels.add(gh1);

        return hostels;
    }

    public static List<Student> createDefaultStudents() {
        List<Student> students = new ArrayList<>();

        Student s1 = new Student("2024CS01", "Aman Verma", "9988776655", "aman@student.edu",
                Gender.MALE, "Computer Science", 1, 145.0);
        s1.setAllocatedHostelName("Aryabhatta Boys Hostel");
        s1.setAllocatedRoomId("101");

        Student s2 = new Student("2024EC01", "Priya Sharma", "9876543211", "priya@student.edu",
                Gender.FEMALE, "Electronics", 1, 85.0);
        s2.setAllocatedHostelName("Gargi Girls Hostel");
        s2.setAllocatedRoomId("G101");

        Student s3 = new Student("2024ME01", "Vikram Singh", "9123456781", "vikram@student.edu",
                Gender.MALE, "Mechanical", 1, 210.0); // In Queue

        Student s4 = new Student("2024CS02", "Rohit Mehta", "9871234560", "rohit@student.edu",
                Gender.MALE, "Civil", 1, 18.0); // Localite Waitlist (<=30km)

        students.add(s1);
        students.add(s2);
        students.add(s3);
        students.add(s4);

        return students;
    }
}
