package com.example.campusrideapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainMenuActivity extends AppCompatActivity {

    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);


        // Buttons
        Button btnProfile = findViewById(R.id.btn_profile);
        Button btnDriver = findViewById(R.id.btn_driver);
        Button btnSearchRide = findViewById(R.id.btn_search_ride);
        Button btnLogout = findViewById(R.id.btn_logout);
        Button btnNotifications = findViewById(R.id.btn_notifications);


        // Profile Button Click
        btnProfile.setOnClickListener(v -> {
            if (currentUser != null) {
                String userId = currentUser.getUid(); // ה־UID של המשתמש המחובר
                Intent intent = new Intent(MainMenuActivity.this, ProfileActivity.class);
                intent.putExtra("USER_ID", userId); // שליחה ל־ProfileActivity
                startActivity(intent);
            } else {
                Toast.makeText(MainMenuActivity.this, "User is not logged in!", Toast.LENGTH_SHORT).show();
            }
        });



        // Driver Button Click
        btnDriver.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Search Ride Button Click
        btnSearchRide.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Logout Button Click
        btnLogout.setOnClickListener(v -> {

            FirebaseAuth.getInstance().signOut();
            Toast.makeText(MainMenuActivity.this, "You have been logged out.", Toast.LENGTH_SHORT).show();

            // Clear session or shared preferences if needed
            Intent intent = new Intent(MainMenuActivity.this, Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // מחיקת היסטוריה
            startActivity(intent);
            finish();
        });


        btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, NotificationsActivity.class);
            intent.putExtra("USER_ID", currentUser.getUid()); // העברת userId ל-NotificationsActivity
            startActivity(intent);
        });
    }
}

