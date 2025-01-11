package com.example.projectcampusride;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MatchActivity extends AppCompatActivity {

    // Declare UI elements
    private Button btnShowDriverNumber, btnPayment, btnRateDriver, btnBack;
    private ImageView settingsIcon, notificationIcon;

    // Variable for driver's phone number
    private String driverPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        // Get driver's phone number from Intent
        Intent intent = getIntent();
        driverPhoneNumber = intent.getStringExtra("DRIVER_PHONE_NUMBER");

        // Initialize Buttons
        initializeButtons();

        // Initialize Icons
        initializeIcons();

        // Set Click Listeners for Buttons
        setButtonListeners();

        // Set Click Listeners for Icons
        setIconListeners();
    }

    /**
     * Initialize Button Views.
     */
    private void initializeButtons() {
        btnShowDriverNumber = findViewById(R.id.showDriverNumberButton);
        btnPayment = findViewById(R.id.paymentButton);
        btnRateDriver = findViewById(R.id.rateDriverButton);
        btnBack = findViewById(R.id.previousScreenButton);
    }

    /**
     * Initialize Icon Views.
     */
    private void initializeIcons() {
        settingsIcon = findViewById(R.id.settingsIcon);
        notificationIcon = findViewById(R.id.notificationIcon);
    }

    /**
     * Set Click Listeners for Buttons.
     */
    private void setButtonListeners() {
        // Show Driver Number
        btnShowDriverNumber.setOnClickListener(v -> {
            if (driverPhoneNumber != null && !driverPhoneNumber.isEmpty()) {
                Toast.makeText(this, "מספר הטלפון של הנהג הוא: " + driverPhoneNumber, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "מספר הנהג אינו זמין", Toast.LENGTH_SHORT).show();
            }
        });

        // Payment Button
        btnPayment.setOnClickListener(v -> {
            Toast.makeText(this, "מעבר לתשלום בביט", Toast.LENGTH_SHORT).show();

            // Open Bit payment app
            Intent launchBitIntent = getPackageManager().getLaunchIntentForPackage("il.co.isracard.bit");
            if (launchBitIntent != null) {
                startActivity(launchBitIntent); // Launch the Bit app
            } else {
                Toast.makeText(this, "Bit app is not installed on your device", Toast.LENGTH_LONG).show();
            }
        });

        // Rate Driver
        btnRateDriver.setOnClickListener(v ->
                Toast.makeText(this, "פתיחת חלון דירוג נהג", Toast.LENGTH_SHORT).show()
        );

//        // Back Button - Navigate to MainActivity
//        btnBack.setOnClickListener(v -> {
//            Toast.makeText(this, "חוזר למסך הקודם", Toast.LENGTH_SHORT).show();
//            Intent intent = new Intent(MatchActivity.this, MainActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//            startActivity(intent);
//            finish();
//        });
        // Back button to return to the previous screen
        btnBack.setOnClickListener(v -> onBackPressed());

    }

    /**
     * Set Click Listeners for Icons.
     */
    private void setIconListeners() {

        settingsIcon.setOnClickListener(v -> {
            // Navigate to Settings Activity
            Intent intent = new Intent(MatchActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        notificationIcon.setOnClickListener(v -> {
            // Show Toast or navigate to Notification Activity
            Intent intent = new Intent(MatchActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });
    }
}
