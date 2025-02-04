package com.example.campusrideapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class RateDriverActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private Button btnSubmitRating;
    private FirebaseFirestore firestore;

    private String userId; // ID של המשתמש שמדרגים

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rate_driver_activity);

        ratingBar = findViewById(R.id.ratingBar);
        btnSubmitRating = findViewById(R.id.btn_submit_rating);
        firestore = FirebaseFirestore.getInstance();

        // קבלת ID של המשתמש שמדרגים
        userId = getIntent().getStringExtra("USER_ID");

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User ID is missing!", Toast.LENGTH_SHORT).show();
            finish(); // סיום הפעילות אם ה-ID לא קיים
            return;
        }

        btnSubmitRating.setOnClickListener(v -> {
            float newRating = ratingBar.getRating();
            if (newRating == 0) {
                Toast.makeText(this, "Please select a rating!", Toast.LENGTH_SHORT).show();
                return;
            }

            submitRating(newRating);
        });
    }

    private void submitRating(float newRating) {
        firestore.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Double currentRating = task.getResult().getDouble("rating");
                        Integer currentCount = task.getResult().getLong("ratingCount").intValue();

                        // חישוב דירוג ממוצע חדש
                        double totalRating = currentRating * currentCount + newRating;
                        int newCount = currentCount + 1;
                        double updatedRating = totalRating / newCount;

                        // עדכון הדירוג ב-Firestore
                        firestore.collection("users").document(userId)
                                .update("rating", updatedRating, "ratingCount", newCount)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Rating submitted successfully!", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Failed to submit rating.", Toast.LENGTH_SHORT).show());
                    }
                });
    }
}
