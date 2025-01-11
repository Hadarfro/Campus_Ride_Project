package com.example.projectcampusride;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ProgressBar;
import android.widget.ImageButton;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private EditText startLocationEdit;
    private EditText endLocationEdit;
    private ListView tripsListView;
    private ProgressBar progressBar;
    private RideManager rideManager;
    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        setContentView(R.layout.activity_main);

        // Initialize Firestore
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        rideManager = RideManager.getInstance();

        // Initialize UI components
        startLocationEdit = findViewById(R.id.start_location_edit);
        endLocationEdit = findViewById(R.id.end_location_edit);
        tripsListView = findViewById(R.id.trips_list_view);
        progressBar = findViewById(R.id.progress_bar);
        Button searchButton = findViewById(R.id.search_button);

        ImageButton settingsButton = findViewById(R.id.settings_button);

        settingsButton.setOnClickListener(v -> {
            // Navigate to Settings Activity
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        ImageButton notificationButton = findViewById(R.id.notification_button);

        notificationButton.setOnClickListener(v -> {
            // Show Toast or navigate to Notification Activity
            Intent intent = new Intent(MainActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });


        // Create sample rides
        // Check for first-time setup
        db.collection("rides").get().addOnSuccessListener(querySnapshot -> {
            if (querySnapshot.isEmpty()) {
                createSampleRides();
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchRides();
            }
        });

        // Load initial rides
//        loadAllRides();
    }

    private void createSampleRides() {
        db.collection("rides")
                .whereEqualTo("driverName", "דן כהן") // Check for an existing sample ride
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        // Only add rides if they don't already exist
                        rideManager.createTrip("מיכל לוי", "Tel Aviv", "Haifa", 3, 55.00);
                        rideManager.createTrip("יוסי אברהם", "Tel Aviv", "Be'er Sheva", 2, 65.00);

                        // Jerusalem area rides
                        rideManager.createTrip("רחל גולן", "Jerusalem", "Tel Aviv", 3, 45.00);
                        rideManager.createTrip("משה דוד", "Jerusalem", "Dead Sea", 4, 35.00);

                        // Haifa area rides
                        rideManager.createTrip("נועה שלום", "Haifa", "Tel Aviv", 2, 55.00);
                        rideManager.createTrip("אדם כהן", "Haifa", "Tiberias", 3, 40.00);

                        // Be'er Sheva area rides
                        rideManager.createTrip("שירה לוי", "Be'er Sheva", "Tel Aviv", 4, 65.00);
                        rideManager.createTrip("עומר פרץ", "Be'er Sheva", "Eilat", 3, 75.00);

                        // Other cities
                        rideManager.createTrip("טל אבני", "Netanya", "Tel Aviv", 2, 30.00);
                        rideManager.createTrip("גיל שפירא", "Rishon LeZion", "Jerusalem", 3, 40.00);
                        rideManager.createTrip("ליאור כץ", "Ashdod", "Tel Aviv", 4, 35.00);
                        rideManager.createTrip("מאי רוזן", "Ariel", "Mudi'in", 4, 15.00);
                        rideManager.createTrip("רון ציוני", "Hadra", "Ariel", 4, 30.00);
                        rideManager.createTrip("רוני פורסט", "Ariel", "Kadomim", 4, 15.00);
                        rideManager.createTrip("טל גוב", "Ariel", "Alichin", 4, 35.00);
                    } else {
                            Log.d(TAG, "Sample rides already exist, skipping creation.");
                        }
                    })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking sample rides", e);
                });
//        // Tel Aviv area rides
//        rideManager.createTrip("דן כהן", "Tel Aviv", "Jerusalem", 4, 45.00);
//
//        rideManager.createTrip("מיכל לוי", "Tel Aviv", "Haifa", 3, 55.00);
//
//        rideManager.createTrip("יוסי אברהם", "Tel Aviv", "Be'er Sheva", 2, 65.00);
//
//        // Jerusalem area rides
//        rideManager.createTrip("רחל גולן", "Jerusalem", "Tel Aviv", 3, 45.00);
//
//        rideManager.createTrip("משה דוד", "Jerusalem", "Dead Sea", 4, 35.00);
//
//        // Haifa area rides
//        rideManager.createTrip("נועה שלום", "Haifa", "Tel Aviv", 2, 55.00);
//
//        rideManager.createTrip("אדם כהן", "Haifa", "Tiberias", 3, 40.00);
//
//        // Be'er Sheva area rides
//        rideManager.createTrip("שירה לוי", "Be'er Sheva", "Tel Aviv", 4, 65.00);
//
//        rideManager.createTrip("עומר פרץ", "Be'er Sheva", "Eilat", 3, 75.00);
//
//        // Other cities
//        rideManager.createTrip("טל אבני", "Netanya", "Tel Aviv", 2, 30.00);
//
//        rideManager.createTrip("גיל שפירא", "Rishon LeZion", "Jerusalem", 3, 40.00);
//
//        rideManager.createTrip("ליאור כץ", "Ashdod", "Tel Aviv", 4, 35.00);
    }


    private void searchRides() {
        String startLocation = startLocationEdit.getText().toString().trim();
        String endLocation = endLocationEdit.getText().toString().trim();

        if (startLocation.isEmpty() || endLocation.isEmpty()) {
            showToast("Please enter both locations");
            return;
        }

        showProgress(true);
        showToast("Searching for: " + startLocation + " to " + endLocation);

        db.collection("rides")
                .whereEqualTo("startLocation", startLocation)
                .whereEqualTo("endLocation", endLocation)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    showProgress(false);
                    showToast("Found " + queryDocumentSnapshots.size() + " rides");

                    Set<Map<String, Object>> matchingRides = new HashSet<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        matchingRides.add(document.getData());
                    }
                    showToast("number of rides: " + matchingRides.size());
                    updateRidesList(new ArrayList<>(matchingRides));
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    showToast("Error: " + e.getMessage());
                    Log.e(TAG, "Error searching rides", e);
                });
    }


    private void loadAllRides() {
        showProgress(true);

        rideManager.getAllTrips()
                .addOnSuccessListener(rides -> {
                    showProgress(false);
                    updateRidesList(rides);
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    Log.e(TAG, "Error loading rides", e);
                    showToast("Error loading rides");
                });
    }

    private void updateRidesList(List<Map<String, Object>> rides) {
        List<Ride> rideList = new ArrayList<>();
        // Clear the ListView before updating
        tripsListView.setAdapter(null);

        for (Map<String, Object> rideData : rides) {
            String driverName = (String) rideData.get("driverName");
            String startLocation = (String) rideData.get("startLocation");
            String endLocation = (String) rideData.get("endLocation");
            Long seats = (Long) rideData.get("availableSeats");
            Double price = (Double) rideData.get("price");
            String rideId = (String) rideData.get("id");  // Make sure to get the ID

            if (driverName != null && startLocation != null && endLocation != null &&
                    seats != null && price != null) {
                Ride ride = new Ride(driverName, startLocation, endLocation,
                        seats.intValue(), price);  // Update Ride constructor to include ID
                rideList.add(ride);
            }
        }
        RideAdapter adapter = new RideAdapter(MainActivity.this, rideList);
        adapter.setOnJoinClickListener(this::handleJoinRequest);
        tripsListView.setAdapter(adapter);


        if (rideList.isEmpty()) {
            showToast("No rides found");
        }
    }

    private void handleJoinRequest(Ride ride) {
        // Get the current logged-in user's information (need to implement this!!!)
        // some sample passenger should be the user connected
        String userId = "USER_ID";  // Replace with actual user ID
        String userEmail = "user@example.com";  // Replace with actual email
        String userPhone = "1234567890";  // Replace with actual phone
        String userName = "User Name";  // Replace with actual name

        // Create a dialog to confirm joining
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Join Ride")
                .setMessage("Would you like to join this ride?\n" +
                        "From: " + ride.getStartLocation() + "\n" +
                        "To: " + ride.getEndLocation() + "\n" +
                        "Price: ₪" + ride.getPrice())
                .setPositiveButton("Join", (dialog, which) -> {
                    // Show progress dialog
                    ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setMessage("Sending request...");
                    progressDialog.show();

                    // Create passenger object
                    Passenger passenger = new Passenger(userId, userName, userEmail, userPhone);

                    // Try to book the ride
                    passenger.bookRide(ride.getId())
                            .addOnSuccessListener(success -> {
                                progressDialog.dismiss();
                                if (success) {
                                    showToast("Successfully joined the ride!");
                                    // Refresh the rides list to show updated seat count
                                    loadAllRides();
                                } else {
                                    showToast("No seats available");
                                }
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                showToast("Failed to join ride: " + e.getMessage());
                                Log.e(TAG, "Error joining ride", e);
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showProgress(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


}