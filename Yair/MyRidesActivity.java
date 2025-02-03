package com.example.campusride;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusride.models.Ride;

import java.util.List;

public class MyRidesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_rides);

        // קבלת driverId מה-Intent
        String driverId = getIntent().getStringExtra("driver_id");
        if (driverId == null) {
            driverId = ""; // טיפול במקרה שהמזהה לא הועבר
        }

        RecyclerView recyclerView = findViewById(R.id.rides_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // שימוש ב-RideManager להציג רשימת נסיעות
        RideManager rideManager = new RideManager(); // או למשוך ממקור אחר
        List<Ride> rides = rideManager.getRidesForDriver(driverId);

        MyAdapter adapter = new MyAdapter(rides);
        recyclerView.setAdapter(adapter);
    }
}

