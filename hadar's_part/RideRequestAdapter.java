package com.example.projectcampusride;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class RideRequestAdapter extends ArrayAdapter<RideRequest> {
    private RideManager rideManager;

    public RideRequestAdapter(Context context, List<RideRequest> requests) {
        super(context, 0, requests);
        rideManager = RideManager.getInstance();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RideRequest request = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.trip_request_item, parent, false);
        }

        TextView passengerName = convertView.findViewById(R.id.passenger_name);
        TextView passengerPhone = convertView.findViewById(R.id.passenger_phone);
        Button approveButton = convertView.findViewById(R.id.approve_button);
        Button rejectButton = convertView.findViewById(R.id.reject_button);

       // passengerName.setText("שם נוסע: " + request.getPassengerName());
       // passengerPhone.setText("טלפון: " + request.getPassengerPhone());

        approveButton.setOnClickListener(v -> {
            rideManager.approveRequest(request.getId());
            Toast.makeText(getContext(), "בקשה אושרה", Toast.LENGTH_SHORT).show();
            remove(request);
        });

        rejectButton.setOnClickListener(v -> {
            rideManager.rejectRequest(request.getId());
            Toast.makeText(getContext(), "בקשה נדחתה", Toast.LENGTH_SHORT).show();
            remove(request);
        });

        return convertView;
    }
}