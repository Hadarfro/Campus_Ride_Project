package com.example.projectcampusride.controllers;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectcampusride.models.Ride;
import com.example.projectcampusride.models.UserRole;
import com.example.projectcampusride.repositories.RideRepository;

import java.security.PublicKey;

public class RideController {
    private RideRepository rideRepository;

    public RideController(){
        this.rideRepository = RideRepository.getInstance();
    }

    public void createRide(Ride ride) {
        rideRepository.createRide(ride);
    }

    public void searchRide(String startLocation, String endLocation){

    }

}
