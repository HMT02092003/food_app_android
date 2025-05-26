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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ListFoodsActivity extends BaseActivity {

    ActivityListFoodsBinding binding;
    private RecyclerView.Adapter adapterListFood;
    private RecyclerView.Adapter categoryAdapter;
    private int categoryId;
    private String categoryName;
    private String searchText;
    private boolean isSearch;
    private List<String> categoryList;
    private RecyclerView categoryRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListFoodsBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        getIntentExtra();
        initCategoryList();
        initCategoryRecyclerView();
        initList();
        setVariable();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setVariable() {

    }

    private void initList() {
        DatabaseReference myRef = database.getReference("Foods");
        binding.progressBar.setVisibility(View.VISIBLE);
        ArrayList<Foods> list = new ArrayList<>();

        Query query;
        if(isSearch) {
            query = myRef.orderByChild("Titile").startAt(searchText).endAt(searchText+'\uf8ff');
        } else {
            if (categoryId == 0) {
                query = myRef;
            } else {
                query = myRef.orderByChild("CategoryId").equalTo(categoryId);
            }
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    list.clear();
                    for(DataSnapshot issue : snapshot.getChildren()) {
                        Foods food = issue.getValue(Foods.class);
                        if (food != null) {
                            if (food.getStar() == 0) {
                                food.setStar(0.0);
                            }
                            list.add(food);
                        }
                    }
                    
                    Collections.sort(list, (food1, food2) -> {
                        double rating1 = food1.getStar();
                        double rating2 = food2.getStar();
                        return Double.compare(rating2, rating1);
                    });

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
                    Toast.makeText(ListFoodsActivity.this, "Không có dữ liệu món ăn", Toast.LENGTH_SHORT).show();
                }
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
                binding.foodListView.setVisibility(View.GONE);
                binding.emptyView.setVisibility(View.VISIBLE);
                Toast.makeText(ListFoodsActivity.this, "Lỗi khi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
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
            if (category.equals("Tất cả")) {
                categoryId = 0;
            } else {
                switch (category) {
                    case "Món cơm": categoryId = 1; break;
                    case "Món nước": categoryId = 2; break;
                    case "Món kho,hầm": categoryId = 3; break;
                    case "Món chiên,xào": categoryId = 4; break;
                    case "Salad": categoryId = 5; break;
                    case "Món súp": categoryId = 6; break;
                    case "Đồ ăn đường phố": categoryId = 7; break;
                    case "Đồ ăn vặt": categoryId = 8; break;
                    case "Món tráng miệng": categoryId = 9; break;
                    case "Món vùng miền": categoryId = 10; break;
                    default: categoryId = 0;
                }
            }
            categoryName = category;
            binding.titleTxt.setText(categoryName);
            initList();
        });
        binding.categoryView.setAdapter(categoryAdapter);
    }
}