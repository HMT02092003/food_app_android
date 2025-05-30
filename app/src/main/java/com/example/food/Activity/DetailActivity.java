package com.example.food.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
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
import com.example.food.Adapter.CommentAdapter;
import com.example.food.Model.Comment;
import com.example.food.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Khai báo các View từ layout activity_detail.xml
    private ImageView imageView8; // Ảnh món ăn chính
    private ImageView favBtn;     // Nút yêu thích
    private ImageView backBtn;    // Nút quay lại
    private TextView titleTxt;    // Tên món ăn
    private TextView priceTxt;    // Giá món ăn
    private RatingBar ratingBar;  // Rating Bar
    private TextView rateTxt;     // Text hiển thị số rating (ví dụ: "5 Rating")
    private TextView categoryTxt; // Giá trị thể loại món ăn (ví dụ: "Món Cơm")
    private TextView descriptionTxt; // Mô tả món ăn
    private TextView ingridentTxt; // Nguyên liệu món ăn
    private TextView recipeContentTxt; // Thêm TextView cho công thức
    private RatingBar userRatingBar;
    private EditText commentInput;
    private Button submitRatingBtn;
    private RecyclerView commentsRecyclerView;

    private String foodId;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private CommentAdapter commentAdapter;
    private List<Comment> comments;

    private GoogleMap mMap;
    private static final LatLng HANOI = new LatLng(21.0285, 105.8542);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        comments = new ArrayList<>();

        // Khởi tạo các View
        initViews();
        // Lấy và hiển thị dữ liệu món ăn
        getAndSetFoodData();
        // Thiết lập các lắng nghe sự kiện
        setupListeners();
        setupCommentsRecyclerView();
        loadComments();

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Hanoi
        mMap.addMarker(new MarkerOptions()
                .position(HANOI)
                .title("Hà Nội, Việt Nam"));

        // Move camera to Hanoi with zoom level 15
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HANOI, 15));

        // Enable zoom controls
        mMap.getUiSettings().setZoomControlsEnabled(true);
        
        // Enable my location button if you have location permission
        try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Phương thức này dùng để ánh xạ các View từ layout XML vào các biến Java.
     */
    private void initViews() {
        imageView8 = findViewById(R.id.imageView8);
        favBtn = findViewById(R.id.favBtn);
        backBtn = findViewById(R.id.backBtn);
        titleTxt = findViewById(R.id.titleTxt);
        priceTxt = findViewById(R.id.priceTxt);
        ratingBar = findViewById(R.id.ratingBar);
        rateTxt = findViewById(R.id.rateTxt);

        categoryTxt = findViewById(R.id.categoryTxt);
        descriptionTxt = findViewById(R.id.descriptionTxt);
        ingridentTxt = findViewById(R.id.ingridentTxt);
        recipeContentTxt = findViewById(R.id.recipeContentTxt); // Ánh xạ TextView cho công thức
        userRatingBar = findViewById(R.id.userRatingBar);
        commentInput = findViewById(R.id.commentInput);
        submitRatingBtn = findViewById(R.id.submitRatingBtn);
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
    }

    private void setupCommentsRecyclerView() {
        commentAdapter = new CommentAdapter(this, comments);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentAdapter);
    }

    private void loadComments() {
        if (foodId == null) return;

        db.collection("Foods")
                .document(foodId)
                .collection("comments")
                .orderBy("timestamp")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading comments: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    comments.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Comment comment = doc.toObject(Comment.class);
                            comments.add(comment);
                        }
                        commentAdapter.updateComments(comments);
                        updateAverageRating();
                    }
                });
    }

    private void updateAverageRating() {
        if (comments.isEmpty()) {
            ratingBar.setRating(0);
            rateTxt.setText("0 Rating");
            return;
        }

        float totalRating = 0;
        for (Comment comment : comments) {
            totalRating += comment.getRating();
        }
        float averageRating = totalRating / comments.size();
        ratingBar.setRating(averageRating);
        rateTxt.setText(String.format("%.1f Rating", averageRating));

        // Update food document with new average rating
        db.collection("Foods").document(foodId)
                .update("rating", averageRating)
                .addOnFailureListener(e -> Toast.makeText(this, "Error updating rating: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void getAndSetFoodData() {
        // Lấy Bundle chứa các extra từ Intent
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            // Lấy dữ liệu từng trường, cung cấp giá trị mặc định nếu không tồn tại
            foodId = extras.getString("foodId", "");
            String foodName = extras.getString("foodName", "Tên món ăn không xác định");
            double foodPrice = extras.getDouble("foodPrice", 0.0);
            String foodDescription = extras.getString("foodDescription", "Mô tả món ăn đang được cập nhật...");
            String foodImagePath = extras.getString("foodImagePath", "");
            double foodRating = extras.getDouble("foodRating", 0.0);
            String foodCategory = extras.getString("foodCategory", "Chưa phân loại");
            String foodIngredients = extras.getString("foodIngredients", "Nguyên liệu đang được cập nhật...");
            String foodRecipe = extras.getString("foodRecipe", "Công thức đang được cập nhật...");

            // Đặt dữ liệu vào các View
            titleTxt.setText(foodName);
            priceTxt.setText(String.format("%,.0f VNĐ", foodPrice));
            descriptionTxt.setText(foodDescription);
            ingridentTxt.setText(foodIngredients);
            recipeContentTxt.setText(foodRecipe);

            // Đặt RatingBar và Rate Text
            ratingBar.setRating((float) foodRating);
            rateTxt.setText(String.format("%.1f Rating", foodRating));

            // Đặt thể loại món ăn
            categoryTxt.setText(foodCategory);

            // Tải ảnh món ăn bằng Glide
            if (!foodImagePath.isEmpty()) {
                Glide.with(this)
                        .load(foodImagePath)
                        .placeholder(R.drawable.food_placeholder)
                        .error(R.drawable.food_placeholder)
                        .into(imageView8);
            } else {
                imageView8.setImageResource(R.drawable.food_placeholder);
            }

            // Nếu không có recipe từ Intent, thử lấy từ Firestore
            if (foodRecipe.equals("Công thức đang được cập nhật...") && !foodId.isEmpty()) {
                loadFoodDataFromFirestore();
            }
        }
    }

    private void loadFoodDataFromFirestore() {
        if (foodId == null || foodId.isEmpty()) return;

        db.collection("Foods")
                .document(foodId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String recipe = documentSnapshot.getString("recipe");
                        if (recipe != null && !recipe.isEmpty()) {
                            recipeContentTxt.setText(recipe);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tải công thức: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Phương thức này thiết lập các lắng nghe sự kiện cho các View.
     */
    private void setupListeners() {
        // Xử lý sự kiện click cho nút quay lại
        backBtn.setOnClickListener(v -> finish()); // Đóng DetailActivity và quay lại màn hình trước đó

        // Xử lý sự kiện click cho nút yêu thích (placeholder)
        favBtn.setOnClickListener(v -> {
            // TODO: Triển khai logic yêu thích tại đây
            // Ví dụ: Thay đổi icon yêu thích, lưu trạng thái yêu thích vào cơ sở dữ liệu (Firebase, Room...)
        });

        submitRatingBtn.setOnClickListener(v -> submitRating());
    }

    private void submitRating() {
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        String commentText = commentInput.getText().toString().trim();
        float rating = userRatingBar.getRating();

        if (rating == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
            return;
        }

        if (commentText.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập bình luận", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create new comment
        String commentId = UUID.randomUUID().toString();
        Comment comment = new Comment(
                commentId,
                currentUser.getUid(),
                currentUser.getDisplayName(),
                currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : "",
                foodId,
                commentText,
                rating
        );

        // Save to Firebase
        db.collection("Foods")
                .document(foodId)
                .collection("comments")
                .document(commentId)
                .set(comment)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đánh giá thành công", Toast.LENGTH_SHORT).show();
                    commentInput.setText("");
                    userRatingBar.setRating(0);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi khi gửi đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}