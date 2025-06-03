package com.example.food.Activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
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

import com.bumptech.glide.Glide;
import com.example.food.Adapter.CommentAdapter;
import com.example.food.Model.Comment;
import com.example.food.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import android.os.Looper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import androidx.core.app.ActivityCompat;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.CircularBounds;
import com.google.android.libraries.places.api.net.SearchNearbyRequest;
import com.google.android.libraries.places.api.net.SearchNearbyResponse;

import java.util.Arrays;

import com.example.food.Domain.Food;
import com.example.food.Adapter.RecommendedFoodAdapter;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Collections;

public class DetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Khai báo các View từ layout activity_detail.xml
    private ImageView imageView8; // Ảnh món ăn chính
    private ImageView favBtn;     // Nút yêu thích
    private ImageView backBtn;    // Nút quay lại
    private TextView titleTxt;    // Tên món ăn
    private TextView priceTxt;    // Giá món ăn
    private RatingBar ratingBar;  // Rating Bar
    private TextView rateTxt;     // Text hiển thị số rating (ví dụ: "5 Rating")
    private TextView descriptionTxt; // Mô tả món ăn
    private RatingBar userRatingBar;
    private EditText commentInput;
    private Button submitRatingBtn;
    private Button submitCommentBtn;
    private RecyclerView commentsRecyclerView;
    private RecyclerView recommendedRecyclerView;
    private RecommendedFoodAdapter recommendedAdapter;
    private List<Food> recommendedFoods = new ArrayList<>();

    private String foodId;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private CommentAdapter commentAdapter;
    private List<Comment> comments;
    private boolean isFavorite = false;

    // Google Maps variables
    private GoogleMap mMap;
    private static final LatLng HANOI = new LatLng(21.0285, 105.8542);
    private String selectedLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private String foodName;
    private static final String PLACES_API_KEY = "AIzaSyCM8udCcG9nU5ShHAEXNg0miJq2zYPcuK4";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private PlacesClient placesClient;

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
        setupRecommendedRecyclerView();

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "Error: Map fragment not found", Toast.LENGTH_SHORT).show();
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Khởi tạo Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), PLACES_API_KEY);
        }
        placesClient = Places.createClient(this);

        // Kiểm tra trạng thái yêu thích và cập nhật icon trái tim
        checkIfFavorite();
    }

    private boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        // Lấy foodName từ intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            foodName = extras.getString("foodName", "Tên món ăn không xác định");
        } else {
            foodName = "Tên món ăn không xác định";
        }
        // Kiểm tra quyền trước khi lấy vị trí
        if (checkLocationPermission()) {
            fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        currentLocation = location;
                        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));
                        mMap.addMarker(new MarkerOptions().position(userLatLng).title("Vị trí của bạn"));
                        // Tìm nhà hàng quanh đây theo tên món ăn
                        searchNearbyRestaurants(userLatLng, foodName);
                    } else {
                        // Nếu không có vị trí cuối cùng, yêu cầu cập nhật vị trí mới
                        LocationRequest locationRequest = LocationRequest.create()
                                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                .setInterval(1000)
                                .setNumUpdates(1);
                        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                Location location1 = locationResult.getLastLocation();
                                if (location1 != null) {
                                    currentLocation = location1;
                                    LatLng userLatLng = new LatLng(location1.getLatitude(), location1.getLongitude());
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));
                                    mMap.addMarker(new MarkerOptions().position(userLatLng).title("Vị trí của bạn"));
                                    searchNearbyRestaurants(userLatLng, foodName);
                                } else {
                                    Toast.makeText(DetailActivity.this, "Không lấy được vị trí hiện tại", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, Looper.getMainLooper());
                    }
                });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Được cấp quyền, reload lại map
                if (mMap != null) {
                    onMapReady(mMap);
                }
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền vị trí để sử dụng chức năng này", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void searchNearbyRestaurants(LatLng location, String foodName) {
        final List<Place.Field> placeFields = Arrays.asList(
            Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS
        );
        CircularBounds circle = CircularBounds.newInstance(location, 10000);
        final List<String> includedTypes = Arrays.asList("restaurant", "cafe");

        final SearchNearbyRequest searchNearbyRequest =
            SearchNearbyRequest.builder(circle, placeFields)
                .setIncludedTypes(includedTypes)
                .setMaxResultCount(20)
                .build();

        placesClient.searchNearby(searchNearbyRequest)
            .addOnSuccessListener(response -> {
                List<Place> places = response.getPlaces();
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(location).title("Vị trí của bạn"));
                for (Place place : places) {
                    if (place.getLatLng() != null && place.getName() != null &&
                        place.getName().toLowerCase().contains(foodName.toLowerCase())) {
                        mMap.addMarker(new MarkerOptions()
                            .position(place.getLatLng())
                            .title(place.getName())
                            .snippet(place.getAddress()));
                    }
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Không tìm được nhà hàng gần bạn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        submitCommentBtn = findViewById(R.id.submitCommentBtn);
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        recommendedRecyclerView = findViewById(R.id.recommendedRecyclerView);
        
        // Initialize category and ingredients TextViews
        TextView categoryTxt = findViewById(R.id.categoryTxt);
        TextView ingredientsTxt = findViewById(R.id.ingridentTxt);
        TextView recipeContentTxt = findViewById(R.id.recipeContentTxt);
        
        // Set initial values
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
        if (foodId == null) return;

        db.collection("Foods")
            .document(foodId)
            .collection("ratings")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.isEmpty()) {
                    ratingBar.setRating(0);
                    rateTxt.setText("0 Rating");
                    return;
                }

                float totalRating = 0;
                int ratingCount = 0;
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Double rating = doc.getDouble("rating");
                    if (rating != null) {
                        totalRating += rating;
                        ratingCount++;
                    }
                }

                float averageRating = ratingCount > 0 ? totalRating / ratingCount : 0;
                ratingBar.setRating(averageRating);
                rateTxt.setText(String.format("%.1f Rating", averageRating));

                // Update food document with new average rating
                db.collection("Foods").document(foodId)
                    .update("rating", averageRating, "reviewCount", ratingCount)
                    .addOnFailureListener(e -> 
                        Toast.makeText(this, "Error updating rating: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Error calculating average rating: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
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

        // First get the current food to get its category
        db.collection("Foods").document(foodId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Food currentFood = documentSnapshot.toObject(Food.class);
                    if (currentFood != null && currentFood.getCategory() != null) {
                        // Then query for foods in the same category
                        db.collection("Foods")
                            .whereEqualTo("category", currentFood.getCategory())
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                recommendedFoods.clear();
                                List<Food> sameCategoryFoods = new ArrayList<>();
                                
                                // Convert documents to Food objects
                                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                    Food food = doc.toObject(Food.class);
                                    if (food != null) {
                                        food.setId(doc.getId());
                                        // Don't add the current food
                                        if (!food.getId().equals(foodId)) {
                                            sameCategoryFoods.add(food);
                                        }
                                    }
                                }
                                
                                // Shuffle the list to get random order
                                Collections.shuffle(sameCategoryFoods);
                                
                                // Take up to 10 items
                                int count = Math.min(sameCategoryFoods.size(), 10);
                                for (int i = 0; i < count; i++) {
                                    recommendedFoods.add(sameCategoryFoods.get(i));
                                }
                                
                                recommendedAdapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Không thể tải món ăn đề xuất", Toast.LENGTH_SHORT).show();
                            });
                    }
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Không thể tải thông tin món ăn", Toast.LENGTH_SHORT).show();
            });
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
                        // Set data to views
                        titleTxt.setText(food.getName());
                        priceTxt.setText(String.format("%,.0f VNĐ", food.getPrice()));
                        descriptionTxt.setText(food.getDetails());
                        
                        // Set category
                        TextView categoryTxt = findViewById(R.id.categoryTxt);
                        if (categoryTxt != null) {
                            categoryTxt.setText(food.getCategory() != null ? food.getCategory() : "Chưa phân loại");
                        }
                        
                        // Set ingredients
                        TextView ingredientsTxt = findViewById(R.id.ingridentTxt);
                        if (ingredientsTxt != null) {
                            ingredientsTxt.setText(food.getIngredients() != null ? food.getIngredients() : "Chưa có thông tin nguyên liệu");
                        }
                        
                        // Set recipe
                        TextView recipeContentTxt = findViewById(R.id.recipeContentTxt);
                        if (recipeContentTxt != null) {
                            recipeContentTxt.setText(food.getRecipe() != null ? food.getRecipe() : "Chưa có công thức");
                        }
                        
                        // Set rating
                        if (food.getRating() > 0) {
                            ratingBar.setRating(food.getRating());
                            rateTxt.setText(String.format("%.1f Rating", food.getRating()));
                        } else {
                            ratingBar.setRating(0);
                            rateTxt.setText("0 Rating");
                        }
                        
                        // Load image
                        if (food.getImageUrls() != null && !food.getImageUrls().isEmpty()) {
                            Glide.with(this)
                                .load(food.getImageUrls().get(0))
                                .into(imageView8);
                        } else {
                            imageView8.setImageResource(R.drawable.food_placeholder);
                        }
                        
                        // Load recommended foods
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
        // Xử lý sự kiện click cho nút quay lại
        backBtn.setOnClickListener(v -> finish());

        // Xử lý sự kiện click cho nút yêu thích
        favBtn.setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(this, "Vui lòng đăng nhập để thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                return;
            }
            toggleFavorite();
        });

        submitRatingBtn.setOnClickListener(v -> submitRating());
        submitCommentBtn.setOnClickListener(v -> submitComment());
    }

    private void toggleFavorite() {
        if (currentUser == null || foodId == null) return;

        String userId = currentUser.getUid();
        DocumentReference userFavoritesRef = db.collection("Users")
                .document(userId)
                .collection("Favorites")
                .document(foodId);

        if (isFavorite) {
            // Remove from favorites
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
            // Add to favorites
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
                    Toast.makeText(this, "Lỗi khi kiểm tra yêu thích: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void submitRating() {
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        float rating = userRatingBar.getRating();

        if (rating == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if user has already rated
        db.collection("Foods")
            .document(foodId)
            .collection("ratings")
            .document(currentUser.getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // User has already rated, update the rating
                    documentSnapshot.getReference()
                        .update("rating", rating, "timestamp", System.currentTimeMillis())
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Đã cập nhật đánh giá", Toast.LENGTH_SHORT).show();
                            loadComments(); // Reload to update average rating
                        })
                        .addOnFailureListener(e -> 
                            Toast.makeText(this, "Lỗi khi cập nhật đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                } else {
                    // User hasn't rated yet, create new rating
                    HashMap<String, Object> ratingData = new HashMap<>();
                    ratingData.put("userId", currentUser.getUid());
                    ratingData.put("userName", currentUser.getDisplayName());
                    ratingData.put("userPhoto", currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : "");
                    ratingData.put("rating", rating);
                    ratingData.put("timestamp", System.currentTimeMillis());

                    documentSnapshot.getReference()
                        .set(ratingData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Đánh giá thành công", Toast.LENGTH_SHORT).show();
                            loadComments(); // Reload to update average rating
                        })
                        .addOnFailureListener(e -> 
                            Toast.makeText(this, "Lỗi khi gửi đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                }
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Lỗi khi kiểm tra đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
    }

    private void submitComment() {
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để bình luận", Toast.LENGTH_SHORT).show();
            return;
        }

        String commentText = commentInput.getText().toString().trim();

        if (commentText.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập bình luận", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get user's rating if exists
        db.collection("Foods")
            .document(foodId)
            .collection("ratings")
            .document(currentUser.getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                float userRating = 0;
                if (documentSnapshot.exists() && documentSnapshot.get("rating") != null) {
                    Object ratingObj = documentSnapshot.get("rating");
                    if (ratingObj instanceof Number) {
                        userRating = ((Number) ratingObj).floatValue();
                    }
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
                    userRating
                );

                // Save to Firebase
                db.collection("Foods")
                    .document(foodId)
                    .collection("comments")
                    .document(commentId)
                    .set(comment)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Bình luận thành công", Toast.LENGTH_SHORT).show();
                        commentInput.setText("");
                    })
                    .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi khi gửi bình luận: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Lỗi khi lấy đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
    }
}