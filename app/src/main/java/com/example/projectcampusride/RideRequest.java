package com.example.projectcampusride;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RideRequest implements Serializable {
    public enum Status {
        PENDING,   // בהמתנה
        APPROVED,  // מאושר
        REJECTED   // נדחה
    }

    private String id;
    private String tripId;
    private String passengerName;
    private String passengerPhone;
    private Date requestTime;
    private Status status;
    private static RideManager instance;
    private List<Ride> rides;
    private List<RideRequest> rideRequests;

    public RideRequest(String tripId, String passengerName, String passengerPhone) {
        this.id = UUID.randomUUID().toString();
        this.tripId = tripId;
        this.passengerName = passengerName;
        this.passengerPhone = passengerPhone;
        this.requestTime = new Date();
        this.status = Status.PENDING;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getTripId() { return tripId; }
    public String getPassengerName() { return passengerName; }
    public String getPassengerPhone() { return passengerPhone; }
    public Date getRequestTime() { return requestTime; }
    public Status getStatus() { return status; }

    public void approve() { this.status = Status.APPROVED; }
    public void reject() { this.status = Status.REJECTED; }

    private void TripManager() {
        this.rides = new ArrayList<>();
        this.rideRequests = new ArrayList<>();
    }

    public static synchronized RideManager getInstance() {
        if (instance == null) {
            instance = new RideManager();
        }
        return instance;
    }

    // שיטות קיימות + הוספת בקשת הצטרפות
    public void requestToJoinTrip(String tripId, String passengerName, String passengerPhone) {
        RideRequest newRequest = new RideRequest(tripId, passengerName, passengerPhone);
        rideRequests.add(newRequest);
    }

    public void approveRequest(String requestId) {
        for (RideRequest request : rideRequests) {
            if (request.getId().equals(requestId)) {
                request.approve();
                // מצא את הנסיעה המתאימה והוסף את הנוסע
                for (Ride ride : rides) {
                    if (ride.getId().equals(request.getTripId())) {
                        ride.addPassenger(request.getPassengerName());
                        break;
                    }
                }
                break;
            }
        }
    }

    public void rejectRequest(String requestId) {
        rideRequests.removeIf(request -> request.getId().equals(requestId));
    }

    public List<RideRequest> getTripRequestsForDriver(String driverName) {
        return rideRequests.stream()
                .filter(request -> {
                    // מצא את הנסיעה של הנהג
                    for (Ride ride : rides) {
                        if (ride.getId().equals(request.getTripId()) &&
                                ride.getDriverName().equals(driverName)) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }
}