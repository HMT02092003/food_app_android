package com.example.food.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.Adapter.FoodAdapter;
import com.example.food.FoodModel; // Corrected import statement
import com.example.food.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminFoodActivity extends AppCompatActivity implements FoodAdapter.OnFoodDeletedListener {

    private BottomNavigationView bottomNavigationView;
    private TabLayout tabLayout;
    private TextView tvTotal;
    private RecyclerView recyclerFood;
    private FoodAdapter foodAdapter;
    private List<FoodModel> allFoodList = new ArrayList<>();
    private Map<String, List<FoodModel>> categorizedFoodMap = new HashMap<>();
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ImageView backBtn;
    private String currentCategory = "Tất cả";

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

        // Khởi tạo Firebase Firestore và Auth
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Ánh xạ các view
        backBtn = findViewById(R.id.backBtn);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        tabLayout = findViewById(R.id.tabLayout);
        tvTotal = findViewById(R.id.tvTotal);
        recyclerFood = findViewById(R.id.recyclerFood);
        recyclerFood.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo adapter với danh sách rỗng và thiết lập listener
        foodAdapter = new FoodAdapter(new ArrayList<>());
        foodAdapter.setOnFoodDeletedListener(this); // Thiết lập listener cho việc xóa món ăn
        recyclerFood.setAdapter(foodAdapter);

        // Thiết lập sự kiện cho nút Back
        backBtn.setOnClickListener(v -> onBackPressed());

        // Thiết lập listener cho BottomNavigationView
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);

        // Thiết lập listener cho TabLayout để lọc món ăn theo thể loại
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentCategory = tab.getText().toString();
                filterFoodByCategory(currentCategory);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Không cần xử lý
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Nếu tab "Tất cả" được chọn lại, hiển thị tất cả món ăn
                if (tab.getPosition() == 0) {
                    currentCategory = "Tất cả";
                    filterFoodByCategory(currentCategory);
                }
            }
        });

        // Gọi hàm để lấy dữ liệu món ăn từ Firebase
        fetchAllFoods();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Kiểm tra người dùng đã đăng nhập chưa
        if (auth.getCurrentUser() == null) {
            // Nếu chưa đăng nhập, bạn CẦN chuyển hướng người dùng đến trang đăng nhập
            // This part should remain to handle expired sessions.
            redirectToLogin(); // Implement this method to navigate to your login activity
        } else {
            // Refresh danh sách món ăn mỗi khi quay lại Activity này (sau khi sửa hoặc xóa món ăn)
            fetchAllFoods();
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class); // Replace LoginActivity with your actual login activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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

                        // Đảm bảo id của món ăn được lưu lại từ Firestore
                        food.setId(document.getId());

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

                    // Hiển thị danh sách theo thể loại hiện tại
                    filterFoodByCategory(currentCategory);
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminFoodActivity", "Lỗi khi lấy dữ liệu món ăn: ", e);
                    Toast.makeText(this, "Lỗi khi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    // Kiểm tra nếu lỗi liên quan đến xác thực
                    if (e.getMessage() != null && (e.getMessage().contains("permission") || e.getMessage().contains("authentication"))) {
                        // Thông báo và chuyển hướng đến trang đăng nhập nếu có lỗi xác thực
                        Toast.makeText(this, "Phiên đăng nhập có vấn đề. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
                        redirectToLogin();
                    }
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

    @Override
    public void onFoodDeleted() {
        // Refresh lại danh sách món ăn sau khi xóa
        Toast.makeText(this, "Đã cập nhật danh sách món ăn", Toast.LENGTH_SHORT).show();
        fetchAllFoods();
    }
}