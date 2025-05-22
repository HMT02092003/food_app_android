package com.example.food.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.food.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class PersonInfoActivity extends AppCompatActivity {
    private ImageView avatar, backBtn;
    private TextView userName, userEmail, userBio, phoneNumber, userAddress;
    private Button btnLogout, btnEditInfo;
    private FirebaseFirestore db;
    private FirebaseUser firebaseUser;
    private String userId;

    // ActivityResultLauncher để nhận kết quả sau khi sửa
    private final ActivityResultLauncher<Intent> editInfoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String name = result.getData().getStringExtra("name");
                    String email = result.getData().getStringExtra("email");
                    String photo = result.getData().getStringExtra("photo");

                    userName.setText(name);
                    if (photo != null && !photo.isEmpty()) {
                        Glide.with(this).load(photo).into(avatar);
                    }
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
            startActivity(intent);
        });

        loadUserInfo();
    }

    private void loadUserInfo() {
        if (firebaseUser != null && userId != null) {
            DocumentReference userRef = db.collection("user").document(userId);
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                String name = null, email = null, phone = null, bio = null, photo = null, address = null;
                if (documentSnapshot.exists()) {
                    name = documentSnapshot.getString("name");
                    email = documentSnapshot.getString("email");
                    phone = documentSnapshot.getString("phone");
                    bio = documentSnapshot.getString("bio");
                    photo = documentSnapshot.getString("photo");
                    address = documentSnapshot.getString("address");
                }
                if (name == null) name = firebaseUser.getDisplayName();
                if (email == null) email = firebaseUser.getEmail();
                if (photo == null && firebaseUser.getPhotoUrl() != null) photo = firebaseUser.getPhotoUrl().toString();
                userName.setText(name != null ? name : "");
                userEmail.setText(email != null ? email : "");
                userBio.setText(bio != null ? bio : "");
                phoneNumber.setText(phone != null ? phone : "");
                userAddress.setText(address != null ? address : "");
                if (photo != null && !photo.isEmpty()) {
                    Glide.with(this).load(photo).into(avatar);
                } else {
                    avatar.setImageResource(R.drawable.default_avatar);
                }
            });
        }
    }
}
