package com.example.food.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.food.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class PersonInfoDetailActivity extends AppCompatActivity {

    private EditText userNameDetail, userEmailDetail, userPhoneDetail, userBioDetail, userAddressDetail;
    private ImageView avatarDetail, backButton, editAvatarIcon;
    private TextView titleTextView;
    private Button saveButton;

    private String photoUrl;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;
    private String userId;

    private static final int REQUEST_CODE_PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_info_detail);

        // Ánh xạ view
        userNameDetail = findViewById(R.id.userNameDetail);
        userEmailDetail = findViewById(R.id.userEmailDetail);
        userPhoneDetail = findViewById(R.id.userPhoneDetail);
        userBioDetail = findViewById(R.id.userBioDetail);
        userAddressDetail = findViewById(R.id.userAddressDetail);
        avatarDetail = findViewById(R.id.avatarDetail);
        editAvatarIcon = findViewById(R.id.editPencil);
        titleTextView = findViewById(R.id.title);
        backButton = findViewById(R.id.backButton);
        saveButton = findViewById(R.id.saveButton);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        userId = firebaseUser != null ? firebaseUser.getUid() : null;

        loadUserInfo();

        // Quay lại
        backButton.setOnClickListener(view -> finish());

        // Chọn ảnh từ thư viện
        avatarDetail.setOnClickListener(view -> {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhoto, REQUEST_CODE_PICK_IMAGE);
        });

        // Nhập URL ảnh
        editAvatarIcon.setOnClickListener(view -> showImageUrlDialog());

        // Lưu thông tin người dùng
        saveButton.setOnClickListener(v -> updateFirebaseUser());
    }

    private void loadUserInfo() {
        if (firebaseUser != null && userId != null) {
            DocumentReference userRef = db.collection("user").document(userId);
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("name");
                    String email = documentSnapshot.getString("email");
                    String phone = documentSnapshot.getString("phone");
                    String bio = documentSnapshot.getString("bio");
                    String photo = documentSnapshot.getString("photo");
                    String address = documentSnapshot.getString("address");
                    titleTextView.setText("Sửa thông tin");
                    userNameDetail.setText(name != null ? name : "");
                    userEmailDetail.setText(email != null ? email : "");
                    userPhoneDetail.setText(phone != null ? phone : "");
                    userBioDetail.setText(bio != null ? bio : "");
                    userAddressDetail.setText(address != null ? address : "");
                    if (photo != null && !photo.isEmpty()) {
                        photoUrl = photo;
                        Glide.with(this).load(photoUrl).into(avatarDetail);
                    } else {
                        avatarDetail.setImageResource(R.drawable.default_avatar);
                    }
                }
            });
        }
    }

    private void showImageUrlDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nhập URL ảnh");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        input.setHint("https://example.com/image.jpg");

        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(50, 0, 50, 0);
        input.setLayoutParams(params);
        container.addView(input);

        builder.setView(container);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String url = input.getText().toString().trim();
            if (!url.isEmpty()) {
                photoUrl = url;
                Glide.with(this).load(photoUrl).into(avatarDetail);
            } else {
                Toast.makeText(this, "Vui lòng nhập URL hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void updateFirebaseUser() {
        String updatedName = userNameDetail.getText().toString().trim();
        String updatedEmail = userEmailDetail.getText().toString().trim();
        String updatedPhone = userPhoneDetail.getText().toString().trim();
        String updatedBio = userBioDetail.getText().toString().trim();
        String updatedAddress = userAddressDetail.getText().toString().trim();
        if (firebaseUser == null || userId == null) {
            Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
            return;
        }
        // Cập nhật Firestore
        DocumentReference userRef = db.collection("user").document(userId);
        userRef.update(
                "name", updatedName,
                "email", updatedEmail,
                "phone", updatedPhone,
                "bio", updatedBio,
                "address", updatedAddress,
                "photo", photoUrl
        ).addOnSuccessListener(aVoid -> {
            // Cập nhật Auth như cũ
            firebaseUser.updateEmail(updatedEmail)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Toast.makeText(this, "Lỗi cập nhật email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(updatedName)
                    .setPhotoUri(photoUrl != null ? Uri.parse(photoUrl) : null)
                    .build();
            firebaseUser.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            firebaseUser.reload().addOnCompleteListener(reloadTask -> {
                                if (reloadTask.isSuccessful()) {
                                    firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                    loadUserInfo();
                                    Toast.makeText(this, "Đã cập nhật thông tin người dùng", Toast.LENGTH_SHORT).show();
                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra("name", updatedName);
                                    resultIntent.putExtra("email", updatedEmail);
                                    resultIntent.putExtra("photo", photoUrl);
                                    setResult(RESULT_OK, resultIntent);
                                    finish();
                                } else {
                                    Toast.makeText(this, "Lỗi tải lại thông tin người dùng", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(this, "Lỗi cập nhật hồ sơ: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                photoUrl = selectedImage.toString();
                Glide.with(this).load(photoUrl).into(avatarDetail);
            }
        }
    }
}
