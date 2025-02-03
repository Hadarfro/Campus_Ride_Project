package com.example.campusride;

import com.example.campusride.models.Ride;
import com.example.campusride.models.User;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


public class Main {
    public static void main(String[] args) {
        // יצירת אובייקט RideManager
        RideManager rideManager = new RideManager();

        // הוספת הנסיעות ל-RideManager
        Ride ride1 = rideManager.createRide("R1", "Tel Aviv", "Jerusalem",LocalDate.of(2025, 1, 10), LocalTime.of(10, 30));
        Ride ride2 = rideManager.createRide("R2", "Haifa", "Tel Aviv", LocalDate.of(2025, 1, 10), LocalTime.of(15, 0));
        Ride ride3 =rideManager.createRide("R3", "Tel Aviv", "Jerusalem", LocalDate.of(2025, 1, 10), LocalTime.of(9, 0));


        // יצירת משתמשים לדוגמה
        User user1 = new Driver("U1", "Alice", "alice@example.com", "123456", true, 5.0 , "123456", "Car: Toyota");
        User user2 = new Passenger("U2", "Bob", "bob@example.com", "654321", true, 4.6, false);

        // הוספת משתמשים לנסיעות באמצעות UserRide
        rideManager.addUserToRide(user1.getUserId(), ride1.getRideId(), "Driver");
        rideManager.addUserToRide(user2.getUserId(), ride1.getRideId(), "Passenger");

        // בדיקת נסיעות לנהג
        List<Ride> driverRides = rideManager.getRidesForDriver(user1.getUserId());
        System.out.println("נסיעות לנהג " + user1.getName() + ":");
        for (Ride ride : driverRides) {
            System.out.println(ride);
        }

        // בדיקת משתמשים בנסיעה מסוימת
        List<User> usersInRide = rideManager.getUsersInRide(ride1.getRideId());
        System.out.println("\nמשתמשים בנסיעה " + ride1.getRideId() + ":");
        for (User user : usersInRide) {
            System.out.println(user);
        }

        // חיפוש נסיעות לפי מיקום וזמן
        System.out.println("\nחיפוש נסיעות מתל אביב לירושלים בין 8:00 ל-11:00:");
        List<Ride> filteredRides = rideManager.getRidesByTimeRange("Tel Aviv", "Jerusalem",
                LocalDate.of(2025, 1, 10), LocalTime.of(8, 0), LocalTime.of(11, 0));
        for (Ride ride : filteredRides) {
            System.out.println(ride);
        }
    }
}

