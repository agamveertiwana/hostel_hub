package com.hostelhub.storage;

import com.hostelhub.model.*;
import java.util.Collection;
import java.util.List;

/**
 * Storage interface defining the contract for data persistence in HostelHub.
 * Demonstrates Interface Abstraction and Separation of Concerns (DAO Pattern).
 */
public interface StorageEngine {
    void saveHostels(Collection<Hostel> hostels);
    List<Hostel> loadHostels();

    void saveStaff(Collection<Staff> staffMembers);
    List<Staff> loadStaff();

    void saveStudents(Collection<Student> students);
    List<Student> loadStudents();

    void saveComplaints(Collection<Complaint> complaints);
    List<Complaint> loadComplaints();

    void saveGatePasses(Collection<GatePass> gatePasses);
    List<GatePass> loadGatePasses();

    void savePayments(Collection<PaymentTransaction> payments);
    List<PaymentTransaction> loadPayments();
}
