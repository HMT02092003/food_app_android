package com.example.food.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.food.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class PersonInfoActivity extends AppCompatActivity {
    private ImageView avatar, backBtn;
    private TextView userName, userEmail, userBio, phoneNumber, userAddress, fullName; // Thêm fullName
    private Button btnLogout, btnEditInfo;
    private FirebaseFirestore db;
    private FirebaseUser firebaseUser;
    private String userId;

    // ActivityResultLauncher để nhận kết quả sau khi sửa
    private final ActivityResultLauncher<Intent> editInfoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) { // Chỉ kiểm tra RESULT_OK
                    // Sau khi chỉnh sửa, tải lại thông tin để cập nhật UI
                    loadUserInfo();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_info);

        // Khởi tạo view
        avatar = findViewById(R.id.avatarDetail);
        userName = findViewById(R.id.userName);
        userBio = findViewById(R.id.userBio);
        userEmail = findViewById(R.id.userEmail);
        phoneNumber = findViewById(R.id.phoneNumber);
        userAddress = findViewById(R.id.userAddress);
        fullName = findViewById(R.id.fullName); // Ánh xạ fullName
        backBtn = findViewById(R.id.backBtn);
        btnLogout = findViewById(R.id.btnLogout);
        btnEditInfo = findViewById(R.id.btnEditInfo);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        userId = firebaseUser != null ? firebaseUser.getUid() : null;

        backBtn.setOnClickListener(v -> finish());
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(PersonInfoActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnEditInfo.setOnClickListener(v -> {
            Intent intent = new Intent(PersonInfoActivity.this, PersonInfoDetailActivity.class);
            editInfoLauncher.launch(intent); // Sử dụng launcher để nhận kết quả
        });

        loadUserInfo();
    }

    private void loadUserInfo() {
        if (firebaseUser != null && userId != null) {
            // Đảm bảo collection là "Users" (chữ U hoa) như trong Firestore của bạn
            DocumentReference userRef = db.collection("Users").document(userId);
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                String name = null, email = null, phone = null, bio = null, photo = null, address = null;
                if (documentSnapshot.exists()) {
                    name = documentSnapshot.getString("name");
                    email = documentSnapshot.getString("email");
                    phone = documentSnapshot.getString("phone");
                    bio = documentSnapshot.getString("bio");
                    // Trường 'photo' (nếu có trong Firestore)
                    // photo = documentSnapshot.getString("photo");
                    address = documentSnapshot.getString("address");
                }

                // Fallback nếu dữ liệu từ Firestore là null
                // Ưu tiên dữ liệu từ Firestore, sau đó là từ FirebaseUser
                if (name == null) name = firebaseUser.getDisplayName();
                if (email == null) email = firebaseUser.getEmail();
                // Nếu bạn có trường 'photo' trong Firestore, bỏ comment dòng dưới
                // if (photo == null && firebaseUser.getPhotoUrl() != null) photo = firebaseUser.getPhotoUrl().toString();

                // Cập nhật UI
                userName.setText(name != null ? name : "Chưa cập nhật tên"); // Tên ở đầu trang
                fullName.setText(name != null ? name : "Chưa cập nhật tên"); // Tên trong CardView
                userEmail.setText(email != null ? email : "Chưa cập nhật email");
                userBio.setText(bio != null ? bio : "Chưa có mô tả");
                phoneNumber.setText(phone != null ? phone : "Chưa cập nhật số điện thoại");
                userAddress.setText(address != null ? address : "Chưa cập nhật địa chỉ");

                // Tải ảnh đại diện
                // Nếu bạn lưu URL ảnh trong Firestore, sử dụng Glide để tải
                // if (photo != null && !photo.isEmpty()) {
                //     Glide.with(this).load(photo).into(avatar);
                // } else {
                //     // Nếu không có ảnh từ Firestore, sử dụng ảnh mặc định
                //     avatar.setImageResource(R.drawable.default_avatar); // Đảm bảo bạn có default_avatar
                // }
                // Hiện tại đang dùng ảnh mặc định từ layout (intro_pic)
                avatar.setImageResource(R.drawable.intro_pic); // Sử dụng intro_pic như trong XML của bạn
            }).addOnFailureListener(e -> {
                // Xử lý lỗi khi không thể tải dữ liệu
                // Ví dụ: Toast.makeText(this, "Lỗi tải thông tin người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }
}