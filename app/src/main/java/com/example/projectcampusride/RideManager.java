package com.example.projectcampusride;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

import java.util.HashMap;
import java.util.Map;

public class RideManager {
    private static RideManager instance;
    protected List<Ride> rides;
    protected List<RideRequest> rideRequests;

    RideManager() {
        this.rides = new ArrayList<>();
    }

    public static synchronized RideManager getInstance() {
        if (instance == null) {
            instance = new RideManager();
        }
        return instance;
    }

    public void createTrip(String driverName, String startLocation, String endLocation,
                           int availableSeats, double price) {
        // Create a Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a Trip object or use a Map
        Map<String, Object> trip = new HashMap<>();
        trip.put("driverName", driverName);
        trip.put("startLocation", startLocation);
        trip.put("endLocation", endLocation);
        trip.put("availableSeats", availableSeats);
        trip.put("price", price);

        // Add the trip document to the 'trips' collection
        db.collection("trips")
                .add(trip)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        System.out.println("Trip uploaded successfully! Document ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        System.err.println("Error adding trip: " + e.getMessage());
                    }
                });
    }

    public List<Ride> searchTrips(String startLocation, String endLocation) {
        return rides.stream()
                .filter(trip ->
                        trip.getStartLocation().toLowerCase().contains(startLocation.toLowerCase()) &&
                                trip.getEndLocation().toLowerCase().contains(endLocation.toLowerCase()) &&
                                trip.getAvailableSeats() > 0)
                .collect(Collectors.toList());
    }

    public List<Ride> getAllTrips() {
        return new ArrayList<>(rides);
    }

    public void requestToJoinTrip(String tripId, String passengerName, String passengerPhone) {
        RideRequest newRequest = new RideRequest(tripId, passengerName, passengerPhone);
        rideRequests.add(newRequest);
    }

    public void approveRequest(String requestId) {
        for (RideRequest request : rideRequests) {
            if (request.getId().equals(requestId)) {
                request.approve();
                // find the wanted ride and accept the passenger
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
                    // find the ride
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
