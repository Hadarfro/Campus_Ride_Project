package com.example.campusride.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campusride.R;
import com.example.campusride.controllers.RoleSelectionController;
import com.example.campusride.models.UserRole;

public class RoleSelectionActivity extends AppCompatActivity {
    private RoleSelectionController roleSelectionController;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        roleSelectionController = new RoleSelectionController();

        //userId = getIntent().getStringExtra("USER_ID");
        userId = "JXC2GvsPvhBcC0REPJ9F";
        findViewById(R.id.driver_button).setOnClickListener(v -> selectRole(userId,UserRole.DRIVER));
        findViewById(R.id.passenger_button).setOnClickListener(v -> selectRole(userId,UserRole.PASSENGER));
    }

    private void selectRole(String userId,UserRole role) {
        roleSelectionController.setUserRole(userId, role, success -> {
            if (success) {
                navigateToNextScreen(role);
            } else {
                Toast.makeText(this, "Failed to set role. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void navigateToNextScreen(UserRole role) {
        Intent intent;
        if (role == UserRole.DRIVER) {
            intent = new Intent(this, DriverMainActivity.class);
        } else {
            intent = new Intent(this, PassengerMainActivity.class);
        }
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
        finish();
    }
}

