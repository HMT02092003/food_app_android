package com.example.food.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
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

    // Định nghĩa email của admin
    private static final String ADMIN_EMAIL = "admin@gmail.com";

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG, "onActivityResult: ResultCode=" + result.getResultCode());
                    if (result.getResultCode() == RESULT_OK) {
                        Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleGoogleSignInResult(accountTask);
                    } else {
                        Log.e(TAG, "Google Sign-In result not OK: " + result.getResultCode());
                        Toast.makeText(LoginActivity.this, "Đăng nhập Google bị hủy hoặc thất bại", Toast.LENGTH_LONG).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "onCreate: LoginActivity started");

        // Đặt màu cho Status Bar và đảm bảo nội dung không lấn lên
        getWindow().setStatusBarColor(Color.parseColor("#FF5722"));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

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
        configureGoogleSignIn();

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
        findViewById(R.id.googleBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Google Sign-In button clicked");
                signInWithGoogle();
            }
        });

        // Xử lý nút đăng nhập Facebook
        findViewById(R.id.facebookBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Facebook Sign-In button clicked");
                signInWithFacebook();
            }
        });

        // Đăng ký callback cho Facebook Login
        setupFacebookCallback();
    }

    /**
     * Cấu hình Google Sign-In
     */
    private void configureGoogleSignIn() {
        // Sử dụng web client ID từ strings.xml
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .requestProfile()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, options);
        
        // Kiểm tra xem đã có token ID chưa
        String clientId = getString(R.string.client_id);
        Log.d(TAG, "Client ID configured: " + (TextUtils.isEmpty(clientId) ? "EMPTY" : "OK"));
    }

    /**
     * Xử lý đăng nhập Google
     */
    private void signInWithGoogle() {
        // Đăng xuất khỏi tài khoản hiện tại trước khi hiển thị lại màn hình chọn tài khoản
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            Log.d(TAG, "Google Sign-Out completed before starting new sign-in flow");
            Intent signInIntent = googleSignInClient.getSignInIntent();
            activityResultLauncher.launch(signInIntent);
        });
    }

    /**
     * Xử lý kết quả đăng nhập Google
     */
    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d(TAG, "Google Sign-In successful, ID Token retrieved");
            
            // Kiểm tra token ID
            String idToken = account.getIdToken();
            if (idToken == null) {
                Log.e(TAG, "ID Token is null");
                Toast.makeText(this, "Không thể lấy thông tin xác thực từ Google", Toast.LENGTH_LONG).show();
                return;
            }
            
            Log.d(TAG, "ID Token length: " + idToken.length());
            
            // Xác thực với Firebase bằng token ID
            firebaseAuthWithGoogle(idToken);
        } catch (ApiException e) {
            // Xử lý lỗi Google Sign-In với thông báo cụ thể
            Log.e(TAG, "Google Sign-In failed: StatusCode=" + e.getStatusCode() + ", Message=" + e.getMessage(), e);
            String errorMessage;
            switch (e.getStatusCode()) {
                case 12501: // Người dùng hủy
                    errorMessage = "Bạn đã hủy đăng nhập";
                    break;
                case 7: // Lỗi kết nối mạng
                    errorMessage = "Vui lòng kiểm tra kết nối mạng";
                    break;
                case 10: // Developer error
                    errorMessage = "Lỗi cấu hình, vui lòng liên hệ quản trị viên";
                    break;
                default:
                    errorMessage = "Lỗi đăng nhập: " + e.getMessage();
            }
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Xác thực với Firebase bằng Google ID Token
     */
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Firebase Auth successful with Google");
                            navigateToAppropriateScreen();
                        } else {
                            Log.e(TAG, "Firebase Auth with Google failed", task.getException());
                            Toast.makeText(LoginActivity.this, "Xác thực thất bại: " + 
                                    (task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định"), 
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * Điều hướng người dùng đến màn hình phù hợp sau khi đăng nhập thành công
     */
    private void navigateToAppropriateScreen() {
        // Lấy thông tin người dùng
        String userName = auth.getCurrentUser().getDisplayName();
        String userEmail = auth.getCurrentUser().getEmail();
        String userPhoto = auth.getCurrentUser().getPhotoUrl() != null ?
                auth.getCurrentUser().getPhotoUrl().toString() : "";
                
        Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

        // Kiểm tra nếu email là email admin
        if (ADMIN_EMAIL.equals(userEmail)) {
            // Nếu là admin thì chuyển đến AdminActivity
            Intent intent = new Intent(LoginActivity.this, AdminFoodActivity.class);
            intent.putExtra("name", userName);
            intent.putExtra("email", userEmail);
            intent.putExtra("photo", userPhoto);
            Log.d(TAG, "Starting AdminActivity with name=" + userName + ", email=" + userEmail);
            startActivity(intent);
        } else {
            // Nếu không phải admin thì chuyển đến HomeActivity như bình thường
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.putExtra("name", userName);
            intent.putExtra("email", userEmail);
            intent.putExtra("photo", userPhoto);
            Log.d(TAG, "Starting HomeActivity with name=" + userName + ", email=" + userEmail);
            startActivity(intent);
        }
        
        finish();
    }

    /**
     * Xử lý đăng nhập Facebook
     */
    private void signInWithFacebook() {
        // Đăng xuất khỏi Facebook trước khi đăng nhập lại
        LoginManager.getInstance().logOut();
        
        // Yêu cầu quyền truy cập
        LoginManager.getInstance().logInWithReadPermissions(
                LoginActivity.this, 
                Arrays.asList("public_profile", "email"));
    }

    /**
     * Thiết lập callback cho Facebook Login
     */
    private void setupFacebookCallback() {
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "Facebook Sign-In successful");
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

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Đăng nhập với Firebase
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Email/password login successful");
                    navigateToAppropriateScreen();
                } else {
                    Log.e(TAG, "Email/password login failed", task.getException());
                    Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: " + 
                            (task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định"), 
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Gửi email đặt lại mật khẩu
     */
    private void sendPasswordResetEmail(String email) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Email đặt lại mật khẩu đã được gửi", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Không thể gửi email đặt lại mật khẩu: " + 
                            (task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định"), 
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Xử lý kết quả đăng nhập Facebook
     */
    private void handleFacebookAccessToken(com.facebook.AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Facebook authentication successful");
                    navigateToAppropriateScreen();
                } else {
                    Log.e(TAG, "Facebook authentication failed", task.getException());
                    Toast.makeText(LoginActivity.this, "Xác thực Facebook thất bại: " + 
                            (task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định"), 
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Hỗ trợ cho Facebook Login
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // Chuyển kết quả đến Facebook SDK (cho phương thức đăng nhập cũ)
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}