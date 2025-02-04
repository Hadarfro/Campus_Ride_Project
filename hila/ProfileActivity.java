package com.example.campusrideapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvFullName, tvEmail, tvId, tvPhone, tvRating;
    private Button btnRateDriver;
    private FirebaseFirestore firestore;

    private String userId; // ID של המשתמש בפרופיל

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // קבלת USER_ID מה-Intent
        userId = getIntent().getStringExtra("USER_ID");

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User ID is missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        firestore = FirebaseFirestore.getInstance();

        // טוענים את הממשק
        initializeUI();
        loadUserProfile();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            // טוען את פרטי המשתמש מחדש כדי להציג את הדירוג המעודכן
            loadUserProfile();
        }
    }

    private void initializeUI() {
        // קישור לממשק המשתמש
        tvFullName = findViewById(R.id.tv_fullName);
        tvEmail = findViewById(R.id.tv_email);
        tvId = findViewById(R.id.tv_id);
        tvPhone = findViewById(R.id.tv_phone);
        tvRating = findViewById(R.id.tv_rating);
        btnRateDriver = findViewById(R.id.btn_rate_driver);


        // טיפול בלחיצה על כפתור הדירוג
        btnRateDriver.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, RateDriverActivity.class);
            intent.putExtra("USER_ID", userId); // העברת userId ל־RateDriverActivity
            startActivityForResult(intent, 1);
        });
    }


    private void loadUserProfile() {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User ID is missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firestore = FirebaseFirestore.getInstance();
        firestore.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String fullName = document.getString("fullName");
                            String email = document.getString("email");
                            String id = document.getString("id");
                            String phone = document.getString("phoneNumber");
                            Double rating = document.getDouble("rating");
                            Integer ratingCount = document.getLong("ratingCount").intValue();

                            // הצגת הנתונים למשתמש
                            tvFullName.setText("Full Name: " + fullName);
                            tvEmail.setText("Email: " + email);
                            tvId.setText("ID: " + id);
                            tvPhone.setText("Phone Number: " + phone);
                            tvRating.setText("Rating: " + rating + " (" + ratingCount + " votes)");
                        } else {
                            Toast.makeText(this, "User document does not exist!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Failed to load profile.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

}

    }