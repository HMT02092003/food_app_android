package com.example.food.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
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
        adapter = new RecommendedFoodAdapter(recommendedFoods, (position, food) -> {
            // Xóa trên Firestore
            String docId = food.getId();
            if (docId != null && !docId.isEmpty()) {
                db.collection("PendingRecipes").document(docId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        recommendedFoods.remove(position);
                        adapter.notifyItemRemoved(position);
                        Toast.makeText(this, "Đã xóa món ăn!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            } else {
                Toast.makeText(this, "Không tìm thấy id món ăn để xóa!", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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