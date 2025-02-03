package com.example.campusride;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;
import java.time.LocalTime;


public class CreateRideActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_ride);

        findViewById(R.id.submit_button).setOnClickListener(v -> {
            String startLocation = ((EditText) findViewById(R.id.start_location)).getText().toString();
            String endLocation = ((EditText) findViewById(R.id.end_location)).getText().toString();
            String date = ((EditText) findViewById(R.id.date_field)).getText().toString();
            String time = ((EditText) findViewById(R.id.time_field)).getText().toString();

            RideManager rideManager = new RideManager();
            rideManager.createRide("R1", "Tel Aviv", "Jerusalem", LocalDate.of(2025, 1, 10), LocalTime.of(10, 30));

            Toast.makeText(this, "נסיעה נוצרה בהצלחה!", Toast.LENGTH_SHORT).show();
        });
    }
}
