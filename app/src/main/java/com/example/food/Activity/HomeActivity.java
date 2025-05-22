package com.example.food.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import com.bumptech.glide.Glide;
import com.example.food.Adapter.CategoryHomeAdapter;
import com.example.food.Adapter.FoodHomeAdapter;
import com.example.food.Adapter.FoodVerticalAdapter;
import com.example.food.Model.FoodModel;
import com.example.food.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class HomeActivity extends AppCompatActivity {

    private TextView userNameTextView;
    private ImageView logoutButton;
    private ImageView heroImageView;
    private TextView heroTitleTextView;
    private Button heroExploreButton;

    private RecyclerView featuredFoodRecyclerView;
    private FoodHomeAdapter featuredFoodAdapter;
    private Button featuredFoodSeeMoreButton;
    private List<FoodModel> featuredFoodList = new ArrayList<>();

    private RecyclerView suggestedFoodRecyclerView;
    private FoodHomeAdapter suggestedFoodAdapter;
    private Button suggestedFoodSeeMoreButton;
    private List<FoodModel> suggestedFoodList = new ArrayList<>();

    private RecyclerView categoryRecyclerView;
    private CategoryHomeAdapter categoryAdapter;
    private Button categorySeeMoreButton;
    private List<String> categoryList = new ArrayList<>();

    // KHAI BÁO MỚI CHO DANH SÁCH MÓN ĂN THEO THỂ LOẠI DẠNG DỌC
    private RecyclerView categoryFoodRecyclerView; // Ánh xạ từ XML
    private FoodVerticalAdapter categoryFoodAdapter; // Adapter mới
    private List<FoodModel> categoryFoodList = new ArrayList<>(); // Danh sách dữ liệu cho adapter này

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scrollView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Nhận thông tin người dùng (nếu có)
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("name")) {
            userName = intent.getStringExtra("name");
        }

        initViews();
        setupListeners();
        loadData();
    }

    private void initViews() {
        // Ánh xạ các view
        userNameTextView = findViewById(R.id.userNameTextView);
        logoutButton = findViewById(R.id.logoutButton);
        heroImageView = findViewById(R.id.heroImageView);
        heroTitleTextView = findViewById(R.id.heroTitleTextView);
        heroExploreButton = findViewById(R.id.heroExploreButton);

        featuredFoodRecyclerView = findViewById(R.id.featuredFoodRecyclerView);
        featuredFoodSeeMoreButton = findViewById(R.id.featuredFoodSeeMoreButton);

        suggestedFoodRecyclerView = findViewById(R.id.suggestedFoodRecyclerView);
        suggestedFoodSeeMoreButton = findViewById(R.id.suggestedFoodSeeMoreButton);

        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        categorySeeMoreButton = findViewById(R.id.categorySeeMoreButton);

        // ÁNH XẠ VÀ CÀI ĐẶT CHO categoryFoodRecyclerView MỚI
        categoryFoodRecyclerView = findViewById(R.id.categoryFoodRecyclerView);
        categoryFoodRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        categoryFoodAdapter = new FoodVerticalAdapter(this, categoryFoodList);
        categoryFoodRecyclerView.setAdapter(categoryFoodAdapter);

        // Hiển thị tên người dùng nếu có
        if (userName != null) {
            userNameTextView.setText(userName);
        }
    }

    private void setupListeners() {
        // Tùy chọn: Đặt lắng nghe sự kiện click cho các item trong danh sách dọc
        categoryFoodAdapter.setOnItemClickListener(food -> {
            // Xử lý khi click vào một món ăn trong danh sách thể loại (ví dụ: mở trang chi tiết)
            Toast.makeText(HomeActivity.this, "Bạn đã chọn: " + food.getName(), Toast.LENGTH_SHORT).show();
            // Bạn có thể mở ListFoodsActivity hoặc DetailActivity ở đây
            // Intent detailIntent = new Intent(HomeActivity.this, FoodDetailActivity.class);
            // detailIntent.putExtra("foodId", food.getId());
            // startActivity(detailIntent);

        });


        // Hiển thị tên người dùng
        if (userName != null) {
            userNameTextView.setText(userName);
        }

        // Cài đặt sự kiện click cho nút đăng xuất
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        });

        // Cài đặt sự kiện click cho nút "Xem thêm"
        featuredFoodSeeMoreButton.setOnClickListener(v -> {
            navigateToFoodListActivity("Món ăn nổi bật");
        });

        suggestedFoodSeeMoreButton.setOnClickListener(v -> {
            navigateToFoodListActivity("Gợi ý hôm nay");
        });

        categorySeeMoreButton.setOnClickListener(v -> {
            navigateToCategoryListActivity();
        });
        // Cài đặt sự kiện click cho userNameTextView
        userNameTextView.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, PersonInfoActivity.class);
            startActivity(intent);
        });
    }

    private void loadData() {
        // Tải dữ liệu
        loadHeroSection();
        loadFeaturedFoods();
        loadSuggestedFoods();
        loadFoodCategories();
    }

    private void loadHeroSection() {
        try {
            // Sử dụng placeholder đơn giản
            Glide.with(this)
                    .load(R.drawable.hero_placeholder)
                    .into(heroImageView);
        } catch (Exception e) {
            Log.e("HomeActivity", "Error loading hero image: " + e.getMessage());
            // Fallback to setting background color if image loading fails
            heroImageView.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
        }

        heroTitleTextView.setText("Khám phá hương vị mới mỗi ngày!");
        heroExploreButton.setOnClickListener(v -> {
            navigateToFoodListActivity("Khám phá");
        });
    }

    private void loadFeaturedFoods() {
        featuredFoodRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        featuredFoodAdapter = new FoodHomeAdapter(this, featuredFoodList);
        featuredFoodRecyclerView.setAdapter(featuredFoodAdapter);

        // Show a loading indicator or placeholder while data is being fetched
        try {
            // Lấy 5 món ăn nổi bật - với timeout để tránh ANR
            db.collection("Foods")
                    .limit(5)
                    .get()
                    .addOnSuccessListener(query -> {
                        featuredFoodList.clear();
                        for (QueryDocumentSnapshot document : query) {
                            try {
                                FoodModel food = document.toObject(FoodModel.class);
                                food.setId(document.getId());
                                featuredFoodList.add(food);
                            } catch (Exception e) {
                                Log.e("HomeActivity", "Lỗi chuyển đổi dữ liệu: " + e.getMessage());
                            }
                        }
                        if (isDestroyed() || isFinishing()) return;
                        featuredFoodAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        if (isDestroyed() || isFinishing()) return;
                        Log.e("HomeActivity", "Lỗi khi tải món ăn nổi bật: ", e);
                        Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Log.e("HomeActivity", "Lỗi không xác định: " + e.getMessage());
        }
    }

    private void loadSuggestedFoods() {
        suggestedFoodRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        suggestedFoodAdapter = new FoodHomeAdapter(this, suggestedFoodList);
        suggestedFoodRecyclerView.setAdapter(suggestedFoodAdapter);

        try {
            // Lấy 5 món ăn đề xuất trong ngày
            db.collection("Foods")
                    .limit(5)
                    .get()
                    .addOnSuccessListener(query -> {
                        if (isDestroyed() || isFinishing()) return;
                        suggestedFoodList.clear();
                        for (QueryDocumentSnapshot document : query) {
                            try {
                                FoodModel food = document.toObject(FoodModel.class);
                                food.setId(document.getId());
                                suggestedFoodList.add(food);
                            } catch (Exception e) {
                                Log.e("HomeActivity", "Lỗi chuyển đổi dữ liệu: " + e.getMessage());
                            }
                        }
                        suggestedFoodAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        if (isDestroyed() || isFinishing()) return;
                        Log.e("HomeActivity", "Lỗi khi tải món ăn đề xuất: ", e);
                        Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Log.e("HomeActivity", "Lỗi không xác định: " + e.getMessage());
        }
    }

    private void loadFoodCategories() {
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoryList.addAll(Arrays.asList(
                "Tất cả",
                "Món cơm",
                "Món nước",
                "Món kho,hầm",
                "Món chiên,xào",
                "Salad",
                "Món súp",
                "Đồ ăn đường phố",
                "Đồ ăn vặt",
                "Món tráng miệng",
                "Món vùng miền"
        ));
        // Khi click vào một thể loại, sẽ gọi loadFoodsByCategory để cập nhật danh sách dọc
        categoryAdapter = new CategoryHomeAdapter(this, categoryList, this::loadFoodsByCategory);
        categoryRecyclerView.setAdapter(categoryAdapter);

        // Load món ăn cho thể loại "Tất cả" ngay khi khởi tạo
        loadFoodsByCategory("Tất cả");
    }

    private void loadFoodsByCategory(String category) {
        try {
            if (category.equals("Tất cả")) {
                // Lấy 5 món ăn bất kỳ cho thể loại "Tất cả"
                db.collection("Foods")
                        .limit(5)
                        .get()
                        .addOnSuccessListener(query -> {
                            if (isDestroyed() || isFinishing()) return;
                            List<FoodModel> categoryFoods = new ArrayList<>();
                            for (QueryDocumentSnapshot document : query) {
                                try {
                                    FoodModel food = document.toObject(FoodModel.class);
                                    food.setId(document.getId());
                                    categoryFoods.add(food);
                                } catch (Exception e) {
                                    Log.e("HomeActivity", "Lỗi chuyển đổi dữ liệu: " + e.getMessage());
                                }
                            }
                            updateCategoryFoodList(categoryFoods);
                        })
                        .addOnFailureListener(e -> {
                            if (isDestroyed() || isFinishing()) return;
                            Log.e("HomeActivity", "Lỗi khi tải món ăn theo thể loại: ", e);
                            Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Lấy món ăn cho thể loại cụ thể
                db.collection("Foods")
                        .whereEqualTo("category", category)
                        .limit(5)
                        .get()
                        .addOnSuccessListener(query -> {
                            if (isDestroyed() || isFinishing()) return;
                            List<FoodModel> categoryFoods = new ArrayList<>();
                            for (QueryDocumentSnapshot document : query) {
                                try {
                                    FoodModel food = document.toObject(FoodModel.class);
                                    food.setId(document.getId());
                                    categoryFoods.add(food);
                                } catch (Exception e) {
                                    Log.e("HomeActivity", "Lỗi chuyển đổi dữ liệu: " + e.getMessage());
                                }
                            }
                            updateCategoryFoodList(categoryFoods);
                        })
                        .addOnFailureListener(e -> {
                            if (isDestroyed() || isFinishing()) return;
                            Log.e("HomeActivity", "Lỗi khi tải món ăn theo thể loại: ", e);
                            Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                        });
            }
        } catch (Exception e) {
            Log.e("HomeActivity", "Lỗi không xác định: " + e.getMessage());
        }
    }

    private void updateCategoryFoodList(List<FoodModel> foods) {
        categoryFoodList.clear();
        categoryFoodList.addAll(foods);
        // CẬP NHẬT UI CỦA DANH SÁCH DỌC
        if (categoryFoodAdapter != null) {
            categoryFoodAdapter.notifyDataSetChanged();
        }
    }

    private void navigateToFoodListActivity(String title) {
        Intent intent = new Intent(HomeActivity.this, ListFoodsActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("isSearch", false); // Không phải là tìm kiếm
        startActivity(intent);
    }

    private void navigateToCategoryListActivity() {
        // Implement the logic to navigate to the category list activity
        Toast.makeText(this, "Chức năng xem thêm thể loại sẽ được cập nhật sau", Toast.LENGTH_SHORT).show();
    }
}