package com.example.projectcampusride;

import android.util.Log;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Ride implements Serializable {
    private static final String TAG = "Ride";
    private static final String COLLECTION_RIDES = "rides";

    private final FirebaseFirestore db;
    private String id;
    private String driverName;
    private String startLocation;
    private String endLocation;
    private int availableSeats;
    private Date departureTime;
    private double price;
    private List<String> passengerIds; // Store passenger IDs instead of objects

    public Ride(String driverName, String startLocation, String endLocation,
                int availableSeats, double price) {
        this.db = FirebaseFirestore.getInstance();
        this.id = UUID.randomUUID().toString();
        this.driverName = driverName;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.availableSeats = availableSeats;
        this.departureTime = new Date();
        this.price = price;
        this.passengerIds = new ArrayList<>();

        // Save to Firestore upon creation
        saveToFirestore();
    }

    // Constructor from Firestore document
    public static Task<Ride> fromFirestore(DocumentSnapshot document) {
        String driverName = document.getString("driverName");
        String startLocation = document.getString("startLocation");
        String endLocation = document.getString("endLocation");
        Long seats = document.getLong("availableSeats");
        Double price = document.getDouble("price");

        if (driverName == null || startLocation == null || endLocation == null ||
                seats == null || price == null) {
            return Tasks.forResult(null);  // Changed from Task.forResult to Tasks.forResult
        }

        Ride ride = new Ride(driverName, startLocation, endLocation, seats.intValue(), price);
        ride.id = document.getId();

        Timestamp timestamp = document.getTimestamp("departureTime");
        if (timestamp != null) {
            ride.departureTime = timestamp.toDate();
        }

        List<String> passengers = (List<String>) document.get("passengerIds");
        if (passengers != null) {
            ride.passengerIds = passengers;
        }

        return Tasks.forResult(ride);
    }

    private void saveToFirestore() {
        Map<String, Object> rideData = new HashMap<>();
        rideData.put("driverName", driverName);
        rideData.put("startLocation", startLocation);
        rideData.put("endLocation", endLocation);
        rideData.put("availableSeats", availableSeats);
        rideData.put("departureTime", new Timestamp(departureTime));
        rideData.put("price", price);
        rideData.put("passengerIds", passengerIds);

        if (id != null) {
            db.collection(COLLECTION_RIDES)
                    .document(id)
                    .set(rideData)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Ride saved successfully"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error saving ride", e));
        } else {
            db.collection(COLLECTION_RIDES)
                    .add(rideData)
                    .addOnSuccessListener(documentReference -> {
                        id = documentReference.getId();
                        Log.d(TAG, "Ride created with ID: " + id);
                    })
                    .addOnFailureListener(e -> Log.w(TAG, "Error creating ride", e));
        }
    }

    // Getters remain the same
    public String getId() { return id; }
    public String getDriverName() { return driverName; }
    public String getStartLocation() { return startLocation; }
    public String getEndLocation() { return endLocation; }
    public int getAvailableSeats() { return availableSeats; }
    public Date getDepartureTime() { return departureTime; }
    public double getPrice() { return price; }

    // Modified to return passenger IDs
    public List<String> getPassengerIds() {
        return new ArrayList<>(passengerIds);
    }

    // Modified to work with Firestore
    public Task<Boolean> addPassenger(Passenger passenger) {
        if (availableSeats <= 0) {
            return Tasks.forResult(false);
        }

        return db.runTransaction(transaction -> {
            DocumentReference rideRef = db.collection(COLLECTION_RIDES).document(id);
            DocumentSnapshot rideDoc = transaction.get(rideRef);

            if (!rideDoc.exists()) {
                try {
                    throw new Exception("Ride does not exist");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            Long currentSeats = rideDoc.getLong("availableSeats");
            if (currentSeats == null || currentSeats <= 0) {
                return false;
            }

            List<String> currentPassengers = (List<String>) rideDoc.get("passengerIds");
            if (currentPassengers == null) {
                currentPassengers = new ArrayList<>();
            }

            currentPassengers.add(passenger.getId());

            transaction.update(rideRef,
                    "availableSeats", currentSeats - 1,
                    "passengerIds", currentPassengers);

            // Update local state
            availableSeats--;
            passengerIds = currentPassengers;

            return true;
        });
    }

    public boolean hasAvailableSeats() {
        return this.availableSeats > 0;
    }

    public Task<Boolean> removePassenger(Passenger passenger) {
        return db.runTransaction(transaction -> {
            DocumentReference rideRef = db.collection(COLLECTION_RIDES).document(id);
            DocumentSnapshot rideDoc = transaction.get(rideRef);

            if (!rideDoc.exists()) {
                try {
                    throw new Exception("Ride does not exist");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            List<String> currentPassengers = (List<String>) rideDoc.get("passengerIds");
            Long currentSeats = rideDoc.getLong("availableSeats");

            if (currentPassengers == null || currentSeats == null) {
                return false;
            }

            if (currentPassengers.remove(passenger.getId())) {
                transaction.update(rideRef,
                        "availableSeats", currentSeats + 1,
                        "passengerIds", currentPassengers);

                // Update local state
                availableSeats++;
                passengerIds = currentPassengers;
                return true;
            }

            return false;
        });
    }

    public void updateDepartureTime(Date newDepartureTime) {
        this.departureTime = newDepartureTime;
        saveToFirestore();
    }

    public void updatePrice(double newPrice) {
        this.price = newPrice;
        saveToFirestore();
    }
}