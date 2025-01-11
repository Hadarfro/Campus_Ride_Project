package com.example.projectcampusride;

import android.app.Notification;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class NotificationsActivity extends AppCompatActivity {
    private static final String TAG = "NotificationsActivity";

    // Firebase Firestore
    private FirebaseFirestore db;

    // ListView
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private ArrayList<NotificationModel> notificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_notifications);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // אתחול הרשימה
        notificationList = new ArrayList<>();

//        recyclerView = findViewById(R.id.notificationsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotificationAdapter(this, notificationList);
        recyclerView.setAdapter(adapter);
        // Load notifications
        loadNotifications();

        adapter.setOnApproveListener(notification -> {
            updateNotificationStatus(notification, "approved");
            Toast.makeText(this, "נסיעה אושרה!", Toast.LENGTH_SHORT).show();
        });

        adapter.setOnRefuseListener(notification -> {
            updateNotificationStatus(notification, "refused");
            Toast.makeText(this, "נסיעה סורבה.", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadNotifications() {
        db.collection("passengers")
                .document("USER_ID") // TODO: Replace with the current user's ID
                .collection("notification")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading notifications", error);
                        return;
                    }

                    if (value != null) {
                        for (DocumentChange doc : value.getDocumentChanges()) {
                            switch (doc.getType()) {
                                case ADDED:
                                    NotificationModel notification = doc.getDocument().toObject(NotificationModel.class);
                                    notificationList.add(notification);
                                    adapter.notifyDataSetChanged();
                                    break;
                                case REMOVED:
                                    // Handle notification removal if necessary
                                    break;
                            }
                        }
                    }
                });
    }

    private void updateNotificationStatus(NotificationModel notification, String status) {
        db.collection("passengers")
                .document(notification.getRecipientId())
                .collection("notifications")
                .document(notification.getId())
                .update("status", status)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Notification updated"))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating notification", e));
    }
}
