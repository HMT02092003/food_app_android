package com.example.food.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.food.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminFoodActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_food);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Tìm tham chiếu đến BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Đặt OnNavigationItemSelectedListener cho BottomNavigationView
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.homeBtn) {
                    // Xử lý khi nút Home được nhấn (nếu cần)
                    return true;
                } else if (id == R.id.addBtn) {
                    // Chuyển sang AdminAddFoodActivity khi nút New được nhấn
                    Intent intent = new Intent(AdminFoodActivity.this, AdminAddFoodActivity.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.profileBtn) {
                    // Xử lý khi nút Profile được nhấn (nếu cần)
                    return true;
                }
                return false;
            }
        });
    }
}