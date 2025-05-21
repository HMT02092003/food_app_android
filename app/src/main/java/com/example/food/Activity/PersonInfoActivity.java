package com.example.food.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.food.R;

public class PersonInfoActivity extends AppCompatActivity {
    private ImageView avatar, backBtn;
    private TextView userName, userEmail, fullName, editText;

    // ActivityResultLauncher để nhận kết quả sau khi sửa
    private final ActivityResultLauncher<Intent> editInfoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String name = result.getData().getStringExtra("name");
                    String email = result.getData().getStringExtra("email");
                    String photo = result.getData().getStringExtra("photo");

                    userName.setText(name);
                    fullName.setText(name);
                    userEmail.setText(email);
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
        fullName = findViewById(R.id.fullName);
        userEmail = findViewById(R.id.userEmail);
        editText = findViewById(R.id.editText);
        backBtn = findViewById(R.id.backBtn);

        // Xử lý nút quay lại
        backBtn.setOnClickListener(v -> finish());

        // Nhận dữ liệu từ Firebase hoặc Intent (lần đầu)
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String email = intent.getStringExtra("email");
        String photo = intent.getStringExtra("photo");

        userName.setText(name);
        fullName.setText(name);
        userEmail.setText(email);
        if (photo != null && !photo.isEmpty()) {
            Glide.with(this).load(photo).into(avatar);
        }

        editText.setOnClickListener(v -> {
            Intent detailIntent = new Intent(PersonInfoActivity.this, PersonInfoDetailActivity.class);
            detailIntent.putExtra("name", name);
            detailIntent.putExtra("email", email);
            detailIntent.putExtra("photo", photo);
            editInfoLauncher.launch(detailIntent);
        });
    }
}
