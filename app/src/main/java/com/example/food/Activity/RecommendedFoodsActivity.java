package com.example.food.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.Adapter.MyPendingRecipesAdapter;
import com.example.food.Model.PendingRecipe;
import com.example.food.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RecommendedFoodsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ImageView backBtn;
    private MyPendingRecipesAdapter adapter;
    private List<PendingRecipe> recommendedFoods;
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
        adapter = new MyPendingRecipesAdapter(recommendedFoods, new MyPendingRecipesAdapter.OnRecipeActionListener() {
            @Override
            public void onEdit(PendingRecipe recipe) {
                Intent intent = new Intent(RecommendedFoodsActivity.this, UserShareRecipeActivity.class);
                intent.putExtra("recipeId", recipe.getId());
                intent.putExtra("recipeName", recipe.getName());
                intent.putExtra("recipeCategory", recipe.getCategory());
                intent.putExtra("recipePrice", recipe.getPrice());
                intent.putExtra("recipeIngredients", recipe.getIngredients());
                intent.putExtra("recipeDetails", recipe.getDetails());
                intent.putExtra("recipeImageUrl", recipe.getImageUrls() != null && !recipe.getImageUrls().isEmpty() ? recipe.getImageUrls().get(0) : "");
                startActivity(intent);
            }
            @Override
            public void onDelete(PendingRecipe recipe) {
                new AlertDialog.Builder(RecommendedFoodsActivity.this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa món ăn này?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        db.collection("PendingRecipes")
                            .document(recipe.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(RecommendedFoodsActivity.this, "Đã xóa món ăn", Toast.LENGTH_SHORT).show();
                                loadRecommendedFoods();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(RecommendedFoodsActivity.this, "Lỗi khi xóa món ăn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            }
        }, R.layout.item_my_pending_recipe_grid);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        // Back button click listener
        backBtn.setOnClickListener(v -> finish());

        // Load recommended foods
        loadRecommendedFoods();
    }

    private void loadRecommendedFoods() {
        progressBar.setVisibility(View.VISIBLE);
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("PendingRecipes")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    recommendedFoods.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        PendingRecipe recipe = doc.toObject(PendingRecipe.class);
                        recipe.setId(doc.getId());
                        recommendedFoods.add(recipe);
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