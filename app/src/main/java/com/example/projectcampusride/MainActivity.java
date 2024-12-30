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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        db = FirebaseFirestore.getInstance();
        rideManager = RideManager.getInstance();

        // Initialize UI components
        startLocationEdit = findViewById(R.id.start_location_edit);
        endLocationEdit = findViewById(R.id.end_location_edit);
        tripsListView = findViewById(R.id.trips_list_view);
        progressBar = findViewById(R.id.progress_bar);
        Button searchButton = findViewById(R.id.search_button);

        // Create sample rides
//        createSampleRides();

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchRides();
            }
        });

        // Load initial rides
        loadAllRides();
    }

    private void createSampleRides() {
//        db.collection("rides")
//                .whereEqualTo("driverName", "אלון") // Check if a specific sample ride exists
//                .get()
//                .addOnSuccessListener(querySnapshot -> {
//                    if (querySnapshot.isEmpty()) {
//                        // Create sample rides only if they don't exist
//                        rideManager.createTrip("אלון", "Tel Aviv", "Jerusalem", 3, 45.50);
//                        rideManager.createTrip("שרה", "Haifa", "Be'er Sheva", 2, 60.00);
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    Log.e(TAG, "Error checking sample rides", e);
//                });
        // Tel Aviv area rides
        rideManager.createTrip("דן כהן", "Tel Aviv", "Jerusalem", 4, 45.00);

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
    }


    private void searchRides() {
        tripsListView.setAdapter(null); // Clear adapter before every search
        String startLocation = startLocationEdit.getText().toString().trim();
        String endLocation = endLocationEdit.getText().toString().trim();

        if (startLocation.isEmpty() || endLocation.isEmpty()) {
            showToast("Please enter both locations");
            return;
        }

        showProgress(true);

        db.collection("rides")
                .whereEqualTo("startLocation", startLocation)
                .whereEqualTo("endLocation", endLocation)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    showProgress(false);
                    List<Map<String, Object>> matchingRides = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        matchingRides.add(document.getData());
                    }

                    // Update the list view with fresh data
                    updateRidesList(matchingRides);
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    Log.e(TAG, "Error searching rides", e);
                    showToast("Error searching rides");
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
