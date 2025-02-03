package com.example.campusride;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;


public class DriverMainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_main);

        String driverId = "12345";

        findViewById(R.id.my_rides_button).setOnClickListener(v -> {
            Intent intent = new Intent(this, MyRidesActivity.class);
            intent.putExtra("driver_id", driverId);
            startActivity(intent);
        });

        findViewById(R.id.create_ride_button).setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateRideActivity.class);
            startActivity(intent);
        });
    }
}
