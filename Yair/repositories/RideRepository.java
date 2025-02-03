package com.example.campusride.repositories;

import android.util.Log;
import com.example.campusride.models.Ride;
import com.example.campusride.models.Passenger;
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

public class RideRepository {
    private static final String TAG = "RideRepository";
    private static final String COLLECTION_RIDES = "rides";
    private static final String COLLECTION_REQUESTS = "rideRequests";

    private static RideRepository instance;
    private final FirebaseFirestore db;

    private RideRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized RideRepository getInstance() {
        if (instance == null) {
            instance = new RideRepository();
        }
        return instance;
    }

    // ✅ יצירת נסיעה חדשה (בדיקת כפילות לפני יצירה)
    public Task<Void> createRide(String driverName, String startLocation, String endLocation, int availableSeats, double price) {
        String rideId = db.collection(COLLECTION_RIDES).document().getId();

        Map<String, Object> ride = new HashMap<>();
        ride.put("driverName", driverName);
        ride.put("startLocation", startLocation);
        ride.put("endLocation", endLocation);
        ride.put("availableSeats", availableSeats);
        ride.put("price", price);
        ride.put("timestamp", FieldValue.serverTimestamp());

        // בדיקת כפילות נסיעה קיימת
        return db.collection(COLLECTION_RIDES)
                .whereEqualTo("driverName", driverName)
                .whereEqualTo("startLocation", startLocation)
                .whereEqualTo("endLocation", endLocation)
                .get()
                .continueWithTask(task -> {
                    if (task.isSuccessful() && task.getResult().isEmpty()) {
                        return db.collection(COLLECTION_RIDES).document(rideId).set(ride);
                    } else {
                        return Tasks.forException(new Exception("A similar ride already exists"));
                    }
                });
    }

    // ✅ חיפוש נסיעות זמינות לפי מוצא ויעד
    public Task<List<Map<String, Object>>> searchRides(String startLocation, String endLocation) {
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

    // ✅ בקשת הצטרפות לנסיעה
    public Task<String> requestToJoinRide(String rideId, Passenger passenger) {
        Map<String, Object> request = new HashMap<>();
        request.put("rideId", rideId);
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

    // ✅ אישור בקשת נוסע
    public Task<Void> approvePassengerRequest(String requestId) {
        return db.runTransaction(transaction -> {
            DocumentSnapshot requestDoc = transaction.get(db.collection(COLLECTION_REQUESTS).document(requestId));

            if (!requestDoc.exists()) {
                throw new Exception("Request not found");
            }

            String rideId = requestDoc.getString("rideId");
            String passengerId = requestDoc.getString("passengerId");

            DocumentSnapshot rideDoc = transaction.get(db.collection(COLLECTION_RIDES).document(rideId));
            if (!rideDoc.exists()) {
                throw new Exception("Ride not found");
            }

            Long availableSeats = rideDoc.getLong("availableSeats");
            if (availableSeats == null || availableSeats <= 0) {
                throw new Exception("No seats available");
            }

            List<String> passengers = (List<String>) rideDoc.get("passengers");
            if (passengers == null) passengers = new ArrayList<>();
            passengers.add(passengerId);

            transaction.update(db.collection(COLLECTION_RIDES).document(rideId),
                    "availableSeats", availableSeats - 1,
                    "passengers", passengers);
            transaction.update(db.collection(COLLECTION_REQUESTS).document(requestId), "status", "APPROVED");

            return null;
        });
    }

    // ✅ דחיית בקשת נוסע
    public Task<Void> rejectPassengerRequest(String requestId) {
        return db.collection(COLLECTION_REQUESTS).document(requestId).update("status", "REJECTED");
    }

    // ✅ קבלת כל הבקשות עבור נהג מסוים
    public Task<List<Map<String, Object>>> getRequestsForDriver(String driverName) {
        return db.collection(COLLECTION_RIDES)
                .whereEqualTo("driverName", driverName)
                .get()
                .continueWithTask(task -> {
                    List<Task<QuerySnapshot>> requestTasks = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot rideDoc : task.getResult()) {
                            Task<QuerySnapshot> requestTask = db.collection(COLLECTION_REQUESTS)
                                    .whereEqualTo("rideId", rideDoc.getId())
                                    .whereEqualTo("status", "PENDING")
                                    .get();
                            requestTasks.add(requestTask);
                        }
                    }
                    return Tasks.whenAllSuccess(requestTasks);
                })
                .continueWith(task -> {
                    List<Map<String, Object>> requests = new ArrayList<>();
                    if (task.isSuccessful()) {
                        for (Object result : task.getResult()) {
                            if (result instanceof QuerySnapshot) {
                                for (DocumentSnapshot document : ((QuerySnapshot) result).getDocuments()) {
                                    Map<String, Object> request = document.getData();
                                    request.put("id", document.getId());
                                    requests.add(request);
                                }
                            }
                        }
                    }
                    return requests;
                });
    }
}
