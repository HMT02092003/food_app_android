package com.example.food.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.food.R;

public class PersonInfoActivity extends AppCompatActivity {
    private ImageView avatar;
    private TextView userName, userEmail, fullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_person_info);

        // Khởi tạo view
        avatar = findViewById(R.id.avatar);
        userName = findViewById(R.id.userName);
        fullName = findViewById(R.id.fullName);
        userEmail = findViewById(R.id.userEmail);

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String email = intent.getStringExtra("email");
        String photo = intent.getStringExtra("photo");

        // Hiển thị thông tin
        userName.setText(name);
        fullName.setText(name);
        userEmail.setText(email);
        if (photo != null && !photo.isEmpty()) {
            Glide.with(this).load(photo).into(avatar);
        }
    }
}