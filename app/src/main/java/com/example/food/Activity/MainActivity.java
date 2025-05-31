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
import com.example.food.Model.FoodModel;
import com.example.food.R;
import com.example.food.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

        // Xử lý nút backBtn quay về HomeActivity
        findViewById(R.id.backBtn).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Lấy tên user từ Firestore collection Users
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            db.collection("Users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists() && documentSnapshot.getString("name") != null) {
                    binding.textView3.setText(documentSnapshot.getString("name"));
                }
            });
        }

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
        setVariable(); // Gọi phương thức setVariable sau khi khởi tạo các thành phần

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
        // Đã xóa nút logoutbtn nên không cần binding.logoutbtn.setOnClickListener nữa
        binding.searchBtn.setOnClickListener(view -> {
            performSearch();
        });
        binding.searchEdt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE ||
                (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER && event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                performSearch();
                return true;
            }
            return false;
        });
    }

    private void performSearch() {
        String text = binding.searchEdt.getText().toString().trim();
        if (!text.isEmpty()) {
            // Lọc danh sách món ăn theo tên (không phân biệt hoa thường)
            List<FoodModel> filteredList = new ArrayList<>();
            for (FoodModel food : foodList) {
                if (food.getName() != null && food.getName().toLowerCase().contains(text.toLowerCase())) {
                    filteredList.add(food);
                }
            }
            userFoodAdapter = new UserFoodAdapter(MainActivity.this, filteredList);
            binding.foodListView.setAdapter(userFoodAdapter);
        } else {
            // Nếu ô tìm kiếm rỗng, hiển thị lại toàn bộ danh sách
            userFoodAdapter = new UserFoodAdapter(MainActivity.this, foodList);
            binding.foodListView.setAdapter(userFoodAdapter);
        }
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
            userFoodAdapter.setOnItemClickListener(food -> {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("foodId", food.getId());
                intent.putExtra("foodName", food.getName());
                intent.putExtra("foodPrice", food.getPrice());
                intent.putExtra("foodDescription", food.getDetails());
                intent.putExtra("foodIngredients", food.getIngredients());
                intent.putExtra("foodRecipe", food.getRecipe());
                intent.putExtra("foodCategory", food.getCategory());
                intent.putExtra("foodRating", food.getRating());
                if (food.getImageUrls() != null && !food.getImageUrls().isEmpty()) {
                    intent.putExtra("foodImagePath", food.getImageUrls().get(0));
                }
                startActivity(intent);
            });
            binding.foodListView.setAdapter(userFoodAdapter);
        } else {
            // Xử lý trường hợp không có món ăn nào
            // Ví dụ: hiển thị một TextView thông báo "Không có món ăn nào"
            // binding.emptyFoodListTextView.setVisibility(View.VISIBLE);
        }
    }

    private void filterFoodByCategory(String categoryName) {
        initFoodList(categoryName);
    }
}
