package com.example.projectcampusride;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText startLocationEdit;
    private EditText endLocationEdit;
    private ListView tripsListView;
    private RideManager rideManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rideManager = RideManager.getInstance();

        // creating fake rides
        rideManager.createTrip("אלון", "תל אביב", "ירושלים", 3, 45.50);
        rideManager.createTrip("שרה", "חיפה", "באר שבע", 2, 60.00);

        startLocationEdit = findViewById(R.id.start_location_edit);
        endLocationEdit = findViewById(R.id.end_location_edit);
        tripsListView = findViewById(R.id.trips_list_view);
        Button searchButton = findViewById(R.id.search_button);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String startLocation = startLocationEdit.getText().toString();
                String endLocation = endLocationEdit.getText().toString();

                List<Ride> searchResults = rideManager.searchTrips(startLocation, endLocation);
                RideAdapter adapter = new RideAdapter(MainActivity.this, searchResults);
                tripsListView.setAdapter(adapter);
            }
        });
    }
}