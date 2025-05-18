// MainActivity.java (trong thư mục com.example.food.Activity)
package com.example.food.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.Adapter.CategoryAdapter;
import com.example.food.Adapter.UserFoodAdapter;
import com.example.food.FoodModel;
import com.example.food.R;
import com.example.food.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends BaseActivity {
    private ActivityMainBinding binding;
    private TextView nameTextView;
    private String userName;
    private String userEmail;
    private String userPhoto;
    private ArrayList<FoodModel> foodList = new ArrayList<>();
    private UserFoodAdapter userFoodAdapter;
    private List<String> categoryList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance(); // Khởi tạo Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        userName = intent.getStringExtra("name");
        userEmail = intent.getStringExtra("email");
        userPhoto = intent.getStringExtra("photo");

        nameTextView = findViewById(R.id.textView3);

        if (userName != null) {
            nameTextView.setText(userName);
        }

        if (nameTextView != null) {
            nameTextView.setOnClickListener(view -> {
                Intent personInfoIntent = new Intent(MainActivity.this, PersonInfoActivity.class);
                personInfoIntent.putExtra("name", userName);
                personInfoIntent.putExtra("email", userEmail);
                personInfoIntent.putExtra("photo", userPhoto);
                startActivity(personInfoIntent);
            });
        }

        initCategoryList();
        initCategoryRecyclerView();
        initFoodList("Tất cả");
        setVariable();

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initCategoryList() {
        categoryList = Arrays.asList(
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
        );
    }

    private void initCategoryRecyclerView() {
        binding.progressBarCategory.setVisibility(View.GONE);
        binding.categoryView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
        CategoryAdapter categoryAdapter = new CategoryAdapter(categoryList, this::filterFoodByCategory);
        binding.categoryView.setAdapter(categoryAdapter);
    }

    private void setVariable() {
        binding.logoutbtn.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        });

        binding.searchBtn.setOnClickListener(view -> {
            String text = binding.searchEdt.getText().toString();
            if (!text.isEmpty()) {
                Intent intent = new Intent(MainActivity.this, ListFoodsActivity.class);
                intent.putExtra("text", text);
                intent.putExtra("isSearch", true);
                startActivity(intent);
            }
        });
    }

    private void initFoodList(String category) {
        binding.progressBarFoodList.setVisibility(View.VISIBLE);
        foodList.clear();
        CollectionReference foodsRef = db.collection("Foods");
        Query query;

        if (!category.equals("Tất cả")) {
            query = foodsRef.whereEqualTo("category", category);
        } else {
            query = foodsRef;
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    foodList.clear();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            FoodModel food = document.toObject(FoodModel.class);
                            food.setId(document.getId());
                            foodList.add(food);
                        }
                    }
                    updateFoodRecyclerView();
                    binding.progressBarFoodList.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    binding.progressBarFoodList.setVisibility(View.GONE);
                    Log.e("MainActivity", "Lỗi khi lấy dữ liệu món ăn từ Firestore: ", e);
                    Toast.makeText(MainActivity.this, "Lỗi khi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateFoodRecyclerView() {
        if (foodList.size() > 0) {
            binding.foodListView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));
            userFoodAdapter = new UserFoodAdapter(MainActivity.this, foodList);
            binding.foodListView.setAdapter(userFoodAdapter);
        } else {
            // Xử lý trường hợp không có món ăn nào
        }
    }

    private void filterFoodByCategory(String categoryName) {
        initFoodList(categoryName);
    }
}