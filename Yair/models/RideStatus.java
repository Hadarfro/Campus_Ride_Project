package com.example.campusride.models;

public enum RideStatus {
    ACTIVE, COMPLETED, CANCELLED;

    @Override
    public String toString() {
        return name().toLowerCase(); // "active", "completed", "cancelled"
    }

    public static RideStatus fromString(String status) {
        try {
            return RideStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ACTIVE; // default
        }
    }
}
