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
import java.util.Random; // Random không được sử dụng, có thể xóa nếu không cần

// SỬA LỖI: Thêm 'implements CategoryHomeAdapter.OnCategoryClickListener' vào đây
public class HomeActivity extends AppCompatActivity implements CategoryHomeAdapter.OnCategoryClickListener {

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

    private RecyclerView categoryFoodRecyclerView;
    private FoodVerticalAdapter categoryFoodAdapter;
    private List<FoodModel> categoryFoodList = new ArrayList<>();

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
            Toast.makeText(HomeActivity.this, "Bạn đã chọn: " + food.getName(), Toast.LENGTH_SHORT).show();

            // Đảm bảo truyền đúng các key và lấy đúng giá trị từ FoodModel
            Intent detailIntent = new Intent(HomeActivity.this, DetailActivity.class);
            detailIntent.putExtra("foodId", food.getId());
            detailIntent.putExtra("foodName", food.getName());
            detailIntent.putExtra("foodPrice", food.getPrice());
            detailIntent.putExtra("foodIngredients", food.getIngredients());

            // Truyền 'details' (FoodModel.getDetails()) vào key "foodDescription"
            detailIntent.putExtra("foodDescription", food.getDetails());

            // Lấy URL ảnh đầu tiên từ danh sách 'imageUrls' để truyền dưới dạng String
            String imageUrlToPass = "";
            if (food.getImageUrls() != null && !food.getImageUrls().isEmpty()) {
                imageUrlToPass = food.getImageUrls().get(0);
            }
            detailIntent.putExtra("foodImagePath", imageUrlToPass);

            detailIntent.putExtra("foodCategory", food.getCategory());
            detailIntent.putExtra("foodRating", food.getRating());

            startActivity(detailIntent);
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

        // Cài đặt sự kiện click cho nút "Xem thêm" Featured Food
        featuredFoodSeeMoreButton.setOnClickListener(v -> {
            navigateToFoodListActivity("Món ăn nổi bật");
        });

        // Cài đặt sự kiện click cho nút "Xem thêm" Suggested Food
        suggestedFoodSeeMoreButton.setOnClickListener(v -> {
            navigateToFoodListActivity("Gợi ý hôm nay");
        });

        // Cài đặt sự kiện click cho nút "Xem thêm" Category (hiện tại chỉ Toast)
        categorySeeMoreButton.setOnClickListener(v -> {
            navigateToCategoryListActivity();
        });
    }

    private void loadData() {
        loadHeroSection();
        loadFeaturedFoods();
        loadSuggestedFoods();
        loadFoodCategories();
    }

    private void loadHeroSection() {
        try {
            Glide.with(this)
                    .load(R.drawable.hero_placeholder)
                    .into(heroImageView);
        } catch (Exception e) {
            Log.e("HomeActivity", "Error loading hero image: " + e.getMessage());
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

        try {
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
                                Log.e("HomeActivity", "Lỗi chuyển đổi dữ liệu khi tải món ăn nổi bật: " + e.getMessage());
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
            Log.e("HomeActivity", "Lỗi không xác định khi tải món ăn nổi bật: " + e.getMessage());
        }
    }

    private void loadSuggestedFoods() {
        suggestedFoodRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        suggestedFoodAdapter = new FoodHomeAdapter(this, suggestedFoodList);
        suggestedFoodRecyclerView.setAdapter(suggestedFoodAdapter);

        try {
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
                                Log.e("HomeActivity", "Lỗi chuyển đổi dữ liệu khi tải món ăn đề xuất: " + e.getMessage());
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
            Log.e("HomeActivity", "Lỗi không xác định khi tải món ăn đề xuất: " + e.getMessage());
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

        // Khởi tạo CategoryHomeAdapter và truyền 'this' làm OnCategoryClickListener
        // HomeActivity giờ đã implement interface này.
        categoryAdapter = new CategoryHomeAdapter(this, categoryList, this); // Truyền 'this' ở đây
        categoryRecyclerView.setAdapter(categoryAdapter);

        // Load món ăn cho thể loại "Tất cả" ngay khi khởi tạo
        // và đảm bảo category "Tất cả" được đánh dấu là selected
        loadFoodsByCategory("Tất cả");
        categoryAdapter.setSelectedCategory("Tất cả"); // Đặt "Tất cả" là được chọn ban đầu
    }

    // THÊM PHƯƠNG THỨC NÀY ĐỂ IMPLEMENT CategoryHomeAdapter.OnCategoryClickListener
    @Override
    public void onCategoryClick(String category) {
        // Log.d("HomeActivity", "Category clicked: " + category); // Để debug
        loadFoodsByCategory(category); // Tải món ăn theo category được chọn
        // Không cần gọi categoryAdapter.setSelectedCategory(category) ở đây
        // vì logic đó đã được xử lý bên trong adapter khi click.
    }


    private void loadFoodsByCategory(String category) {
        // Log.d("HomeActivity", "Loading foods for category: " + category); // Để debug

        try {
            if (category.equals("Tất cả")) {
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
                                    Log.e("HomeActivity", "Lỗi chuyển đổi dữ liệu khi tải thể loại 'Tất cả': " + e.getMessage());
                                }
                            }
                            updateCategoryFoodList(categoryFoods);
                        })
                        .addOnFailureListener(e -> {
                            if (isDestroyed() || isFinishing()) return;
                            Log.e("HomeActivity", "Lỗi khi tải món ăn theo thể loại 'Tất cả': ", e);
                            Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                        });
            } else {
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
                                    Log.e("HomeActivity", "Lỗi chuyển đổi dữ liệu khi tải thể loại '" + category + "': " + e.getMessage());
                                }
                            }
                            updateCategoryFoodList(categoryFoods);
                        })
                        .addOnFailureListener(e -> {
                            if (isDestroyed() || isFinishing()) return;
                            Log.e("HomeActivity", "Lỗi khi tải món ăn theo thể loại '" + category + "': ", e);
                            Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                        });
            }
        } catch (Exception e) {
            Log.e("HomeActivity", "Lỗi không xác định khi tải món ăn theo thể loại: " + e.getMessage());
        }
    }

    private void updateCategoryFoodList(List<FoodModel> foods) {
        categoryFoodList.clear();
        categoryFoodList.addAll(foods);
        if (categoryFoodAdapter != null) {
            categoryFoodAdapter.notifyDataSetChanged();
        }
    }

    private void navigateToFoodListActivity(String title) {
        Intent intent = new Intent(HomeActivity.this, ListFoodsActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("isSearch", false);
        startActivity(intent);
    }

    private void navigateToCategoryListActivity() {
        Toast.makeText(this, "Chức năng xem thêm thể loại sẽ được cập nhật sau", Toast.LENGTH_SHORT).show();
    }
}