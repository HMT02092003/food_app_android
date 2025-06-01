package com.example.food.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ImageView;
import com.example.food.Adapter.FavoriteFoodAdapter;
import com.example.food.Domain.Food;
import com.example.food.Model.FoodModel;
import com.example.food.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class FavoriteFoodsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FavoriteFoodAdapter foodAdapter;
    private List<FoodModel> favoriteFoods;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_foods);

        recyclerView = findViewById(R.id.recyclerViewFavoriteFoods);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        favoriteFoods = new ArrayList<>();
        foodAdapter = new FavoriteFoodAdapter(favoriteFoods);
        recyclerView.setAdapter(foodAdapter);

        ImageView backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        loadFavoriteFoods();
    }

    private void loadFavoriteFoods() {
        if (currentUser == null) return;
        db.collection("Users")
                .document(currentUser.getUid())
                .collection("Favorites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    favoriteFoods.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String foodId = doc.getString("foodId");
                        db.collection("Foods").document(foodId).get().addOnSuccessListener(foodDoc -> {
                            if (foodDoc.exists()) {
                                FoodModel food = foodDoc.toObject(FoodModel.class);
                                if (food != null) {
                                    food.setId(foodDoc.getId());
                                    favoriteFoods.add(food);
                                    foodAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải món ăn yêu thích", Toast.LENGTH_SHORT).show());
    }
} 