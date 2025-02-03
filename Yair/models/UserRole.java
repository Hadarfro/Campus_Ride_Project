package com.example.campusride.models;

public enum UserRole {
    DRIVER, PASSENGER, UNKNOWN;

    @Override
    public String toString() {
        return name().toLowerCase(); // ("driver" / "passenger")
    }

    public static UserRole fromString(String role) {
        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
