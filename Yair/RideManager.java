package com.example.campusride;

import com.example.campusride.models.Ride;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RideManager {
    private static final String TAG = "RideManager";
    private static final String COLLECTION_RIDES = "rides";
    private static final String COLLECTION_REQUESTS = "rideRequests";

    private static RideManager instance;
    private final FirebaseFirestore db;

    protected RideManager() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized RideManager getInstance() {
        if (instance == null) {
            instance = new RideManager();
        }
        return instance;
    }

    public Task<Void> createTrip(String driverName, String startLocation, String endLocation, int availableSeats
            , double price) {
        // Generate a unique ID for the ride
        String rideId = db.collection("rides").document().getId();

        Map<String, Object> ride = new HashMap<>();
        //ride.put("id", rideId);
        ride.put("driverName", driverName);
        ride.put("startLocation", startLocation);
        ride.put("endLocation", endLocation);
        ride.put("availableSeats", availableSeats);
        ride.put("price", price);
        ride.put("timestamp", FieldValue.serverTimestamp());  // Add timestamp for tracking

        // First check if a similar ride exists
        return db.collection("rides")
                .whereEqualTo("driverName", driverName)
                .whereEqualTo("startLocation", startLocation)
                .whereEqualTo("endLocation", endLocation)
                .get()
                .continueWithTask(task -> {
                    if (task.isSuccessful() && task.getResult().isEmpty()) {
                        // No duplicate found, create the new ride
                        return db.collection("rides").document(rideId).set(ride);
                    } else {
                        // Duplicate found, return a failed task
                        return Tasks.forException(new Exception("A similar ride already exists"));
                    }
                });
    }


    public Task<List<Map<String, Object>>> searchTrips(String startLocation, String endLocation) {
        return db.collection(COLLECTION_RIDES)
                .whereEqualTo("startLocation", startLocation)
                .whereEqualTo("endLocation", endLocation)
                .whereGreaterThan("availableSeats", 0)
                .whereEqualTo("status", "ACTIVE")
                .get()
                .continueWith(task -> {
                    List<Map<String, Object>> results = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> ride = document.getData();
                            ride.put("id", document.getId());
                            results.add(ride);
                        }
                    }
                    return results;
                });
    }

    public Task<List<Map<String, Object>>> getAllTrips() {
        return db.collection(COLLECTION_RIDES)
                .whereEqualTo("status", "ACTIVE")
                .get()
                .continueWith(task -> {
                    List<Map<String, Object>> rides = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> ride = document.getData();
                            ride.put("id", document.getId());
                            rides.add(ride);
                        }
                    }
                    return rides;
                });
    }

    public Task<String> requestToJoinTrip(String tripId, Passenger passenger) {
        Map<String, Object> request = new HashMap<>();
        request.put("tripId", tripId);
        request.put("passengerId", passenger.getId());
        request.put("passengerName", passenger.getFullName());
        request.put("status", "PENDING");
        request.put("createdAt", com.google.firebase.Timestamp.now());

        return db.collection(COLLECTION_REQUESTS)
                .add(request)
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        return task.getResult().getId();
                    } else {
                        throw new Exception("Failed to create request");
                    }
                });
    }

    public Task<Void> approveRequest(String requestId) {
        return db.runTransaction(transaction -> {
            // Get the request document
            DocumentSnapshot requestDoc = transaction.get(
                    db.collection(COLLECTION_REQUESTS).document(requestId)
            );

            if (!requestDoc.exists()) {
                try {
                    throw new Exception("Request not found");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            String tripId = requestDoc.getString("tripId");
            String passengerId = requestDoc.getString("passengerId");

            // Get the ride document
            DocumentSnapshot rideDoc = transaction.get(
                    db.collection(COLLECTION_RIDES).document(tripId)
            );

            if (!rideDoc.exists()) {
                try {
                    throw new Exception("Ride not found");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            // Update available seats
            Long currentSeats = rideDoc.getLong("availableSeats");
            if (currentSeats == null || currentSeats <= 0) {
                try {
                    throw new Exception("No seats available");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            // Get current passengers list
            List<String> passengers = (List<String>) rideDoc.get("passengers");
            if (passengers == null) {
                passengers = new ArrayList<>();
            }
            passengers.add(passengerId);

            // Update the ride document
            transaction.update(db.collection(COLLECTION_RIDES).document(tripId),
                    "availableSeats", currentSeats - 1,
                    "passengers", passengers);

            // Update the request status
            transaction.update(db.collection(COLLECTION_REQUESTS).document(requestId),
                    "status", "APPROVED");

            return null;
        });
    }

    public Task<Void> rejectRequest(String requestId) {
        return db.collection(COLLECTION_REQUESTS)
                .document(requestId)
                .update("status", "REJECTED");
    }

    public Task<List<Map<String, Object>>> getTripRequestsForDriver(String driverName) {
        return db.collection(COLLECTION_RIDES)
                .whereEqualTo("driverName", driverName)
                .get()
                .continueWithTask(rideTask -> {
                    if (!rideTask.isSuccessful() || rideTask.getResult() == null) {
                        throw new Exception("Failed to get rides");
                    }

                    List<Task<QuerySnapshot>> requestTasks = new ArrayList<>();

                    // Safely iterate through the QuerySnapshot
                    for (DocumentSnapshot rideDoc : rideTask.getResult().getDocuments()) {
                        Task<QuerySnapshot> requestTask = db.collection(COLLECTION_REQUESTS)
                                .whereEqualTo("tripId", rideDoc.getId())
                                .whereEqualTo("status", "PENDING")
                                .get();
                        requestTasks.add(requestTask);
                    }

                    return Tasks.whenAllSuccess(requestTasks);
                })
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw new Exception("Failed to get requests");
                    }

                    List<Map<String, Object>> requests = new ArrayList<>();
                    List<Object> results = task.getResult();

                    // Safely process each QuerySnapshot
                    for (Object result : results) {
                        if (result instanceof QuerySnapshot) {
                            QuerySnapshot snapshot = (QuerySnapshot) result;
                            for (DocumentSnapshot document : snapshot.getDocuments()) {
                                Map<String, Object> request = new HashMap<>(document.getData());
                                request.put("id", document.getId());
                                requests.add(request);
                            }
                        }
                    }
                    return requests;
                });
    }

    // Helper method to convert Firestore document to Ride object
    private Ride documentToRide(DocumentSnapshot doc) {
        String driverName = doc.getString("driverName");
        String startLocation = doc.getString("startLocation");
        String endLocation = doc.getString("endLocation");
        Long seats = doc.getLong("availableSeats");
        Double price = doc.getDouble("price");
        String id = doc.getString("id");

        if (driverName == null || startLocation == null || endLocation == null ||
                seats == null || price == null) {
            return null;
        }

        return new Ride(driverName, startLocation, endLocation, seats.intValue(), price);
    }
}



