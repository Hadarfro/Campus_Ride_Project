package com.example.projectcampusride;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Passenger {
    private static final String TAG = "Passenger";
    private static final String COLLECTION_RIDES = "Rides";
    private static final String COLLECTION_PASSENGERS = "passengers";

    private FirebaseFirestore db;
    private String id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private double rating;
    private int totalRides;
    private List<String> rideHistory; // Store ride IDs instead of Ride objects
    private List<String> preferredRoutes;
    private Map<String, String> notifications;

    public Passenger(String id, String fullName, String email, String phoneNumber) {
        this.db = FirebaseFirestore.getInstance();
        this.id = id;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.rating = 5.0;
        this.totalRides = 0;
        this.rideHistory = new ArrayList<>();
        this.preferredRoutes = new ArrayList<>();
        this.notifications = new HashMap<>();

        // Save passenger to Firestore
        saveToFirestore();
    }

    private void saveToFirestore() {
        Map<String, Object> passengerData = new HashMap<>();
        passengerData.put("id", id);
        passengerData.put("full Name", fullName);
        passengerData.put("email", email);
        passengerData.put("phone Number", phoneNumber);
        passengerData.put("rating", rating);
        passengerData.put("total Rides", totalRides);
        passengerData.put("ride History", rideHistory);
        passengerData.put("preferred Routes", preferredRoutes);
        passengerData.put("notifications", notifications);

        db.collection(COLLECTION_PASSENGERS)
                .document(id)
                .set(passengerData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Passenger saved successfully"))
                .addOnFailureListener(e -> Log.w(TAG, "Error saving passenger", e));
    }

    public Task<List<Map<String, Object>>> searchRide(String startLocation, String destination, Date time) {
        return db.collection(COLLECTION_RIDES)
                .whereEqualTo("startLocation", startLocation)
                .whereEqualTo("endLocation", destination)
                .get()
                .continueWith(task -> {List<Map<String, Object>> availableRides = new ArrayList<>();
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            availableRides.add(document.getData());
                        }
                    }
                    return availableRides;
                });
    }

    public Task<Boolean> bookRide(String rideId) {
        DocumentReference rideRef = db.collection(COLLECTION_RIDES).document(rideId);

        return db.runTransaction(transaction -> {
            // Get the latest ride data
            Map<String, Object> ride = transaction.get(rideRef).getData();
            if (ride == null) return false;

            // Check available seats
            long availableSeats = (long) ride.get("availableSeats");
            if (availableSeats <= 0) return false;

            // Update the ride
            transaction.update(rideRef, "availableSeats", availableSeats - 1);

            // Add passenger to ride's passenger list
            List<String> passengers = (List<String>) ride.get("passengers");
            if (passengers == null) passengers = new ArrayList<>();
            passengers.add(id);
            transaction.update(rideRef, "passengers", passengers);

            // Update passenger's ride history
            rideHistory.add(rideId);
            totalRides++;
            saveToFirestore();

            return true;
        });
    }

    public Task<Boolean> cancelRide(String rideId) {
        DocumentReference rideRef = db.collection(COLLECTION_RIDES).document(rideId);

        return db.runTransaction(transaction -> {
            // Get the latest ride data
            Map<String, Object> ride = transaction.get(rideRef).getData();
            if (ride == null) return false;

            // Update available seats
            long availableSeats = (long) ride.get("availableSeats");
            transaction.update(rideRef, "availableSeats", availableSeats + 1);

            // Remove passenger from ride's passenger list
            List<String> passengers = (List<String>) ride.get("passengers");
            if (passengers != null) {
                passengers.remove(id);
                transaction.update(rideRef, "passengers", passengers);
            }

            // Update passenger's ride history
            rideHistory.remove(rideId);
            saveToFirestore();

            return true;
        });
    }

    public Task<DocumentReference> rateDriver(String driverId, double rating, String comment) {
        Map<String, Object> ratingData = new HashMap<>();
        ratingData.put("passengerId", id);
        ratingData.put("driverId", driverId);
        ratingData.put("rating", rating);
        ratingData.put("comment", comment);
        ratingData.put("timestamp", new Date());

        return db.collection("ratings").add(ratingData);
    }

    public void addPreferredRoute(String route) {
        preferredRoutes.add(route);
        saveToFirestore();
    }

    public void receiveNotification(String rideId, String message) {
        notifications.put(rideId, message);
        saveToFirestore();
    }

    // Getters remain the same
    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public double getRating() { return rating; }
    public List<String> getRideHistory() { return new ArrayList<>(rideHistory); }
    public int getTotalRides() { return totalRides; }
    public List<String> getPreferredRoutes() { return new ArrayList<>(preferredRoutes); }
    public Map<String, String> getNotifications() { return new HashMap<>(notifications); }

    // Setters now update Firestore
    public void setFullName(String fullName) {
        this.fullName = fullName;
        saveToFirestore();
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        saveToFirestore();
    }

    public void updateRating(double newRating) {
        this.rating = (this.rating * this.totalRides + newRating) / (this.totalRides + 1);
        saveToFirestore();
    }

    public void clearNotifications() {
        this.notifications.clear();
        saveToFirestore();
    }

    @Override
    public String toString() {
        return "Passenger { " +
                "id = '" + id + '\'' +
                ", fullName = '" + fullName + '\'' +
                ", rating = " + rating +
                ", totalRides = " + totalRides +
                '}';
    }
}