package com.example.campusrideapp;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NotificationUtils {

    public static void addNotification(String userId, String message, String type) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        Map<String, Object> notification = new HashMap<>();
        notification.put("message", message);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("type", type);
        notification.put("status", "pending");

        firestore.collection("users").document(userId).collection("notifications")
                .add(notification)
                .addOnSuccessListener(docRef -> Log.d("Firestore", "Notification added successfully: " + docRef.getId()))
                .addOnFailureListener(e -> Log.e("Firestore", "Failed to add notification.", e));
    }

    public static void updateExistingNotifications(String userId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("users").document(userId).collection("notifications")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String notificationId = document.getId();

                            firestore.collection("users").document(userId).collection("notifications")
                                    .document(notificationId)
                                    .update("type", "info", "status", "pending")
                                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Notification updated: " + notificationId))
                                    .addOnFailureListener(e -> Log.e("Firestore", "Failed to update notification: " + notificationId, e));
                        }
                    }
                });
    }
}
