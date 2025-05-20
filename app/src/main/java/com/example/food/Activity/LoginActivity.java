package com.example.food.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.auth.FacebookAuthProvider;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;

    // Add references to the UI elements
    private EditText userEmailEdt;
    private EditText userPasswordEdt;
    private Button loginButton;
    private TextView forgotPasswordText;

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
                                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                        // Lấy thông tin người dùng
                                        String userName = auth.getCurrentUser().getDisplayName();
                                        String userEmail = auth.getCurrentUser().getEmail();
                                        String userPhoto = auth.getCurrentUser().getPhotoUrl() != null ?
                                                auth.getCurrentUser().getPhotoUrl().toString() : "";

                                        // Chuyển sang HomeActivity và truyền dữ liệu
                                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                        intent.putExtra("name", userName);
                                        intent.putExtra("email", userEmail);
                                        intent.putExtra("photo", userPhoto);
                                        Log.d(TAG, "Starting HomeActivity with name=" + userName + ", email=" + userEmail);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Log.e(TAG, "Firebase Auth failed: " + task.getException(), task.getException());
                                        Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } catch (ApiException e) {
                            Log.e(TAG, "Google Sign-In failed: StatusCode=" + e.getStatusCode() + ", Message=" + e.getMessage(), e);
                            Toast.makeText(LoginActivity.this, "Đăng nhập Google thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.e(TAG, "Google Sign-In result not OK: " + result.getResultCode());
                        Toast.makeText(LoginActivity.this, "Đăng nhập Google bị hủy hoặc thất bại", Toast.LENGTH_LONG).show();
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

        // Khởi tạo Facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());
        callbackManager = CallbackManager.Factory.create();

        // Đăng xuất người dùng hiện tại để đảm bảo không có tự động đăng nhập
        if (auth.getCurrentUser() != null) {
            Log.d(TAG, "Signing out current user: " + auth.getCurrentUser().getUid());
            auth.signOut();

            // Đăng xuất khỏi Google nếu đã đăng nhập
            GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build())
                    .signOut().addOnCompleteListener(task -> {
                        Log.d(TAG, "Google sign out completed");
                    });

            // Đăng xuất khỏi Facebook nếu đã đăng nhập
            LoginManager.getInstance().logOut();

            Log.d(TAG, "User signed out successfully");
        }

        // Liên kết các thành phần UI
        userEmailEdt = findViewById(R.id.userEdt);
        userPasswordEdt = findViewById(R.id.passEdt);
        loginButton = findViewById(R.id.button4);
        forgotPasswordText = findViewById(R.id.textView8);

        // Xử lý nút đăng nhập bằng email và password
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Email/Password login button clicked");
                loginWithEmailPassword();
            }
        });

        // Xử lý chức năng quên mật khẩu
        forgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = userEmailEdt.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(LoginActivity.this, "Vui lòng nhập email trước", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendPasswordResetEmail(email);
            }
        });

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

        // Xử lý nút đăng nhập Google - FIX: sử dụng googleBtn thay vì imageView5
        findViewById(R.id.googleBtn).setOnClickListener(new View.OnClickListener() {
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

        // Xử lý nút đăng nhập Facebook - FIX: sử dụng facebookBtn thay vì imageView6
        findViewById(R.id.facebookBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Facebook Sign-In button clicked");
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile", "email"));
            }
        });

        // Đăng ký callback cho Facebook Login
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "Facebook Sign-In successful, Access Token: " + loginResult.getAccessToken().getToken());
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "Facebook Sign-In canceled");
                Toast.makeText(LoginActivity.this, "Đăng nhập Facebook bị hủy", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                Log.e(TAG, "Facebook Sign-In error: " + exception.getMessage(), exception);
                Toast.makeText(LoginActivity.this, "Lỗi đăng nhập Facebook: " + exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Xử lý đăng nhập bằng email và password
     */
    private void loginWithEmailPassword() {
        String email = userEmailEdt.getText().toString().trim();
        String password = userPasswordEdt.getText().toString().trim();

        Log.d("LOGIN_DEBUG", "Email: " + email);
        Log.d("LOGIN_DEBUG", "Password: " + password);

        // Kiểm tra email và password
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị một thông báo đang xử lý
        Toast.makeText(LoginActivity.this, "Đang đăng nhập...", Toast.LENGTH_SHORT).show();

        // Thực hiện đăng nhập với Firebase
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Đăng nhập thành công
                            Log.d(TAG, "signInWithEmail:success");
                            Toast.makeText(LoginActivity.this, "Đăng nhập thành công!",
                                    Toast.LENGTH_SHORT).show();

                            // Lấy thông tin người dùng
                            String userEmail = auth.getCurrentUser().getEmail();
                            String userName = auth.getCurrentUser().getDisplayName();
                            String userPhoto = auth.getCurrentUser().getPhotoUrl() != null ?
                                    auth.getCurrentUser().getPhotoUrl().toString() : "";

                            // Kiểm tra nếu displayName trống (người dùng đăng ký bằng email)
                            if (TextUtils.isEmpty(userName)) {
                                userName = userEmail.split("@")[0]; // Sử dụng phần đầu của email làm tên
                            }
                            // Chuyển sang HomeActivity và truyền dữ liệu
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            intent.putExtra("name", userName);
                            intent.putExtra("email", userEmail);
                            intent.putExtra("photo", userPhoto);
                            Log.d(TAG, "Starting HomeActivity with name=" + userName + ", email=" + userEmail);
                            startActivity(intent);
                            finish();

                        } else {
                            // Đăng nhập thất bại
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            String errorMessage = "Sai email hoặc mật khẩu";
                            if (task.getException() != null) {
                                errorMessage = task.getException().getMessage();
                            }
                            Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: " + errorMessage,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * Gửi email đặt lại mật khẩu
     */
    private void sendPasswordResetEmail(String email) {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Password reset email sent to " + email);
                            Toast.makeText(LoginActivity.this,
                                    "Đã gửi email đặt lại mật khẩu đến " + email,
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Log.w(TAG, "Failed to send password reset email", task.getException());
                            Toast.makeText(LoginActivity.this,
                                    "Không thể gửi email đặt lại mật khẩu: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Chuyển kết quả activity cho Facebook CallbackManager
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookAccessToken(com.facebook.AccessToken token) {
        Log.d(TAG, "Handling Facebook Access Token");
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Firebase Auth with Facebook successful, User: " + auth.getCurrentUser().getUid());
                    Toast.makeText(LoginActivity.this, "Đăng nhập Facebook thành công!", Toast.LENGTH_SHORT).show();
                    // Lấy thông tin người dùng
                    String userName = auth.getCurrentUser().getDisplayName();
                    String userEmail = auth.getCurrentUser().getEmail();
                    String userPhoto = auth.getCurrentUser().getPhotoUrl() != null ?
                            auth.getCurrentUser().getPhotoUrl().toString() : "";

                    // Chuyển sang HomeActivity và truyền dữ liệu
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    intent.putExtra("name", userName);
                    intent.putExtra("email", userEmail);
                    intent.putExtra("photo", userPhoto);
                    Log.d(TAG, "Starting HomeActivity with name=" + userName + ", email=" + userEmail);
                    startActivity(intent);
                    finish();
                } else {
                    Log.e(TAG, "Firebase Auth with Facebook failed: " + task.getException(), task.getException());
                    Toast.makeText(LoginActivity.this, "Đăng nhập Facebook thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

