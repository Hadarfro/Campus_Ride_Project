package com.example.projectcampusride;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RideDetailsActivity extends AppCompatActivity {
    private RideManager rideManager;
    private Ride selectedRide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        rideManager = RideManager.getInstance();
        selectedRide = (Ride) getIntent().getSerializableExtra("SELECTED_TRIP");

        // הצגת פרטי הנסיעה
        TextView tripDetails = findViewById(R.id.trip_details);
        tripDetails.setText(String.format("מסלול: %s → %s\nנהג: %s\nמקומות פנויים: %d\nמחיר: %.2f ₪",
                selectedRide.getStartLocation(),
                selectedRide.getEndLocation(),
                selectedRide.getDriverName(),
                selectedRide.getAvailableSeats(),
                selectedRide.getPrice()));

        Button requestJoinButton = findViewById(R.id.request_join_button);
        requestJoinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // כאן תהיה הזדהות משתמש אמיתית - זו דוגמה פשוטה
                Passenger passenger = new Passenger("0","ישראל ישראלי",
                        "israel@gmail.com", "050-00000000");

                if (selectedRide.getAvailableSeats() > 0) {
                    rideManager.requestToJoinTrip(
                            selectedRide.getId(),
                            passenger
                    );
                    Toast.makeText(RideDetailsActivity.this,
                            "בקשת הצטרפות נשלחה לנהג",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RideDetailsActivity.this,
                            "אין מקומות פנויים בנסיעה",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
