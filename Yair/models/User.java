package com.example.campusride.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    private String userId;
    private String name;
    private String email;
    private String phone;
    private boolean validStudent;
    private double rateGrade;
    private UserRole role; // "Driver" or "Passenger"
    private List<String> rideHistory;
    private Map<String, String> notifications;

    // fileds for Driver
    private String licenseNumber;
    private String vehicleDetails;

    public User(String userId, String name, String email, String phone, boolean validStudent, double rateGrade, UserRole role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.validStudent = validStudent;
        this.rateGrade = rateGrade;
        this.role = role != null ? role : UserRole.UNKNOWN;
        this.rideHistory = new ArrayList<>();
        this.notifications = new HashMap<>();

        if (this.role == UserRole.DRIVER) {
            this.licenseNumber = "";
            this.vehicleDetails = "";
        }
    }

    // Getters & Setters
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public boolean isValidStudent() { return validStudent; }
    public double getRateGrade() { return rateGrade; }
    public UserRole getRole() { return role; }
    public List<String> getRideHistory() { return new ArrayList<>(rideHistory); }
    public Map<String, String> getNotifications() { return new HashMap<>(notifications); }


    public void setRole(UserRole role) {
        this.role = role;
        if (role != UserRole.DRIVER) {
            this.licenseNumber = null;
            this.vehicleDetails = null;
        }
    }
    public void addRideToHistory(String rideId) { rideHistory.add(rideId); }

    public void setDriverDetails(String licenseNumber, String vehicleDetails) {
        if (this.role == UserRole.DRIVER) {
            this.licenseNumber = licenseNumber;
            this.vehicleDetails = vehicleDetails;
        }
    }

    public String getLicenseNumber() { return licenseNumber; }
    public String getVehicleDetails() { return vehicleDetails; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("User { ")
                .append("ID='").append(userId).append("', ")
                .append("Name='").append(name).append("', ")
                .append("Role='").append(role).append("', ")
                .append("Rating=").append(rateGrade).append(", ")
                .append("Ride History=").append(rideHistory.size());

        if (this.role == UserRole.DRIVER) {
            sb.append(", LicenseNumber='").append(licenseNumber).append("', ")
                    .append("Vehicle='").append(vehicleDetails).append("'");
        }

        sb.append(" }");
        return sb.toString();
    }

}


