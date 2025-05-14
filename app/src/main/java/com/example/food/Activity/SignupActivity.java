package com.example.food.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.food.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";
    private EditText etFullName, etEmail, etPassword, etRePassword;
    private Button btnLogin;
    private CheckBox cbRemember;
    private TextView tvForgotPassword, tvGoToLogin;

    // Firebase Auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ view
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etRePassword = findViewById(R.id.etRePassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Bắt sự kiện đăng ký
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Bắt sự kiện chuyển sang màn hình đăng nhập
        tvGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Nếu không muốn quay lại SignupActivity khi nhấn nút back
            }
        });

        // Xử lý khi người dùng quên mật khẩu
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    private void registerUser() {
        // Lấy thông tin từ EditText
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String rePassword = etRePassword.getText().toString().trim();

        // Kiểm tra thông tin
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Vui lòng nhập họ và tên");
            etFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(rePassword)) {
            etRePassword.setError("Mật khẩu nhập lại không khớp");
            etRePassword.requestFocus();
            return;
        }

        // Hiển thị loading (có thể thêm ProgressBar)
        btnLogin.setEnabled(false);
        btnLogin.setText("Đang xử lý...");

        // Kiểm tra email tồn tại
        checkEmailExists(email, fullName, password);
    }

    private void checkEmailExists(final String email, final String fullName, final String password) {
        // Phương thức để kiểm tra email đã tồn tại trong hệ thống chưa
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(new OnCompleteListener<>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.auth.SignInMethodQueryResult> task) {
                        if (task.isSuccessful()) {
                            boolean isNewUser = task.getResult().getSignInMethods().isEmpty();
                            if (isNewUser) {
                                // Email chưa được sử dụng, tiến hành đăng ký
                                createAccount(email, password, fullName);
                            } else {
                                // Email đã tồn tại
                                btnLogin.setEnabled(true);
                                btnLogin.setText("ĐĂNG KÝ");
                                etEmail.setError("Email này đã được sử dụng");
                                etEmail.requestFocus();
                                Toast.makeText(SignupActivity.this,
                                        "Email này đã tồn tại trong hệ thống",
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            // Có lỗi xảy ra khi kiểm tra email
                            btnLogin.setEnabled(true);
                            btnLogin.setText("ĐĂNG KÝ");
                            Toast.makeText(SignupActivity.this,
                                    "Không thể kiểm tra email. Vui lòng thử lại sau",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void createAccount(String email, String password, String fullName) {
        // Đăng ký tài khoản với Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Đăng ký thành công
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            // Cập nhật profile với fullName
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(fullName)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "User profile updated.");
                                                Toast.makeText(SignupActivity.this, "Đăng ký thành công!",
                                                        Toast.LENGTH_SHORT).show();

                                                FirebaseAuth.getInstance().signOut();

                                                // Chuyển tới màn hình đăng nhập
                                                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                        Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                                        Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }
                                    });
                        } else {
                            // Đăng ký thất bại
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            btnLogin.setEnabled(true);
                            btnLogin.setText("ĐĂNG KÝ");

                            String errorMessage = "Đăng ký thất bại. Vui lòng thử lại.";
                            if (task.getException() != null) {
                                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    // Email đã tồn tại trong hệ thống
                                    errorMessage = "Email này đã được sử dụng bởi một tài khoản khác";
                                } else {
                                    errorMessage = task.getException().getMessage();
                                }
                            }
                            Toast.makeText(SignupActivity.this, errorMessage,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}