package com.example.food.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private ProgressBar progressBarCategory;
    private ProgressBar progressBarFoodList;
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

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("name")) {
            userName = intent.getStringExtra("name");
        }

        initViews();
        setupListeners();
        loadData();
    }

    private void initViews() {
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
        progressBarCategory = findViewById(R.id.progressBarCategory);
        progressBarFoodList = findViewById(R.id.progressBarFoodList);
        categoryFoodRecyclerView = findViewById(R.id.categoryFoodRecyclerView);
        categoryFoodRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        categoryFoodAdapter = new FoodVerticalAdapter(this, categoryFoodList);
        categoryFoodRecyclerView.setAdapter(categoryFoodAdapter);

        if (userName != null) {
            userNameTextView.setText(userName);
        }
    }

    private void setupListeners() {
        // Áp dụng OnItemClickListener cho FoodHomeAdapter (Featured và Suggested)
        // Đây là phương thức giả định sẽ được thêm vào FoodHomeAdapter
        FoodHomeAdapter.OnItemClickListener foodHomeClickListener = food -> {
            navigateToDetailActivity(food);
        };

        // Gán listener cho Featured Food Adapter
        if (featuredFoodAdapter != null) {
            featuredFoodAdapter.setOnItemClickListener(foodHomeClickListener);
        }

        // Gán listener cho Suggested Food Adapter
        if (suggestedFoodAdapter != null) {
            suggestedFoodAdapter.setOnItemClickListener(foodHomeClickListener);
        }

        // Đã có listener cho Category Food Adapter
        categoryFoodAdapter.setOnItemClickListener(food -> {
            navigateToDetailActivity(food);
        });

        if (userName != null) {
            userNameTextView.setText(userName);
        }

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        });

        featuredFoodSeeMoreButton.setOnClickListener(v -> {
            navigateToFoodListActivity("Món ăn nổi bật");
        });

        suggestedFoodSeeMoreButton.setOnClickListener(v -> {
            navigateToFoodListActivity("Gợi ý hôm nay");
        });
    }

    private void loadData() {
        loadHeroSection();
        loadFeaturedFoods();
        loadSuggestedFoods();
        initCategoryList();
        initCategoryRecyclerView();
        loadFoodsByCategory("Tất cả");
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
        // Gán listener sau khi adapter được khởi tạo
        featuredFoodAdapter.setOnItemClickListener(food -> navigateToDetailActivity(food));

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
        // Gán listener sau khi adapter được khởi tạo
        suggestedFoodAdapter.setOnItemClickListener(food -> navigateToDetailActivity(food));

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

    private void initCategoryList() {
        categoryList.clear();
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
    }

    private void initCategoryRecyclerView() {
        progressBarCategory.setVisibility(View.GONE);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryHomeAdapter(this, categoryList, this);
        categoryRecyclerView.setAdapter(categoryAdapter);
    }

    @Override
    public void onCategoryClick(String category) {
        loadFoodsByCategory(category);
    }

    private void loadFoodsByCategory(String category) {
        progressBarFoodList.setVisibility(View.VISIBLE);
        categoryFoodList.clear();
        CollectionReference foodsRef = db.collection("Foods");
        Query query;

        if (!category.equals("Tất cả")) {
            query = foodsRef.whereEqualTo("category", category);
        } else {
            query = foodsRef;
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    categoryFoodList.clear();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            FoodModel food = document.toObject(FoodModel.class);
                            food.setId(document.getId());
                            categoryFoodList.add(food);
                        }
                    }
                    if (categoryFoodAdapter != null) {
                        categoryFoodAdapter.notifyDataSetChanged();
                    }
                    progressBarFoodList.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBarFoodList.setVisibility(View.GONE);
                    Log.e("HomeActivity", "Lỗi khi lấy dữ liệu món ăn từ Firestore: ", e);
                    Toast.makeText(HomeActivity.this, "Lỗi khi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Phương thức chung để điều hướng đến DetailActivity
    private void navigateToDetailActivity(FoodModel food) {
        Intent detailIntent = new Intent(HomeActivity.this, DetailActivity.class);
        detailIntent.putExtra("foodId", food.getId());
        detailIntent.putExtra("foodName", food.getName());
        detailIntent.putExtra("foodPrice", food.getPrice());
        detailIntent.putExtra("foodIngredients", food.getIngredients());
        detailIntent.putExtra("foodDescription", food.getDetails()); // Sử dụng getDetails()
        String imageUrlToPass = "";
        if (food.getImageUrls() != null && !food.getImageUrls().isEmpty()) {
            imageUrlToPass = food.getImageUrls().get(0); // Lấy URL ảnh đầu tiên
        }
        detailIntent.putExtra("foodImagePath", imageUrlToPass);
        detailIntent.putExtra("foodCategory", food.getCategory());
        detailIntent.putExtra("foodRating", food.getRating());
        startActivity(detailIntent);
    }

    private void navigateToFoodListActivity(String title) {
        Intent intent = new Intent(HomeActivity.this, ListFoodsActivity.class);
        intent.putExtra("title", title); // Dùng cho tiêu đề của ListFoodsActivity
        intent.putExtra("isSearch", false);
        // Nếu bạn muốn lọc theo loại (Featured, Suggested), bạn có thể thêm extra ở đây
        // Ví dụ: intent.putExtra("filterType", title);
        startActivity(intent);
    }
}