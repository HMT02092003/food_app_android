package com.example.food.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log; // Import Log
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

    private static final String TAG = "PersonInfoDetail"; // Tag cho Logcat

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
        userAddressDetail = findViewById(R.id.userAddressDetail); // Lỗi ở đây, phải là R.id.userAddressDetail
        avatarDetail = findViewById(R.id.avatarDetail);
        editAvatarIcon = findViewById(R.id.editPencil);
        titleTextView = findViewById(R.id.title);
        backButton = findViewById(R.id.backButton);
        saveButton = findViewById(R.id.saveButton);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        userId = firebaseUser != null ? firebaseUser.getUid() : null;

        if (userId == null) {
            Toast.makeText(this, "Người dùng chưa đăng nhập hoặc không tìm thấy ID.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "userId is null. Cannot load user info.");
            finish(); // Đóng activity nếu không có userId
            return;
        }

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
        Log.d(TAG, "Loading user info for userId: " + userId);
        // Đảm bảo tên collection là "Users" hoặc "user" tùy theo Firebase của bạn
        // Trong PersonInfoActivity bạn đã sửa thành "Users", vậy nên ở đây cũng nên nhất quán.
        // Tôi sẽ sửa thành "Users" cho nhất quán. Nếu Firestore của bạn là "user", hãy đổi lại.
        DocumentReference userRef = db.collection("Users").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Log.d(TAG, "Document data: " + documentSnapshot.getData());
                String name = documentSnapshot.getString("name");
                String email = documentSnapshot.getString("email");
                String phone = documentSnapshot.getString("phone");
                String bio = documentSnapshot.getString("bio");
                String photo = documentSnapshot.getString("photo"); // Lấy trường photo từ Firestore
                String address = documentSnapshot.getString("address");

                titleTextView.setText("Sửa thông tin");
                userNameDetail.setText(name != null ? name : "");
                userEmailDetail.setText(email != null ? email : "");
                userPhoneDetail.setText(phone != null ? phone : "");
                userBioDetail.setText(bio != null ? bio : "");
                userAddressDetail.setText(address != null ? address : "");

                // Tải ảnh đại diện
                if (photo != null && !photo.isEmpty()) {
                    photoUrl = photo; // Cập nhật photoUrl để lưu lại
                    Glide.with(this).load(photoUrl).into(avatarDetail);
                    Log.d(TAG, "Loaded photo from Firestore: " + photoUrl);
                } else if (firebaseUser.getPhotoUrl() != null) {
                    // Nếu không có ảnh trong Firestore, thử lấy từ FirebaseUser (nếu có)
                    photoUrl = firebaseUser.getPhotoUrl().toString();
                    Glide.with(this).load(photoUrl).into(avatarDetail);
                    Log.d(TAG, "Loaded photo from FirebaseUser: " + photoUrl);
                }
                else {
                    avatarDetail.setImageResource(R.drawable.intro_pic); // Sử dụng intro_pic như trong XML
                    photoUrl = null; // Đảm bảo photoUrl là null nếu không có ảnh
                    Log.d(TAG, "Using default avatar.");
                }
            } else {
                Log.d(TAG, "No such document for userId: " + userId);
                // Nếu không có document, có thể điền thông tin từ FirebaseUser ban đầu
                userNameDetail.setText(firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "");
                userEmailDetail.setText(firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "");
                avatarDetail.setImageResource(R.drawable.intro_pic); // Đặt ảnh mặc định
                photoUrl = firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null; // Cập nhật photoUrl nếu có từ FirebaseUser
                Glide.with(this).load(photoUrl).into(avatarDetail); // Cố gắng tải nếu có
                Toast.makeText(this, "Không tìm thấy thông tin chi tiết, hiển thị thông tin cơ bản.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error loading user info: ", e);
            Toast.makeText(this, "Lỗi khi tải thông tin người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
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
        params.setMargins(50, 0, 50, 0); // Thêm margin để EditText không bị dính sát mép
        input.setLayoutParams(params);
        container.addView(input);

        builder.setView(container);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String url = input.getText().toString().trim();
            if (!url.isEmpty()) {
                photoUrl = url;
                Glide.with(this).load(photoUrl).into(avatarDetail);
                Log.d(TAG, "Photo URL set to: " + photoUrl);
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
            Log.e(TAG, "FirebaseUser or userId is null during update.");
            return;
        }

        // Đảm bảo tên collection là "Users" hoặc "user" tùy theo Firebase của bạn
        DocumentReference userRef = db.collection("Users").document(userId);

        userRef.update(
                "name", updatedName,
                "email", updatedEmail,
                "phone", updatedPhone,
                "bio", updatedBio,
                "address", updatedAddress,
                "photo", photoUrl // Lưu URL ảnh vào Firestore
        ).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Firestore update successful for user: " + userId);

            // Cập nhật email trong Firebase Auth
            firebaseUser.updateEmail(updatedEmail)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "Error updating email in Auth: " + task.getException().getMessage());
                            Toast.makeText(this, "Lỗi cập nhật email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG, "Email updated in Auth successfully.");
                        }
                    });

            // Cập nhật profile (display name và photo URL) trong Firebase Auth
            UserProfileChangeRequest.Builder profileUpdatesBuilder = new UserProfileChangeRequest.Builder()
                    .setDisplayName(updatedName);

            if (photoUrl != null && !photoUrl.isEmpty()) {
                profileUpdatesBuilder.setPhotoUri(Uri.parse(photoUrl));
            } else {
                // Nếu photoUrl là null hoặc rỗng, có thể cân nhắc đặt lại PhotoUri thành null
                // hoặc bỏ qua việc set PhotoUri để giữ nguyên ảnh cũ nếu có
                // profileUpdatesBuilder.setPhotoUri(null); // Tùy chọn: Đặt ảnh về null
            }
            UserProfileChangeRequest profileUpdates = profileUpdatesBuilder.build();


            firebaseUser.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Profile (name/photo) updated in Auth successfully.");
                            // Tải lại thông tin người dùng để đảm bảo dữ liệu FirebaseUser được cập nhật mới nhất
                            firebaseUser.reload().addOnCompleteListener(reloadTask -> {
                                if (reloadTask.isSuccessful()) {
                                    Log.d(TAG, "FirebaseUser reloaded successfully.");
                                    // Cần lấy lại firebaseUser sau reload để có dữ liệu mới nhất
                                    firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                    loadUserInfo(); // Tải lại UI với dữ liệu mới
                                    Toast.makeText(this, "Đã cập nhật thông tin người dùng", Toast.LENGTH_SHORT).show();

                                    // Trả về kết quả cho PersonInfoActivity
                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra("name", updatedName);
                                    resultIntent.putExtra("email", updatedEmail);
                                    resultIntent.putExtra("photo", photoUrl); // Trả về URL ảnh mới
                                    setResult(RESULT_OK, resultIntent);
                                    finish();
                                } else {
                                    Log.e(TAG, "Error reloading FirebaseUser: " + reloadTask.getException().getMessage());
                                    Toast.makeText(this, "Lỗi tải lại thông tin người dùng", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Log.e(TAG, "Error updating profile in Auth: " + task.getException().getMessage());
                            Toast.makeText(this, "Lỗi cập nhật hồ sơ: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error updating Firestore: " + e.getMessage());
            Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                photoUrl = selectedImage.toString(); // Lấy URI ảnh từ thư viện
                Glide.with(this).load(photoUrl).into(avatarDetail);
                Log.d(TAG, "Image selected from gallery: " + photoUrl);
                // Lưu ý: Nếu muốn tải ảnh lên Firebase Storage, bạn cần thực hiện logic đó ở đây
                // và sau đó lấy URL tải xuống để lưu vào Firestore và Auth.
            }
        }
    }
}