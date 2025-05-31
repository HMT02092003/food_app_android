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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import android.util.Log;

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
    private boolean isFavorite = false;

    // Google Maps variables
    private GoogleMap mMap;
    private static final LatLng HANOI = new LatLng(21.0285, 105.8542);
    private String selectedLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private String foodName;
    private static final String PLACES_API_KEY = "AIzaSyB1WBuHRowfPITiKK8DqkH3-RzVw-0Paj0"; // Replace with your new API key
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
            Places.initialize(getApplicationContext(), PLACES_API_KEY); // Đảm bảo PLACES_API_KEY là API Key của bạn
        }
        placesClient = Places.createClient(this);
    }

    private boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }
    private void searchNearbyRestaurants(LatLng location, String foodName) {
        int radius = 5000; // 5km radius
        String url = "";
        try {
            // Encode food name for URL
            String encodedFoodName = URLEncoder.encode(foodName, "UTF-8");
            url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                    "?location=" + location.latitude + "," + location.longitude +
                    "&radius=" + radius +
                    "&type=restaurant" +
                    "&keyword=" + encodedFoodName + // Add food name as keyword
                    "&key=" + PLACES_API_KEY;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi mã hóa tên món ăn", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(DetailActivity.this, "Lỗi khi tìm nhà hàng", Toast.LENGTH_SHORT).show());
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
                            mMap.clear();
                            // Add user location marker
                            mMap.addMarker(new MarkerOptions()
                                    .position(location)
                                    .title("Vị trí của bạn")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                            if (results.length() == 0) {
                                Toast.makeText(DetailActivity.this, 
                                    "Không tìm thấy nhà hàng nào phục vụ " + foodName + " trong bán kính 5km!", 
                                    Toast.LENGTH_SHORT).show();
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
                                    
                                    // Create marker with custom icon and info
                                    MarkerOptions markerOptions = new MarkerOptions()
                                            .position(new LatLng(lat, lng))
                                            .title(name)
                                            .snippet("Địa chỉ: " + address + "\nĐánh giá: " + rating + "⭐")
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                    
                                    mMap.addMarker(markerOptions);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            
                            // Move camera to show all markers
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13.0f));
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
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

            // Check if food is in favorites
            checkIfFavorite();

            // If no recipe from Intent, try to get from Firestore
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