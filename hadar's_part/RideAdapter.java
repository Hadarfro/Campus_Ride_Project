package com.example.projectcampusride;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import java.util.List;

public class RideAdapter extends ArrayAdapter<Ride> {
        private final List<Ride> rides;
        private OnJoinClickListener joinClickListener;

        // Interface for click handling
        public interface OnJoinClickListener {
            void onJoinClick(Ride ride);
        }

        public RideAdapter(Context context, List<Ride> rides) {
            super(context, 0, rides);
            this.rides = rides;
        }

        public void setOnJoinClickListener(OnJoinClickListener listener) {
            this.joinClickListener = listener;
        }

    public void addAll(List<Ride> newRides) {
        rides.clear(); // Ensure old rides are cleared
        rides.addAll(newRides); // Add new rides
        notifyDataSetChanged();
    }


    @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Ride ride = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.trip_item, parent, false);
            }

            TextView driverNameView = convertView.findViewById(R.id.driver_name);
            TextView startEndLocation = convertView.findViewById(R.id.start_end_location);
            TextView availableSeats = convertView.findViewById(R.id.available_seats);
            TextView price = convertView.findViewById(R.id.trip_price);
            Button joinButton = convertView.findViewById(R.id.join_button);

            driverNameView.setText("שם הנהג: " + ride.getDriverName());
            startEndLocation.setText(ride.getStartLocation() + " → " + ride.getEndLocation());
            availableSeats.setText("מקומות פנויים: " + ride.getAvailableSeats());
            price.setText(String.format("%.2f ₪", ride.getPrice()));

            joinButton.setOnClickListener(v -> {
                if (joinClickListener != null) {
                    joinClickListener.onJoinClick(ride);
                }
            });

            return convertView;
        }

    public void clear() {
        rides.clear();
        notifyDataSetChanged();
    }

}