package com.example.projectcampusride.view;

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

import com.example.projectcampusride.NotificationsActivity;
import com.example.projectcampusride.R;
import com.example.projectcampusride.RideAdapter;
import com.example.projectcampusride.SettingsActivity;
import com.example.projectcampusride.models.RideStatus;
import com.example.projectcampusride.models.UserRole;
import com.example.projectcampusride.repositories.RideRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.projectcampusride.models.Ride;
import com.example.projectcampusride.models.User;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PassengerMainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String COLLECTION_RIDES = "rides";

    private EditText startLocationEdit;
    private EditText endLocationEdit;
    private ListView ridesListView;
    private ProgressBar progressBar;
    private RideRepository rideManager;
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
        rideManager = RideRepository.getInstance();

        // Initialize UI components
        startLocationEdit = findViewById(R.id.start_location_edit);
        endLocationEdit = findViewById(R.id.end_location_edit);
        ridesListView = findViewById(R.id.rides_list_view);
        progressBar = findViewById(R.id.progress_bar);
        Button searchButton = findViewById(R.id.search_button);

        ImageButton settingsButton = findViewById(R.id.settings_button);

        settingsButton.setOnClickListener(v -> {
            // Navigate to Settings Activity
            Intent intent = new Intent(PassengerMainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        ImageButton notificationButton = findViewById(R.id.notification_button);

        notificationButton.setOnClickListener(v -> {
            // Show Toast or navigate to Notification Activity
            Intent intent = new Intent(PassengerMainActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });

        // Search button click listener to find rides
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchRides();
            }
        });

    }

    private void createSampleRides() {
        db.collection(COLLECTION_RIDES)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // Only add rides if they don't already exist
                    Ride ride = new Ride("1234", "111", "Tel Aviv", "Haifa", "02/03/25", "12:00", 3, 55.0, RideStatus.ACTIVE);
                    rideManager.createRide(ride);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking sample rides", e);
                });
    }


    private void searchRides() {
        String startLocation = startLocationEdit.getText().toString().trim();
        String endLocation = endLocationEdit.getText().toString().trim();

        if (startLocation.isEmpty() || endLocation.isEmpty()) {
            showToast("Please enter both locations");
            return;
        }

        showProgress(true);
        showToast("Searching ride from: " + startLocation + " to " + endLocation);
//        rideManager.searchRides(startLocation, endLocation);

        db.collection(COLLECTION_RIDES)
                .whereEqualTo("startLocation", startLocation)
                .whereEqualTo("endLocation", endLocation)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    showProgress(false);

                    Set<Map<String, Object>> matchingRides = new HashSet<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        matchingRides.add(document.getData());
                    }
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
        ridesListView.setAdapter(null);
        rideManager.updateRidesList(rides,rideList);
        
        RideAdapter adapter = new RideAdapter(PassengerMainActivity.this, rideList);
        adapter.setOnJoinClickListener(this::handleJoinRequest);
        ridesListView.setAdapter(adapter);


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
                    User passenger = new User(userId, userName, userPhone, userEmail, false, 5.0, UserRole.PASSENGER);

                    // Try to book the ride
//                    passenger.bookRide(ride.getRideId())
//                            .addOnSuccessListener(success -> {
//                                progressDialog.dismiss();
//                                if (success) {
//                                    showToast("Successfully joined the ride!");
//                                    // Refresh the rides list to show updated seat count
//                                    loadAllRides();
//                                }
//                                else {
//                                    showToast("No seats available");
//                                }
//                            })
//                            .addOnFailureListener(e -> {
//                                progressDialog.dismiss();
//                                showToast("Failed to join ride: " + e.getMessage());
//                                Log.e(TAG, "Error joining ride", e);
//                            });
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
