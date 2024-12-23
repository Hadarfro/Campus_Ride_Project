package com.example.projectcampusride;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Ride implements Serializable {
    private String id;
    private String driverName;
    private String startLocation;
    private String endLocation;
    private int availableSeats;
    private Date departureTime;
    private double price;
    private List<String> passengers;

    public Ride(String driverName, String startLocation, String endLocation,
                int availableSeats, double price) {
        this.id = UUID.randomUUID().toString();
        this.driverName = driverName;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.availableSeats = availableSeats;
        this.departureTime = new Date();
        this.price = price;
        this.passengers = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getDriverName() { return driverName; }
    public String getStartLocation() { return startLocation; }
    public String getEndLocation() { return endLocation; }
    public int getAvailableSeats() { return availableSeats; }
    public Date getDepartureTime() { return departureTime; }
    public double getPrice() { return price; }
    public List<String> getPassengers() { return passengers; }

    public boolean addPassenger(String passengerName) {
        if (availableSeats > 0) {
            passengers.add(passengerName);
            availableSeats--;
            return true;
        }
        return false;
    }
}