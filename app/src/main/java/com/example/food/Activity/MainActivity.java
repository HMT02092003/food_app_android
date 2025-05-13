package com.example.food.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.food.R;

public class MainActivity extends AppCompatActivity {
    private TextView nameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Khởi tạo TextView
        nameTextView = findViewById(R.id.textView3);

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String email = intent.getStringExtra("email");
        String photo = intent.getStringExtra("photo");

        // Hiển thị tên
        if (name != null) {
            nameTextView.setText(name);
        }

        // Xử lý sự kiện nhấp vào tên
        nameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent personInfoIntent = new Intent(MainActivity.this, PersonInfoActivity.class);
                personInfoIntent.putExtra("name", name);
                personInfoIntent.putExtra("email", email);
                personInfoIntent.putExtra("photo", photo);
                startActivity(personInfoIntent);
            }
        });
    }
}