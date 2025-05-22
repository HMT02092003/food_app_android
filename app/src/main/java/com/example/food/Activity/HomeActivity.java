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

        // Sự kiện click vào tên người dùng
        userNameTextView.setOnClickListener(v -> {
            Intent personInfoIntent = new Intent(HomeActivity.this, PersonInfoActivity.class);
            personInfoIntent.putExtra("name", userName);
            startActivity(personInfoIntent);
        });

        // Cài đặt sự kiện click cho nút đăng xuất
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        });

        // Tải dữ liệu
        loadHeroSection();
        loadFeaturedFoods();
        loadSuggestedFoods();
        loadFoodCategories(); // Hàm này giờ sẽ set danh sách mặc định

        // Cài đặt sự kiện click cho nút "Xem thêm" (tạm thời vô hiệu hóa)
        featuredFoodSeeMoreButton.setOnClickListener(v -> {
            // navigateToFoodListActivity("Món ăn nổi bật");
            Toast.makeText(HomeActivity.this, "Chức năng xem thêm món ăn nổi bật sẽ được cập nhật sau", Toast.LENGTH_SHORT).show();
        });
        suggestedFoodSeeMoreButton.setOnClickListener(v -> {
            // navigateToFoodListActivity("Gợi ý hôm nay");
            Toast.makeText(HomeActivity.this, "Chức năng xem thêm gợi ý hôm nay sẽ được cập nhật sau", Toast.LENGTH_SHORT).show();
        });
        categorySeeMoreButton.setOnClickListener(v -> {
            // navigateToCategoryListActivity();
            Toast.makeText(HomeActivity.this, "Chức năng xem thêm thể loại sẽ được cập nhật sau", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadHeroSection() {
        // Tạm thời set một ảnh và tiêu đề tĩnh. Bạn có thể thay đổi logic này
        Glide.with(this)
                .load(R.drawable.hero_placeholder) // Thay thế bằng URL ảnh thật
                .into(heroImageView);
        heroTitleTextView.setText("Khám phá hương vị mới mỗi ngày!");
        heroExploreButton.setOnClickListener(v -> {
            // Xử lý khi người dùng nhấn nút khám phá
            Toast.makeText(this, "Chức năng khám phá", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadFeaturedFoods() {
        featuredFoodRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        featuredFoodAdapter = new FoodHomeAdapter(this, featuredFoodList);
        featuredFoodRecyclerView.setAdapter(featuredFoodAdapter);

        // Lấy 5 món ăn nổi bật
        db.collection("Foods")
                .limit(5) // Giới hạn 5 món
                .get()
                .addOnSuccessListener(query -> {
                    featuredFoodList.clear();
                    for (QueryDocumentSnapshot document : query) {
                        FoodModel food = document.toObject(FoodModel.class);
                        food.setId(document.getId());
                        featuredFoodList.add(food);
                    }
                    featuredFoodAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("HomeActivity", "Lỗi khi tải món ăn nổi bật: ", e);
                    Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadSuggestedFoods() {
        suggestedFoodRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        suggestedFoodAdapter = new FoodHomeAdapter(this, suggestedFoodList);
        suggestedFoodRecyclerView.setAdapter(suggestedFoodAdapter);

        // Lấy 5 món ăn đề xuất trong ngày
        db.collection("Foods")
                .limit(5) // Giới hạn 5 món
                .get()
                .addOnSuccessListener(query -> {
                    suggestedFoodList.clear();
                    for (QueryDocumentSnapshot document : query) {
                        FoodModel food = document.toObject(FoodModel.class);
                        food.setId(document.getId());
                        suggestedFoodList.add(food);
                    }
                    suggestedFoodAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("HomeActivity", "Lỗi khi tải món ăn đề xuất: ", e);
                    Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                });
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
        if (category.equals("Tất cả")) {
            // Lấy 5 món ăn bất kỳ cho thể loại "Tất cả"
            db.collection("Foods")
                    .limit(5) // Hoặc bạn có thể lấy nhiều hơn nếu muốn
                    .get()
                    .addOnSuccessListener(query -> {
                        List<FoodModel> categoryFoods = new ArrayList<>();
                        for (QueryDocumentSnapshot document : query) {
                            FoodModel food = document.toObject(FoodModel.class);
                            food.setId(document.getId());
                            categoryFoods.add(food);
                        }
                        updateCategoryFoodList(categoryFoods);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("HomeActivity", "Lỗi khi tải món ăn theo thể loại: ", e);
                        Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Lấy món ăn cho thể loại cụ thể
            db.collection("Foods")
                    .whereEqualTo("category", category)
                    .limit(5) // Hoặc bạn có thể lấy nhiều hơn
                    .get()
                    .addOnSuccessListener(query -> {
                        List<FoodModel> categoryFoods = new ArrayList<>();
                        for (QueryDocumentSnapshot document : query) {
                            FoodModel food = document.toObject(FoodModel.class);
                            food.setId(document.getId());
                            categoryFoods.add(food);
                        }
                        updateCategoryFoodList(categoryFoods);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("HomeActivity", "Lỗi khi tải món ăn theo thể loại: ", e);
                        Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    });
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
}