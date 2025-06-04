package com.example.food.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.Adapter.CategoryAdapter;
import com.example.food.Adapter.FoodListAdapter;
import com.example.food.Domain.Foods;
import com.example.food.R;
import com.example.food.databinding.ActivityListFoodsBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.util.Log;

public class ListFoodsActivity extends BaseActivity {

    ActivityListFoodsBinding binding;
    private RecyclerView.Adapter adapterListFood;
    private RecyclerView.Adapter categoryAdapter;
    private int categoryId;
    private String categoryName;
    private String searchText;
    private boolean isSearch;
    private List<String> categoryList;
    private FirebaseFirestore db;
    private String currentCategory = "Tất cả";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListFoodsBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        getIntentExtra();
        initCategoryList();
        initCategoryRecyclerView();
        initList();
        setVariable();

        // Ẩn progressBarCategory ngay khi vào màn hình
        binding.progressBarCategory.setVisibility(View.GONE);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setVariable() {
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
        String keyword = binding.searchEdt.getText().toString().trim();
        if (!keyword.isEmpty()) {
            searchText = keyword;
            isSearch = true;
        } else {
            searchText = null;
            isSearch = false;
        }
        initList();
    }

    private void initList() {
        binding.progressBar.setVisibility(View.VISIBLE);
        ArrayList<Foods> list = new ArrayList<>();

        Query query;
        if (isSearch && searchText != null && !searchText.isEmpty()) {
            // Nếu đang tìm kiếm, lấy tất cả rồi lọc ở client (Firestore không hỗ trợ contains/LIKE cho text)
            query = db.collection("Foods");
        } else if (currentCategory.equals("Tất cả")) {
            query = db.collection("Foods");
        } else {
            query = db.collection("Foods").whereEqualTo("category", currentCategory);
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                list.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Foods food = new Foods();
                    food.setId(document.getString("id") != null ? document.getString("id") : document.getId());
                    String title = document.getString("name");
                    food.setTitle(title != null ? title : "Không tên");
                    List<String> imageUrls = (List<String>) document.get("imageUrls");
                    String imagePath = (imageUrls != null && !imageUrls.isEmpty()) ? imageUrls.get(0) : "";
                    food.setImagePath(imagePath);
                    Double price = document.getDouble("price");
                    food.setPrice(price != null ? price : 0);
                    Double star = document.getDouble("rating");
                    food.setStar(star != null ? star : 0.0);
                    Long reviewCount = document.getLong("reviewCount");
                    food.setReviewCount(reviewCount != null ? reviewCount : 0);
                    // Nếu đang tìm kiếm thì chỉ add nếu tên chứa từ khóa
                    if (isSearch && searchText != null && !searchText.isEmpty()) {
                        if (food.getTitle().toLowerCase().contains(searchText.toLowerCase())) {
                            list.add(food);
                        }
                    } else {
                        list.add(food);
                    }
                }
                // Sắp xếp theo số lượng comments giảm dần
                Collections.sort(list, (food1, food2) -> Long.compare(food2.getReviewCount(), food1.getReviewCount()));
                
                if(list.size() > 0) {
                    binding.foodListView.setLayoutManager(new GridLayoutManager(ListFoodsActivity.this, 2));
                    adapterListFood = new FoodListAdapter(list);
                    binding.foodListView.setAdapter(adapterListFood);
                    binding.foodListView.setVisibility(View.VISIBLE);
                    binding.emptyView.setVisibility(View.GONE);
                } else {
                    binding.foodListView.setVisibility(View.GONE);
                    binding.emptyView.setVisibility(View.VISIBLE);
                    Toast.makeText(ListFoodsActivity.this, "Không tìm thấy món ăn nào", Toast.LENGTH_SHORT).show();
                }
            } else {
                binding.foodListView.setVisibility(View.GONE);
                binding.emptyView.setVisibility(View.VISIBLE);
                Toast.makeText(ListFoodsActivity.this, "Lỗi khi tải dữ liệu: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
            binding.progressBar.setVisibility(View.GONE);
        });
    }

    private void getIntentExtra() {
        categoryId=getIntent().getIntExtra("CategoryId",0);
        categoryName=getIntent().getStringExtra("CategoryName");
        searchText=getIntent().getStringExtra("text");
        isSearch=getIntent().getBooleanExtra("isSearch",false);

        binding.titleTxt.setText(categoryName);
        binding.backBtn.setOnClickListener(view -> finish());
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
        binding.categoryView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryAdapter(categoryList, category -> {
            currentCategory = category;
            binding.titleTxt.setText(currentCategory);
            initList();
        });
        binding.categoryView.setAdapter(categoryAdapter);
    }
}