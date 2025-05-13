package com.example.food.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.food.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG, "onActivityResult: ResultCode=" + result.getResultCode());
                    if (result.getResultCode() == RESULT_OK) {
                        Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class);
                            Log.d(TAG, "Google Sign-In successful, ID Token: " + signInAccount.getIdToken());
                            AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);
                            auth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "Firebase Auth successful, User: " + auth.getCurrentUser().getUid());
                                        Toast.makeText(LoginActivity.this, "Signed in successfully!", Toast.LENGTH_SHORT).show();
                                        // Lấy thông tin người dùng
                                        String userName = auth.getCurrentUser().getDisplayName();
                                        String userEmail = auth.getCurrentUser().getEmail();
                                        String userPhoto = auth.getCurrentUser().getPhotoUrl() != null ?
                                                auth.getCurrentUser().getPhotoUrl().toString() : "";

                                        // Chuyển sang MainActivity
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        intent.putExtra("name", userName);
                                        intent.putExtra("email", userEmail);
                                        intent.putExtra("photo", userPhoto);
                                        Log.d(TAG, "Starting MainActivity with name=" + userName + ", email=" + userEmail);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Log.e(TAG, "Firebase Auth failed: " + task.getException(), task.getException());
                                        Toast.makeText(LoginActivity.this, "Firebase Auth failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } catch (ApiException e) {
                            Log.e(TAG, "Google Sign-In failed: StatusCode=" + e.getStatusCode() + ", Message=" + e.getMessage(), e);
                            Toast.makeText(LoginActivity.this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.e(TAG, "Google Sign-In result not OK: " + result.getResultCode());
                        Toast.makeText(LoginActivity.this, "Google Sign-In canceled or failed", Toast.LENGTH_LONG).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "onCreate: LoginActivity started");

        // Khởi tạo Firebase
        auth = FirebaseAuth.getInstance();

        // Kiểm tra trạng thái đăng nhập
        if (auth.getCurrentUser() != null) {
            Log.d(TAG, "User already signed in: " + auth.getCurrentUser().getUid());
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("name", auth.getCurrentUser().getDisplayName());
            intent.putExtra("email", auth.getCurrentUser().getEmail());
            intent.putExtra("photo", auth.getCurrentUser().getPhotoUrl() != null ?
                    auth.getCurrentUser().getPhotoUrl().toString() : "");
            startActivity(intent);
            finish();
            return;
        }

        // Khởi tạo Google Sign-In
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .requestProfile()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, options);

        // Xử lý sự kiện bấm vào chữ "Đăng ký"
        TextView tvGoToSignup = findViewById(R.id.textView11);
        tvGoToSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Navigating to SignupActivity");
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        // Xử lý nút đăng nhập Google
        findViewById(R.id.imageView5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Google Sign-In button clicked");
                // Đăng xuất Google để yêu cầu chọn tài khoản
                googleSignInClient.signOut().addOnCompleteListener(task -> {
                    Log.d(TAG, "Google Sign-Out completed");
                    Intent signInIntent = googleSignInClient.getSignInIntent();
                    activityResultLauncher.launch(signInIntent);
                });
            }
        });
    }
}