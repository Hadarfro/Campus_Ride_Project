package com.example.campusrideapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class SignUp extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword,editTextFullName, editTextPhoneNumber, editTextId;
    Button buttonSignUp;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonSignUp = findViewById(R.id.btn_signUp);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.loginNow);
        editTextFullName = findViewById(R.id.fullName);
        editTextPhoneNumber = findViewById(R.id.phone_number);
        editTextId = findViewById(R.id.id_num);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String emailEdit, passwordEdit;
                emailEdit = String.valueOf(editTextEmail.getText());
                passwordEdit = String.valueOf(editTextPassword.getText());

                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String fullName = editTextFullName.getText().toString().trim();
                String phoneNumber = editTextPhoneNumber.getText().toString().trim();
                String id = editTextId.getText().toString().trim();


                if(TextUtils.isEmpty(email)){
                    Toast.makeText(SignUp.this, "Enter email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    Toast.makeText(SignUp.this, "Enter password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(fullName)) {
                    Toast.makeText(SignUp.this, "Enter your full name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (phoneNumber.isEmpty()) {
                    editTextPhoneNumber.setError("Phone number is required");
                    editTextPhoneNumber.requestFocus();
                    return;
                }

                if (id.isEmpty()) {
                    editTextId.setError("ID is required");
                    editTextId.requestFocus();
                    return;
                }


                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);

                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        // עדכון פרופיל המשתמש
                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(fullName)
                                                .build();

                                        user.updateProfile(profileUpdates)
                                                .addOnCompleteListener(profileTask -> {
                                                    if (profileTask.isSuccessful()) {
                                                        Log.d("SignUp", "User profile updated with full name.");
                                                    } else {
                                                        Log.d("SignUp", "Failed to update profile.");
                                                    }
                                                });

                                        // שמירת נתוני משתמש ב-Firestore
                                        String uid = user.getUid();
                                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                                        Map<String, Object> userProfile = new HashMap<>();
                                        userProfile.put("fullName", fullName);
                                        userProfile.put("email", email);
                                        userProfile.put("phoneNumber", phoneNumber);
                                        userProfile.put("id", id);
                                        userProfile.put("rating", 0.0); // דירוג התחלתי
                                        userProfile.put("ratingCount", 0);

                                        db.collection("users").document(uid).set(userProfile)
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("Firestore", "User profile saved with ID.");

                                                    // יצירת רשימת התראות
                                                    Map<String, Object> notification = new HashMap<>();
                                                    notification.put("message", "Welcome to CampusRide!");
                                                    notification.put("timestamp", System.currentTimeMillis());

                                                    db.collection("users").document(uid).collection("notifications")
                                                            .add(notification)
                                                            .addOnSuccessListener(docRef -> Log.d("Firestore", "Notification added successfully."))
                                                            .addOnFailureListener(e -> Log.e("Firestore", "Failed to add notification.", e));
                                                })
                                                .addOnFailureListener(e -> Log.e("Firestore", "Error saving user profile.", e));
                                    }

                                    Toast.makeText(SignUp.this, "Account created.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), Login.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(SignUp.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


            }
        });

    }
}
