package com.example.campusride.models;

public enum RatingType {
    DRIVER, PASSENGER;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public static RatingType fromString(String type) {
        try {
            return RatingType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DRIVER; // default
        }
    }
}
