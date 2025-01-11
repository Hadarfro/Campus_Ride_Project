package com.example.projectcampusride;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "SettingsPreferences";
    private SharedPreferences sharedPreferences;

    private RadioGroup languageGroup, themeGroup;
    private CheckBox notificationsCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize UI elements
        languageGroup = findViewById(R.id.languageGroup);
        themeGroup = findViewById(R.id.themeGroup);
        notificationsCheckBox = findViewById(R.id.notificationsCheckBox);
        Button backButton = findViewById(R.id.backButton);

        // Load saved settings
        loadSettings();

        // Handle language selection
        languageGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioEnglish) {
                setLocale("en");
            } else if (checkedId == R.id.radioHebrew) {
                setLocale("he");
            }
        });

        // Handle theme selection
        themeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioLight) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                saveThemePreference(false);
            } else if (checkedId == R.id.radioDark) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                saveThemePreference(true);
            }
        });

        // Handle notifications checkbox
        notificationsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("notifications_enabled", isChecked);
            editor.apply();
            Toast.makeText(this, isChecked ? "Notifications Enabled" : "Notifications Disabled", Toast.LENGTH_SHORT).show();
        });

        // Back button to return to the previous screen
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void loadSettings() {
        // Load language setting
        String language = sharedPreferences.getString("language", "en");
        if (language.equals("he")) {
            ((RadioButton) findViewById(R.id.radioHebrew)).setChecked(true);
        } else {
            ((RadioButton) findViewById(R.id.radioEnglish)).setChecked(true);
        }

        // Load theme setting
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            ((RadioButton) findViewById(R.id.radioDark)).setChecked(true);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            ((RadioButton) findViewById(R.id.radioLight)).setChecked(true);
        }

        // Load notifications setting
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true);
        notificationsCheckBox.setChecked(notificationsEnabled);
    }

    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        // Save language preference
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("language", languageCode);
        editor.apply();

        // Restart activity to apply changes
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    private void saveThemePreference(boolean isDarkMode) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("dark_mode", isDarkMode);
        editor.apply();
    }
}
