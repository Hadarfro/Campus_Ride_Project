package com.example.campusrideapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class NotificationsAdapter extends ArrayAdapter<Map<String, Object>> {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final String userId;

    public NotificationsAdapter(@NonNull Context context, @NonNull List<Map<String, Object>> notifications, String userId) {
        super(context, 0, notifications);
        this.userId = userId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_notification, parent, false);
        }

        Map<String, Object> notification = getItem(position);

        TextView messageView = convertView.findViewById(R.id.tv_message);
        Button acceptButton = convertView.findViewById(R.id.btn_accept);
        Button declineButton = convertView.findViewById(R.id.btn_decline);

        String message = (String) notification.get("message");
        String type = (String) notification.get("type");
        String notificationId = (String) notification.get("id");

        messageView.setText(message);

        if ("decision".equals(type)) {
            // הצגת כפתורים להתראות מסוג decision
            acceptButton.setVisibility(View.VISIBLE);
            declineButton.setVisibility(View.VISIBLE);

            acceptButton.setOnClickListener(v -> updateNotificationStatus(notificationId, "accepted"));
            declineButton.setOnClickListener(v -> updateNotificationStatus(notificationId, "declined"));
        } else {
            // הסתרת כפתורים להתראות מסוג info
            acceptButton.setVisibility(View.GONE);
            declineButton.setVisibility(View.GONE);
        }

        return convertView;
    }

    private void updateNotificationStatus(String notificationId, String status) {
        firestore.collection("users").document(userId).collection("notifications")
                .document(notificationId)
                .update("status", status)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Notification " + status, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to update notification.", Toast.LENGTH_SHORT).show();
                });
    }
}
