package com.example.projectcampusride;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String id = "USER_ID"; // תמשכי את ה-ID מהזדהות המשתמש (Firebase Auth, למשל)



    // UI Components
    private TextView fullNameTextView, emailTextView, idTextView, universityTextView;
    private EditText editFullName, editEmail, editIdNumber, editUniversity;
    private Button editProfileButton, saveProfileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_profile);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();



        // Initialize UI Components
//        fullNameTextView = findViewById(R.id.profile_name);
//        emailTextView = findViewById(R.id.profile_email);
//        idTextView = findViewById(R.id.profile_id);
//        universityTextView = findViewById(R.id.profile_university);
//
//        editFullName = findViewById(R.id.edit_full_name);
//        editEmail = findViewById(R.id.edit_email);
//        editIdNumber = findViewById(R.id.edit_id_number);
//        editUniversity = findViewById(R.id.edit_university);
//
//        editProfileButton = findViewById(R.id.edit_profile_button);
//        saveProfileButton = findViewById(R.id.save_profile_button);

        // Load user data
        fetchUserData(id);

        // Edit Profile Button Logic
        editProfileButton.setOnClickListener(v -> {
            // Switch to Edit Mode
            toggleEditMode(true);
        });

        // Save Profile Button Logic
        saveProfileButton.setOnClickListener(v -> {
            // Get updated data from EditTexts
            String newFullName = editFullName.getText().toString();
            String newEmail = editEmail.getText().toString();
            String newIdNumber = editIdNumber.getText().toString();
            String newUniversity = editUniversity.getText().toString();

            // Validate inputs
            if (newFullName.isEmpty() || newEmail.isEmpty() || newIdNumber.isEmpty() || newUniversity.isEmpty()) {
                Toast.makeText(this, "אנא מלאי את כל השדות", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save data to Firestore
            saveUserData(id, newFullName, newEmail, newIdNumber, newUniversity);

            // Update UI and switch back to View Mode
            fullNameTextView.setText(newFullName);
            emailTextView.setText(newEmail);
            idTextView.setText(newIdNumber);
            universityTextView.setText(newUniversity);
            toggleEditMode(false);
        });
    }

    private void saveUserData(String userId, String fullName, String email, String idNumber, String university) {
        db.collection("passengers").document(userId)
                .update("fullName", fullName,
                        "email", email,
                        "idNumber", idNumber,
                        "university", university)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "הנתונים נשמרו בהצלחה!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "שגיאה בשמירת הנתונים: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void fetchUserData(String userId) {
        db.collection("passengers").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Fetch user data
                        String fullName = documentSnapshot.getString("fullName"); // שדה ב-Firestore
                        String email = documentSnapshot.getString("email");
                        String idNumber = documentSnapshot.getString("idNumber"); // שדה ב-Firestore
                        String university = documentSnapshot.getString("university");

                        // עדכון הממשק עם הנתונים
                        updateUI(fullName, email, idNumber, university);
                    } else {
                        Toast.makeText(this, "משתמש לא נמצא", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "שגיאה בשליפת נתונים: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUI(String fullName, String email, String idNumber, String university) {
        fullNameTextView.setText(fullName);
        emailTextView.setText(email);
        idTextView.setText(idNumber);
        universityTextView.setText(university);
    }

    private void toggleEditMode(boolean isEditMode) {
        if (isEditMode) {
            // Hide TextViews and show EditTexts
            fullNameTextView.setVisibility(View.GONE);
            emailTextView.setVisibility(View.GONE);
            idTextView.setVisibility(View.GONE);
            universityTextView.setVisibility(View.GONE);

            editFullName.setVisibility(View.VISIBLE);
            editEmail.setVisibility(View.VISIBLE);
            editIdNumber.setVisibility(View.VISIBLE);
            editUniversity.setVisibility(View.VISIBLE);

            saveProfileButton.setVisibility(View.VISIBLE);
            editProfileButton.setVisibility(View.GONE);

            // Copy data from TextViews to EditTexts
            editFullName.setText(fullNameTextView.getText().toString());
            editEmail.setText(emailTextView.getText().toString());
            editIdNumber.setText(idTextView.getText().toString());
            editUniversity.setText(universityTextView.getText().toString());
        } else {
            // Hide EditTexts and show TextViews
            fullNameTextView.setVisibility(View.VISIBLE);
            emailTextView.setVisibility(View.VISIBLE);
            idTextView.setVisibility(View.VISIBLE);
            universityTextView.setVisibility(View.VISIBLE);

            editFullName.setVisibility(View.GONE);
            editEmail.setVisibility(View.GONE);
            editIdNumber.setVisibility(View.GONE);
            editUniversity.setVisibility(View.GONE);

            saveProfileButton.setVisibility(View.GONE);
            editProfileButton.setVisibility(View.VISIBLE);
        }
    }
    //for update the rating write updateRating(5);when user give rating
    private void updateRating(int newRating) {
        // Get current rating and count of reviews from the database (mock example)
        int currentRating = 4; // Example: Existing average rating
        int totalReviews = 10; // Example: Total number of reviews

        // Calculate new average rating
        int updatedTotalReviews = totalReviews + 1;
        int updatedRatingSum = currentRating * totalReviews + newRating;
        int newAverageRating = updatedRatingSum / updatedTotalReviews;

        // Update rating in the database (replace with actual database logic)
        // Example:
        // database.updateUserRating(userId, newAverageRating, updatedTotalReviews);

        // Update UI
//        TextView ratingTextView = findViewById(R.id.current_rating);
//        ratingTextView.setText("הדירוג הנוכחי: " + newAverageRating + " כוכבים");

//        ImageView star1 = findViewById(R.id.star1);
//        ImageView star2 = findViewById(R.id.star2);
//        ImageView star3 = findViewById(R.id.star3);
//        ImageView star4 = findViewById(R.id.star4);
//        ImageView star5 = findViewById(R.id.star5);

        // Update star icons
//        updateStarRating(newAverageRating, star1, star2, star3, star4, star5);

        // Notify user
        Toast.makeText(this, "הדירוג עודכן בהצלחה!", Toast.LENGTH_SHORT).show();
    }


    private void updateStarRating(int rating, ImageView... stars) {
//        for (int i = 0; i < stars.length; i++) {
//            if (i < rating) {
//                stars[i].setImageResource(R.drawable.ic_star_filled); // Filled star
//            } else {
//                stars[i].setImageResource(R.drawable.ic_star_outline); // Outline star
//            }
//        }
    }

}
