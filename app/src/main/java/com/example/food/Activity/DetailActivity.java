package com.example.food.Activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food.Adapter.CommentAdapter;
import com.example.food.Adapter.RecommendedFoodAdapter;
import com.example.food.Domain.Food;
import com.example.food.Model.Comment;
import com.example.food.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    // --- Khai báo View ---
    private ImageView imageView8;
    private ImageView favBtn;
    private ImageView backBtn;
    private TextView titleTxt;
    private TextView priceTxt;
    private RatingBar ratingBar;
    private TextView rateTxt;
    private TextView descriptionTxt;
    private RatingBar userRatingBar;
    private EditText commentInput;
    private Button submitRatingBtn;
    private RecyclerView commentsRecyclerView;
    private RecyclerView recommendedRecyclerView;
    private TextView categoryTxt;
    private TextView ingredientsTxt;
    private TextView recipeContentTxt;

    // --- Firebase Variables ---
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String foodId;

    // --- Comments & Recommended Food Variables ---
    private CommentAdapter commentAdapter;
    private List<Comment> comments;
    private RecommendedFoodAdapter recommendedAdapter;
    private List<Food> recommendedFoods = new ArrayList<>();
    private boolean isFavorite = false;

    // --- Google Maps & Location Variables ---
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private String foodName;
    private static final String PLACES_API_KEY = "YOUR_Maps_API_KEY"; // Thay thế bằng API Key của bạn
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail); // EdgeToEdge đã được xử lý trong layout hoặc theme
        applyWindowInsets();

        // Khởi tạo Firebase
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
        setupRecommendedRecyclerView();
        checkIfFavorite(); // Kiểm tra trạng thái yêu thích ban đầu

        // Khởi tạo Google Maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy mảnh bản đồ", Toast.LENGTH_SHORT).show();
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Lấy foodName từ Intent cho chức năng tìm kiếm nhà hàng
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            foodName = extras.getString("foodName", "Tên món ăn không xác định");
        } else {
            foodName = "Tên món ăn không xác định";
        }
    }

    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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
        descriptionTxt = findViewById(R.id.descriptionTxt);
        userRatingBar = findViewById(R.id.userRatingBar);
        commentInput = findViewById(R.id.commentInput);
        submitRatingBtn = findViewById(R.id.submitRatingBtn);
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        recommendedRecyclerView = findViewById(R.id.recommendedRecyclerView);
        categoryTxt = findViewById(R.id.categoryTxt);
        ingredientsTxt = findViewById(R.id.ingridentTxt);
        recipeContentTxt = findViewById(R.id.recipeContentTxt);

        // Đặt giá trị ban đầu (có thể không cần thiết nếu dữ liệu được tải ngay)
        if (categoryTxt != null) categoryTxt.setText("");
        if (ingredientsTxt != null) ingredientsTxt.setText("");
        if (recipeContentTxt != null) recipeContentTxt.setText("");
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
                        Log.e("DetailActivity", "Error loading comments: " + error.getMessage());
                        Toast.makeText(this, "Lỗi khi tải bình luận: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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

        // Cập nhật rating trung bình vào tài liệu món ăn chính
        db.collection("Foods").document(foodId)
                .update("rating", averageRating)
                .addOnFailureListener(e -> Log.e("DetailActivity", "Error updating average rating: " + e.getMessage()));
    }

    private void setupRecommendedRecyclerView() {
        recommendedRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recommendedAdapter = new RecommendedFoodAdapter(recommendedFoods, this);
        recommendedRecyclerView.setAdapter(recommendedAdapter);
    }

    private void loadRecommendedFoods() {
        if (foodId == null) {
            return;
        }

        db.collection("Foods").document(foodId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Food currentFood = documentSnapshot.toObject(Food.class);
                        if (currentFood != null && currentFood.getCategory() != null) {
                            db.collection("Foods")
                                    .whereEqualTo("category", currentFood.getCategory())
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                        recommendedFoods.clear();
                                        List<Food> sameCategoryFoods = new ArrayList<>();

                                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                            Food food = doc.toObject(Food.class);
                                            if (food != null) {
                                                food.setId(doc.getId());
                                                if (!food.getId().equals(foodId)) { // Không thêm món ăn hiện tại
                                                    sameCategoryFoods.add(food);
                                                }
                                            }
                                        }

                                        Collections.shuffle(sameCategoryFoods); // Xáo trộn để có thứ tự ngẫu nhiên
                                        int count = Math.min(sameCategoryFoods.size(), 10); // Lấy tối đa 10 món
                                        for (int i = 0; i < count; i++) {
                                            recommendedFoods.add(sameCategoryFoods.get(i));
                                        }

                                        recommendedAdapter.notifyDataSetChanged();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "Không thể tải món ăn đề xuất", Toast.LENGTH_SHORT).show());
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Không thể tải thông tin món ăn", Toast.LENGTH_SHORT).show());
    }

    private void getAndSetFoodData() {
        foodId = getIntent().getStringExtra("foodId");
        if (foodId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin món ăn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadFoodDataFromFirestore();
    }

    private void loadFoodDataFromFirestore() {
        db.collection("Foods").document(foodId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Food food = documentSnapshot.toObject(Food.class);
                        if (food != null) {
                            food.setId(documentSnapshot.getId());
                            // Cập nhật foodName cho chức năng tìm kiếm nhà hàng
                            foodName = food.getName();

                            titleTxt.setText(food.getName());
                            priceTxt.setText(String.format("%.0f$", food.getPrice()));
                            descriptionTxt.setText(food.getDetails());

                            if (categoryTxt != null) categoryTxt.setText(food.getCategory() != null ? food.getCategory() : "Chưa phân loại");
                            if (ingredientsTxt != null) ingredientsTxt.setText(food.getIngredients() != null ? food.getIngredients() : "Chưa có thông tin nguyên liệu");
                            if (recipeContentTxt != null) recipeContentTxt.setText(food.getRecipe() != null ? food.getRecipe() : "Chưa có công thức");

                            if (food.getRating() > 0) {
                                ratingBar.setRating(food.getRating());
                                rateTxt.setText(String.format("%.1f Rating", food.getRating()));
                            } else {
                                ratingBar.setRating(0);
                                rateTxt.setText("0 Rating");
                            }

                            if (food.getImageUrls() != null && !food.getImageUrls().isEmpty()) {
                                Glide.with(this).load(food.getImageUrls().get(0)).into(imageView8);
                            } else {
                                imageView8.setImageResource(R.drawable.food_placeholder);
                            }

                            loadRecommendedFoods();
                        }
                    } else {
                        Toast.makeText(this, "Không tìm thấy thông tin món ăn", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tải thông tin món ăn", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    /**
     * Phương thức này thiết lập các lắng nghe sự kiện cho các View.
     */
    private void setupListeners() {
        backBtn.setOnClickListener(v -> finish());

        favBtn.setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(this, "Vui lòng đăng nhập để thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                return;
            }
            toggleFavorite();
        });

        submitRatingBtn.setOnClickListener(v -> submitRating());
    }

    private void toggleFavorite() {
        if (currentUser == null || foodId == null) return;

        String userId = currentUser.getUid();
        DocumentReference userFavoritesRef = db.collection("Users").document(userId).collection("Favorites").document(foodId);

        if (isFavorite) {
            userFavoritesRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        isFavorite = false;
                        favBtn.setImageResource(R.drawable.ic_favorite_white);
                        Toast.makeText(this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi khi xóa khỏi yêu thích: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        } else {
            userFavoritesRef.set(new HashMap<String, Object>() {{
                        put("foodId", foodId);
                        put("timestamp", System.currentTimeMillis());
                    }})
                    .addOnSuccessListener(aVoid -> {
                        isFavorite = true;
                        favBtn.setImageResource(R.drawable.ic_favorite_filled);
                        Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi khi thêm vào yêu thích: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void checkIfFavorite() {
        if (currentUser == null || foodId == null) return;

        String userId = currentUser.getUid();
        db.collection("Users")
                .document(userId)
                .collection("Favorites")
                .document(foodId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    isFavorite = documentSnapshot.exists();
                    favBtn.setImageResource(isFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_white);
                })
                .addOnFailureListener(e ->
                        Log.e("DetailActivity", "Error checking favorite status: " + e.getMessage())
                );
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

        // Tạo bình luận mới
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

        // Lưu vào Firebase
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

    // --- Google Maps and Location Logic ---

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Kiểm tra quyền trước khi lấy vị trí
        if (checkLocationPermission()) {
            getLastLocationAndSearchRestaurants();
        }
    }

    private boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    private void getLastLocationAndSearchRestaurants() {
        // Kiểm tra lại quyền trước khi yêu cầu vị trí
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Quyền chưa được cấp, không làm gì
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        currentLocation = location;
                        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        updateMapWithUserLocation(userLatLng);
                        searchNearbyRestaurants(userLatLng, foodName);
                    } else {
                        // Nếu không có vị trí cuối cùng, yêu cầu cập nhật vị trí mới
                        requestNewLocationUpdates();
                    }
                });
    }

    private void requestNewLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)
                .setNumUpdates(1);

        // Kiểm tra lại quyền trước khi yêu cầu cập nhật
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    currentLocation = location;
                    LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    updateMapWithUserLocation(userLatLng);
                    searchNearbyRestaurants(userLatLng, foodName);
                } else {
                    Toast.makeText(DetailActivity.this, "Không lấy được vị trí hiện tại", Toast.LENGTH_SHORT).show();
                }
            }
        }, Looper.getMainLooper());
    }

    private void updateMapWithUserLocation(LatLng userLatLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));
        mMap.addMarker(new MarkerOptions()
                .position(userLatLng)
                .title("Vị trí của bạn")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Được cấp quyền, thử tải lại vị trí và tìm kiếm nhà hàng
                if (mMap != null) {
                    getLastLocationAndSearchRestaurants();
                }
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền vị trí để sử dụng chức năng này", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void searchNearbyRestaurants(LatLng location, String foodName) {
        int radius = 5000; // Bán kính 5km
        String url = "";
        try {
            String encodedFoodName = URLEncoder.encode(foodName, "UTF-8");
            url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                    "?location=" + location.latitude + "," + location.longitude +
                    "&radius=" + radius +
                    "&type=restaurant" +
                    "&keyword=" + encodedFoodName +
                    "&key=" + PLACES_API_KEY;
        } catch (Exception e) {
            Log.e("PLACES_API", "Lỗi mã hóa tên món ăn: " + e.getMessage());
            Toast.makeText(this, "Lỗi mã hóa tên món ăn", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(DetailActivity.this, "Lỗi khi tìm nhà hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d("PLACES_API", "RESPONSE: " + responseData);
                    try {
                        JSONObject json = new JSONObject(responseData);
                        JSONArray results = json.getJSONArray("results");
                        runOnUiThread(() -> {
                            mMap.clear(); // Xóa tất cả các marker cũ trước khi thêm mới
                            updateMapWithUserLocation(location); // Thêm lại marker vị trí người dùng

                            if (results.length() == 0) {
                                Toast.makeText(DetailActivity.this,
                                        "Không tìm thấy nhà hàng nào phục vụ '" + foodName + "' trong bán kính 5km!",
                                        Toast.LENGTH_LONG).show();
                            }

                            for (int i = 0; i < results.length(); i++) {
                                try {
                                    JSONObject place = results.getJSONObject(i);
                                    JSONObject geometry = place.getJSONObject("geometry").getJSONObject("location");
                                    double lat = geometry.getDouble("lat");
                                    double lng = geometry.getDouble("lng");
                                    String name = place.getString("name");
                                    String address = place.optString("vicinity", "");
                                    double rating = place.optDouble("rating", 0.0);

                                    MarkerOptions markerOptions = new MarkerOptions()
                                            .position(new LatLng(lat, lng))
                                            .title(name)
                                            .snippet("Địa chỉ: " + address + "\nĐánh giá: " + rating + "⭐")
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

                                    mMap.addMarker(markerOptions);
                                } catch (JSONException e) {
                                    Log.e("PLACES_API", "Lỗi phân tích JSON kết quả: " + e.getMessage());
                                }
                            }
                            // Di chuyển camera để hiển thị tất cả các marker (nếu cần)
                            // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13.0f)); // Giữ nguyên zoom hoặc điều chỉnh
                        });
                    } catch (JSONException e) {
                        Log.e("PLACES_API", "Lỗi phân tích JSON response: " + e.getMessage());
                    }
                }
            }
        });
    }
}