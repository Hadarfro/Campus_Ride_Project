package com.example.myapplication;

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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.Login;
import com.example.myapplication.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {
    private static final String TAG = "GoogleSignIn";
    private GoogleSignInClient mGoogleSignInClient;

    TextInputEditText editTextEmail, editTextPassword, editTextFullName;
    TextInputEditText editTextID, editPhoneNum;
    Button buttonSignUp, googleBtn;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
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
        editTextID = findViewById(R.id.id_num);
        editPhoneNum = findViewById(R.id.phone_number);
        googleBtn = findViewById(R.id.btn_google_sign_in);
        googleBtn.setOnClickListener(v -> signInGoogle());

        textView.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Make sure this exists in strings.xml
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        buttonSignUp.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String fullName = editTextFullName.getText().toString().trim();
            String id = editTextID.getText().toString().trim();
            String PhoneNum = editPhoneNum.getText().toString().trim();

            // Checking for empty fields
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(SignUp.this, "Enter email", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(SignUp.this, "Enter password", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            if (TextUtils.isEmpty(fullName)) {
                Toast.makeText(SignUp.this, "Enter your full name", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            if (TextUtils.isEmpty(id)) {
                editTextID.setError("ID is required");
                progressBar.setVisibility(View.GONE);
                return;
            } else if (id.length() < 9) {
                editTextID.setError("ID must be at least 9 digits");
                progressBar.setVisibility(View.GONE);
                return;
            }

            if (TextUtils.isEmpty(PhoneNum)) {
                Toast.makeText(SignUp.this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(fullName)
                                        .build();

                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(profileTask -> {
                                            if (profileTask.isSuccessful()) {
                                                Log.d("SignUp", "User profile updated.");
                                            }
                                        });

                                String uid = user.getUid();
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                Map<String, Object> userProfile = new HashMap<>();
                                userProfile.put("fullName", fullName);
                                userProfile.put("email", email);
                                userProfile.put("phoneNumber", PhoneNum);
                                userProfile.put("id", id);

                                db.collection("users").document(uid).set(userProfile)
                                        .addOnSuccessListener(aVoid -> Log.d("Firestore", "User profile saved"))
                                        .addOnFailureListener(e -> Log.w("Firestore", "Error saving user profile", e));

                                Toast.makeText(SignUp.this, "Account created.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), Login.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            Toast.makeText(SignUp.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void signInGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    try {
                        GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(result.getData())
                                .getResult(ApiException.class);
                        firebaseAuthWithGoogle(account);
                    } catch (ApiException e) {
                        Log.w(TAG, "Google sign-in failed", e);
                        Toast.makeText(SignUp.this, "Google sign-in failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithCredential(GoogleAuthProvider.getCredential(acct.getIdToken(), null))
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Handle user sign-in success
                        Toast.makeText(SignUp.this, "Google Sign-In Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignUp.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(SignUp.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
