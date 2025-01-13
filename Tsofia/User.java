package com.example.signuplogincampusride;

public class User {
    public String name, email, phoneNumber, id;

    public User() {
        // Default constructor
    }

    public User(String name, String email, String phoneNumber, String id) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.id = id;
    }
}
