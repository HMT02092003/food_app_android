package com.example.food.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.food.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class PersonInfoUpdateActivity extends AppCompatActivity {

    private EditText userNameEdit, userEmailEdit, userPhoneEdit, userBioEdit;
    private ImageView avatarEdit, backButton;
    private Button saveButton;
    private TextView titleTextView;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private Uri selectedImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_info_detail); // sử dụng lại layout cũ

        // Khởi tạo FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Không có người dùng đang đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Ánh xạ View
        userNameEdit = findViewById(R.id.userNameDetail);
        userEmailEdit = findViewById(R.id.userEmailDetail);
        userPhoneEdit = findViewById(R.id.userPhoneDetail);
        userBioEdit = findViewById(R.id.userBioDetail); // Bạn có thể lưu bio riêng bằng SharedPreferences hoặc Realtime Database
        avatarEdit = findViewById(R.id.avatarDetail);
        backButton = findViewById(R.id.backButton);
        saveButton = findViewById(R.id.saveButton);
        titleTextView = findViewById(R.id.title);
        titleTextView.setText("Sửa thông tin");

        // Load thông tin hiện tại từ currentUser
        if (currentUser.getDisplayName() != null)
            userNameEdit.setText(currentUser.getDisplayName());
        if (currentUser.getEmail() != null)
            userEmailEdit.setText(currentUser.getEmail());
        if (currentUser.getPhotoUrl() != null)
            Glide.with(this).load(currentUser.getPhotoUrl()).into(avatarEdit);

        // Bắt sự kiện quay lại
        backButton.setOnClickListener(v -> finish());

        // Bắt sự kiện chọn ảnh
        avatarEdit.setOnClickListener(view -> {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhoto, 1);
        });

        // Bắt sự kiện lưu
        saveButton.setOnClickListener(v -> updateUserProfile());
    }

    private void updateUserProfile() {
        String newName = userNameEdit.getText().toString().trim();
        String newEmail = userEmailEdit.getText().toString().trim();

        // Cập nhật tên + avatar
        UserProfileChangeRequest.Builder profileUpdatesBuilder = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName);
        if (selectedImageUri != null) {
            profileUpdatesBuilder.setPhotoUri(selectedImageUri);
        }

        currentUser.updateProfile(profileUpdatesBuilder.build())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Cập nhật hồ sơ thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Lỗi cập nhật hồ sơ", Toast.LENGTH_SHORT).show();
                    }
                });

        // Cập nhật Email
        if (!newEmail.equals(currentUser.getEmail())) {
            currentUser.updateEmail(newEmail)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Email đã được cập nhật", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Lỗi khi cập nhật Email", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Nhận ảnh đại diện mới từ thư viện
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                Glide.with(this).load(selectedImageUri).into(avatarEdit);
            }
        }
    }
}
