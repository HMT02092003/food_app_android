package com.example.food.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.Adapter.FoodAdapter;
import com.example.food.FoodModel;
import com.example.food.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminFoodActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAdd;
    private TabLayout tabLayout;
    private TextView tvTotal;
    private RecyclerView recyclerFood;
    private FoodAdapter foodAdapter;
    private List<FoodModel> allFoodList = new ArrayList<>();
    private Map<String, List<FoodModel>> categorizedFoodMap = new HashMap<>();
    private FirebaseFirestore db;

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

        // Khởi tạo Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Ánh xạ các view
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        fabAdd = findViewById(R.id.fabAdd);
        tabLayout = findViewById(R.id.tabLayout);
        tvTotal = findViewById(R.id.tvTotal);
        recyclerFood = findViewById(R.id.recyclerFood);
        recyclerFood.setLayoutManager(new LinearLayoutManager(this));
        foodAdapter = new FoodAdapter(new ArrayList<>()); // Khởi tạo adapter với danh sách rỗng
        recyclerFood.setAdapter(foodAdapter);

        // Thiết lập listener cho BottomNavigationView
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);

        // Thiết lập listener cho FloatingActionButton để thêm món ăn mới
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(AdminFoodActivity.this, AdminAddFoodActivity.class);
            startActivity(intent);
        });

        // Thiết lập listener cho TabLayout để lọc món ăn theo thể loại
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String category = tab.getText().toString();
                filterFoodByCategory(category);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Không cần xử lý
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Nếu tab "Tất cả" được chọn lại, hiển thị tất cả món ăn
                if (tab.getPosition() == 0) {
                    filterFoodByCategory("Tất cả");
                }
            }
        });

        // Gọi hàm để lấy dữ liệu món ăn từ Firebase
        fetchAllFoods();
    }

    private boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.homeBtn) {
            // Xử lý khi nút Home được nhấn (nếu cần)
            return true;
        } else if (id == R.id.createBtn) {
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

    private void fetchAllFoods() {
        CollectionReference foodsRef = db.collection("Foods");
        foodsRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allFoodList.clear();
                    categorizedFoodMap.clear();
                    categorizedFoodMap.put("Tất cả", new ArrayList<>()); // Khởi tạo danh sách cho "Tất cả"

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        FoodModel food = document.toObject(FoodModel.class);
                        allFoodList.add(food);

                        // Thêm vào danh sách của thể loại tương ứng
                        String category = food.getCategory();
                        if (!categorizedFoodMap.containsKey(category)) {
                            categorizedFoodMap.put(category, new ArrayList<>());
                        }
                        categorizedFoodMap.get(category).add(food);
                        categorizedFoodMap.get("Tất cả").add(food); // Thêm vào danh sách "Tất cả"
                    }

                    // Hiển thị tổng số món ăn
                    tvTotal.setText("Total " + allFoodList.size() + " items");

                    // Hiển thị danh sách "Tất cả" ban đầu
                    filterFoodByCategory("Tất cả");
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminFoodActivity", "Lỗi khi lấy dữ liệu món ăn: ", e);
                    // Xử lý lỗi (ví dụ: hiển thị thông báo cho người dùng)
                });
    }

    private void filterFoodByCategory(String category) {
        List<FoodModel> filteredList;
        if (category.equals("Tất cả")) {
            filteredList = allFoodList;
        } else {
            filteredList = categorizedFoodMap.get(category);
            if (filteredList == null) {
                filteredList = new ArrayList<>(); // Tránh NullPointerException nếu thể loại không tồn tại
            }
        }
        foodAdapter.updateData(filteredList);
    }
}