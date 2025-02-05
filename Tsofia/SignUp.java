package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
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

    private Button btnOpenCamera, btnUploadImage;
    private Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
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

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        progressBar = findViewById(R.id.progressBar);
        btnOpenCamera = findViewById(R.id.btn_open_camera);
        btnUploadImage = findViewById(R.id.btn_upload_image);

        // Permission request for Camera and Gallery
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }

        btnOpenCamera.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                cameraResultLauncher.launch(takePictureIntent);
            }
        });

        btnUploadImage.setOnClickListener(v -> {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryResultLauncher.launch(pickPhoto);
        });

        googleBtn.setOnClickListener(v -> signInGoogle());

        textView.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        buttonSignUp.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        progressBar.setVisibility(View.VISIBLE);
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String fullName = editTextFullName.getText().toString().trim();
        String id = editTextID.getText().toString().trim();
        String phoneNum = editPhoneNum.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(fullName) ||
                TextUtils.isEmpty(id) || TextUtils.isEmpty(phoneNum)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (id.length() < 9) {
            editTextID.setError("ID must be at least 9 digits");
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
                            user.updateProfile(profileUpdates);

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            Map<String, Object> userProfile = new HashMap<>();
                            userProfile.put("fullName", fullName);
                            userProfile.put("email", email);
                            userProfile.put("phoneNumber", phoneNum);
                            userProfile.put("id", id);

                            db.collection("users").document(user.getUid()).set(userProfile);
                            Toast.makeText(SignUp.this, "Account created.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), Login.class));
                            finish();
                        }
                    } else {
                        Toast.makeText(SignUp.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
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

    ActivityResultLauncher<Intent> cameraResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                    imageUri = getImageUri(this, photo);
                }
            }
    );

    ActivityResultLauncher<Intent> galleryResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                }
            }
    );

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null) {
            progressBar.setVisibility(View.VISIBLE);
            String userId = mAuth.getCurrentUser().getUid();
            StorageReference fileReference = storageReference.child("student_cards/" + userId + ".jpg");

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SignUp.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            saveStudentData(imageUrl);
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SignUp.this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void saveStudentData(String imageUrl) {
        String email = ((TextInputEditText) findViewById(R.id.email)).getText().toString();
        String fullName = ((TextInputEditText) findViewById(R.id.fullName)).getText().toString();
        String idNum = ((TextInputEditText) findViewById(R.id.id_num)).getText().toString();
        String phoneNumber = ((TextInputEditText) findViewById(R.id.phone_number)).getText().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> studentData = new HashMap<>();
        studentData.put("email", email);
        studentData.put("fullName", fullName);
        studentData.put("idNum", idNum);
        studentData.put("phoneNumber", phoneNumber);
        studentData.put("imageUrl", imageUrl);

        db.collection("students").document(mAuth.getCurrentUser().getUid())
                .set(studentData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SignUp.this, "Student registered successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SignUp.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(SignUp.this, "Google sign-in failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "title", null);
        return Uri.parse(path);
    }
}
