package com.hostelhub.model;

import java.io.Serializable;

/**
 * Abstract base class demonstrating Abstraction and Encapsulation.
 */
public abstract class Person implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String phone;
    private String email;
    private Gender gender;

    public Person(String id, String name, String phone, String email, Gender gender) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.gender = gender;
    }

    // Abstract methods to enforce Polymorphism in subclasses
    public abstract Role getRole();
    public abstract void displayDetails();

    // Getters and Setters (Encapsulation)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return String.format("[%s] ID: %s | Name: %s | Phone: %s", getRole(), id, name, phone);
    }
}
