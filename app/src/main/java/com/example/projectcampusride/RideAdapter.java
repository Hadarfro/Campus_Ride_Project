package com.example.projectcampusride;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class RideAdapter extends ArrayAdapter<Ride> {
    public RideAdapter(Context context, List<Ride> rides) {
        super(context, 0, rides);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Ride ride = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.trip_item, parent, false);
        }

        TextView startEndLocation = convertView.findViewById(R.id.start_end_location);
        TextView availableSeats = convertView.findViewById(R.id.available_seats);
        TextView price = convertView.findViewById(R.id.trip_price);

        startEndLocation.setText(ride.getStartLocation() + " → " + ride.getEndLocation());
        availableSeats.setText("מקומות פנויים: " + ride.getAvailableSeats());
        price.setText(String.format("%.2f ₪", ride.getPrice()));

        return convertView;
    }
}
