package com.example.campusride.repositories;

import android.util.Log;
import androidx.annotation.NonNull;

import com.example.campusride.models.User;
import com.example.campusride.models.UserRole;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private static final String COLLECTION_USERS = "users";

    private static UserRepository instance;
    private final FirebaseFirestore db;

    private UserRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    public Task<Void> saveUserToFirestore(User user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", user.getUserId());
        userData.put("name", user.getName());
        userData.put("email", user.getEmail());
        userData.put("phone", user.getPhone());
        userData.put("validStudent", user.isValidStudent());
        userData.put("rateGrade", user.getRateGrade());
        userData.put("role", user.getRole().toString());
        userData.put("notifications", user.getNotifications());


        if (user.getRole() == UserRole.DRIVER) {
            userData.put("licenseNumber", user.getLicenseNumber());
            userData.put("vehicleDetails", user.getVehicleDetails());
        }


        return db.collection(COLLECTION_USERS)
                .document(user.getUserId())
                .set(userData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User saved successfully"))
                .addOnFailureListener(e -> Log.w(TAG, "Error saving user", e));
    }

    public Task<User> getUserById(String userId) {
        return db.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .continueWith(task -> {
                    DocumentSnapshot doc = task.getResult();
                    if (doc != null && doc.exists()) {
                        UserRole role = UserRole.fromString(doc.getString("role"));

                        User user = new User(
                                doc.getId(),
                                doc.getString("name"),
                                doc.getString("email"),
                                doc.getString("phone"),
                                doc.getBoolean("validStudent") != null ? doc.getBoolean("validStudent") : false,
                                doc.getDouble("rateGrade") != null ? doc.getDouble("rateGrade") : 0.0,
                                role
                        );

                        if (role == UserRole.DRIVER) {
                            user.setDriverDetails(
                                    doc.getString("licenseNumber"),
                                    doc.getString("vehicleDetails")
                            );
                        }

                        return user;
                    } else {
                        throw new RuntimeException("User not found");
                    }
                });
    }

    public Task<Void> updateUserRole(String userId, UserRole newRole) {
        return db.collection(COLLECTION_USERS)
                .document(userId)
                .update("role", newRole.toString())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User role updated successfully"))
                .addOnFailureListener(e -> Log.w(TAG, "Error updating user role", e));
    }

    public Task<Void> updateUserRating(String userId, double newRating) {
        return db.collection(COLLECTION_USERS)
                .document(userId)
                .update("rateGrade", newRating)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User rating updated successfully"))
                .addOnFailureListener(e -> Log.w(TAG, "Error updating rating", e));
    }

    public Task<Void> deleteUser(String userId) {
        return db.collection(COLLECTION_USERS)
                .document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User deleted successfully"))
                .addOnFailureListener(e -> Log.w(TAG, "Error deleting user", e));
    }

    public Task<Void> clearNotifications(String userId) {
        DocumentReference userRef = db.collection(COLLECTION_USERS).document(userId);

        return userRef.update("notifications", new HashMap<>())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "All notifications cleared for user: " + userId))
                .addOnFailureListener(e -> Log.w(TAG, "Failed to clear notifications", e));
    }
}






