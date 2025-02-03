package com.example.campusride;

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

public class Driver {
    private static final String TAG = "Driver";
    private static final String COLLECTION_RIDES = "Rides";
    private static final String COLLECTION_DRIVERS = "drivers";

    private static final String COLLECTION_RATINGS = "ratings";




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

    private String licenseNumber;
    private String vehicleDetails;


    public Driver(String id, String fullName, String email, String phoneNumber) {
        this.db = FirebaseFirestore.getInstance();
        this.id = id;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.rating = 5.0;
        this.totalRides = 0;
        this.rideHistory = new ArrayList<>();
        this.preferredRoutes = new ArrayList<>();
        this.licenseNumber = licenseNumber;
        this.vehicleDetails = vehicleDetails;
        this.notifications = new HashMap<>();


        // Save passenger to Firestore
        saveToFirestore();
    }

    private void saveToFirestore() {
        Map<String, Object> driverData = new HashMap<>();
        driverData.put("id", id);
        driverData.put("full Name", fullName);
        driverData.put("email", email);
        driverData.put("phone Number", phoneNumber);
        driverData.put("rating", rating);
        driverData.put("total Rides", totalRides);
        driverData.put("ride History", rideHistory);
        driverData.put("preferred Routes", preferredRoutes);
        driverData.put("licenseNumber", licenseNumber);
        driverData.put("vehicleDetails", vehicleDetails);
        driverData.put("notifications", notifications);


        db.collection(COLLECTION_DRIVERS)
                .document(id)
                .set(driverData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Driver saved successfully"))
                .addOnFailureListener(e -> Log.w(TAG, "Error saving driver", e));
    }

    public Task<Void> createRide(String startLocation, String endLocation, int availableSeats, double price) {
        String rideId = db.collection(COLLECTION_RIDES).document().getId();

        Map<String, Object> ride = new HashMap<>();
        ride.put("driverId", id);
        ride.put("driverName", fullName);
        ride.put("licenseNumber", licenseNumber);
        ride.put("startLocation", startLocation);
        ride.put("endLocation", endLocation);
        ride.put("availableSeats", availableSeats);
        ride.put("price", price);
        ride.put("vehicleDetails",vehicleDetails);
        ride.put("timestamp", new Date());
        ride.put("passengers", new ArrayList<String>());

        return db.collection(COLLECTION_RIDES)
                .document(rideId)
                .set(ride)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Ride created successfully");
                    rideHistory.add(rideId);
                    totalRides++;
                    saveToFirestore();
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error creating ride", e));
    }

    public Task<Void> deleteRide(String rideId) {
        return db.collection(COLLECTION_RIDES)
                .document(rideId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Ride deleted successfully");
                    rideHistory.remove(rideId);
                    saveToFirestore();
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error deleting ride", e));
    }

    public Task<Boolean> approvePassenger(String rideId, String passengerId) {
        DocumentReference rideRef = db.collection(COLLECTION_RIDES).document(rideId);

        return db.runTransaction(transaction -> {
            Map<String, Object> ride = transaction.get(rideRef).getData();
            if (ride == null) return false;

            List<String> passengers = (List<String>) ride.get("passengers");
            if (passengers == null) passengers = new ArrayList<>();

            long availableSeats = (long) ride.get("availableSeats");

            if (!passengers.contains(passengerId) && availableSeats > 0) {
                passengers.add(passengerId);
                transaction.update(rideRef, "passengers", passengers);

                transaction.update(rideRef, "availableSeats", availableSeats - 1);
            } else {
                return false;
            }
            return true;
        });
    }


    public Task<Boolean> removePassenger(String rideId, String passengerId) {
        DocumentReference rideRef = db.collection(COLLECTION_RIDES).document(rideId);

        return db.runTransaction(transaction -> {
            Map<String, Object> ride = transaction.get(rideRef).getData();
            if (ride == null) return false;

            List<String> passengers = (List<String>) ride.get("passengers");
            if (passengers != null && passengers.contains(passengerId)) {
                passengers.remove(passengerId);
                transaction.update(rideRef, "passengers", passengers);
            }
            return true;
        });
    }

    public Task<DocumentReference> ratePassenger(String passengerId, double rating, String comment) {
        Map<String, Object> ratingData = new HashMap<>();
        ratingData.put("driverId", id);
        ratingData.put("passengerId", passengerId);
        ratingData.put("rating", rating);
        ratingData.put("comment", comment);
        ratingData.put("timestamp",new Date());

        return db.collection(COLLECTION_RATINGS).add(ratingData);
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
    public String getLicenseNumber() { return licenseNumber; }
    public String getVehicleDetails() {return vehicleDetails; }
    public Map<String, String> getNotifications() { return new HashMap<>(notifications); }

    // Setters now update Firestore
    public void setFullName(String fullName) {
        this.fullName = fullName;
        saveToFirestore();
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
        saveToFirestore();
    }

    public void setVehicleDetails(String vehicleDetails) {
        this.vehicleDetails = vehicleDetails;
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
        return "Driver { " +
                "id = '" + id + '\'' +
                ", licenseNumber = '" + licenseNumber + '\'' +
                ", fullName = '" + fullName + '\'' +
                ", vehicleDetails = " + vehicleDetails +
                ", rating = " + rating +
                ", totalRides = " + totalRides +
                '}';
    }
}
