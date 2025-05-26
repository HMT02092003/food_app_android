package com.example.food.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.util.Log; // Thêm import này cho Log

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

// Imports mới cho Firestore
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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

    // Khởi tạo Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListFoodsBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        getIntentExtra();
        initCategoryList();
        initCategoryRecyclerView();
        initList(); // Gọi phương thức initList đã được cập nhật
        setVariable();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setVariable() {
        // Có thể thêm các biến hoặc sự kiện khác nếu cần
    }

    private void initList() {
        binding.progressBar.setVisibility(View.VISIBLE);
        ArrayList<Foods> list = new ArrayList<>();
        CollectionReference foodsRef = db.collection("Foods"); // Tham chiếu tới collection "Foods"

        Query query;

        if(isSearch) {
            // Đối với tìm kiếm, bạn có thể thực hiện tìm kiếm trên trường 'Title'
            // Firestore hỗ trợ tìm kiếm 'startsWith' bằng cách kết hợp startAt và endAt
            query = foodsRef.orderBy("Title").startAt(searchText).endAt(searchText + '\uf8ff');
        } else {
            if (categoryId == 0) {
                // Lấy tất cả món ăn, sau đó sẽ sắp xếp theo rating trong ứng dụng
                query = foodsRef;
            } else {
                // Lọc theo CategoryId
                query = foodsRef.whereEqualTo("CategoryId", categoryId);
            }
        }

        query.get() // Thực hiện truy vấn Firestore
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    list.clear();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Foods food = document.toObject(Foods.class);
                            // Đảm bảo ID được gán vào đối tượng Foods nếu bạn cần nó sau này
                            // food.setId(document.getId()); // Nếu Id trong Foods là String và khớp với document ID của Firestore

                            // Xử lý rating mặc định là 0.0 nếu chưa có
                            if (food.getStar() == 0) { // Kiểm tra xem giá trị hiện tại có phải là 0 không
                                food.setStar(0.0);
                            }
                            // Nếu bạn có khả năng Star có thể là null từ Firestore,
                            // hãy kiểm tra null trước khi gọi food.getStar()
                            // if (food.getStar() == null) {
                            //    food.setStar(0.0);
                            // }


                            list.add(food);
                        }
                    }

                    // Sắp xếp danh sách theo rating giảm dần (rating cao nhất ở đầu)
                    Collections.sort(list, (food1, food2) -> {
                        double rating1 = (food1.getStar() != 0) ? food1.getStar() : 0.0; // Đảm bảo rating là 0.0 nếu chưa có
                        double rating2 = (food2.getStar() != 0) ? food2.getStar() : 0.0; // Đảm bảo rating là 0.0 nếu chưa có
                        return Double.compare(rating2, rating1); // Giảm dần (rating cao hơn đứng trước)
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
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.foodListView.setVisibility(View.GONE);
                    binding.emptyView.setVisibility(View.VISIBLE);
                    Log.e("ListFoodsActivity", "Lỗi khi tải dữ liệu món ăn từ Firestore: ", e); // Log lỗi để debug
                    Toast.makeText(ListFoodsActivity.this, "Lỗi khi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            isSearch = false; // Đảm bảo tắt chế độ tìm kiếm khi lọc theo danh mục
            initList(); // Gọi lại initList để tải dữ liệu mới
        });
        binding.categoryView.setAdapter(categoryAdapter);
    }
}