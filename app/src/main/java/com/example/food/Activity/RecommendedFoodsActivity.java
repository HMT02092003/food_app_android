package com.example.food.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager; // Import GridLayoutManager
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.Adapter.RecommendedFoodAdapter;
import com.example.food.Domain.Food;
import com.example.food.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RecommendedFoodsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ImageView backBtn;
    private RecommendedFoodAdapter adapter;
    private List<Food> recommendedFoods;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommended_foods);

        // Initialize views
        recyclerView = findViewById(R.id.recommendedFoodsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        backBtn = findViewById(R.id.backBtn);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Setup RecyclerView
        recommendedFoods = new ArrayList<>();
        adapter = new RecommendedFoodAdapter(recommendedFoods, this);

        // --- Đã sửa đổi ở đây: Sử dụng GridLayoutManager ---
        // Tham số thứ hai (2) là 'spanCount', tức là số cột trên mỗi hàng.
        // Bạn muốn 2 thẻ trên 1 hàng, nên đặt là 2.
        // Mặc định GridLayoutManager sẽ sắp xếp theo chiều dọc (xuống dòng)
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        // ---------------------------------------------------

        recyclerView.setAdapter(adapter);

        // Back button click listener
        backBtn.setOnClickListener(v -> finish());

        // Load recommended foods
        loadRecommendedFoods();
    }

    private void loadRecommendedFoods() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("PendingRecipes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    recommendedFoods.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String name = document.getString("name");
                        String details = document.getString("details");
                        Double price = document.getDouble("price");
                        List<String> imageUrls = (List<String>) document.get("imageUrls");
                        String id = document.getId();
                        Food food = new Food();
                        food.setName(name);
                        food.setDetails(details);
                        food.setPrice(price != null ? price : 0);
                        food.setImageUrls(imageUrls);
                        food.setId(id);
                        recommendedFoods.add(food);
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tải món ăn đề xuất: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
    }
}