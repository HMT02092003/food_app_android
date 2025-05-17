package com.example.food.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.food.R;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminUpdateFoodActivity extends AppCompatActivity {

    private EditText itemName, priceInput, ingredientInput, detailsInput, imageUrl1;
    private ImageView backBtn;
    private Button saveButton, resetBtn;
    private String foodId;
    private String foodName;
    private double foodPrice;
    private String foodIngredients;
    private String foodDetails;
    private ArrayList<String> foodImageUrls;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_update_food);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Ánh xạ các view
        backBtn = findViewById(R.id.backBtn);
        resetBtn = findViewById(R.id.resetBtn);
        itemName = findViewById(R.id.itemName);
        priceInput = findViewById(R.id.priceInput);
        ingredientInput = findViewById(R.id.ingredientInput);
        detailsInput = findViewById(R.id.detailsInput);
        imageUrl1 = findViewById(R.id.imageUrl1);
        saveButton = findViewById(R.id.saveButton);

        // Nhận dữ liệu từ Intent
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            foodId = bundle.getString("FOOD_ID");
            foodName = bundle.getString("FOOD_NAME");
            foodPrice = bundle.getDouble("FOOD_PRICE");
            foodIngredients = bundle.getString("FOOD_INGREDIENTS");
            foodDetails = bundle.getString("FOOD_DETAILS");
            foodImageUrls = bundle.getStringArrayList("FOOD_IMAGE_URLS");

            // Hiển thị dữ liệu lên các view
            itemName.setText(foodName);
            priceInput.setText(String.valueOf(foodPrice));
            ingredientInput.setText(foodIngredients);
            detailsInput.setText(foodDetails);
            if (foodImageUrls != null && !foodImageUrls.isEmpty()) {
                imageUrl1.setText(foodImageUrls.get(0));
            }
        }

        // Thiết lập sự kiện cho nút Back
        backBtn.setOnClickListener(v -> onBackPressed());

        // Thiết lập sự kiện cho nút Reset
        resetBtn.setOnClickListener(v -> {
            // Đặt lại các trường về giá trị ban đầu
            itemName.setText(foodName);
            priceInput.setText(String.valueOf(foodPrice));
            ingredientInput.setText(foodIngredients);
            detailsInput.setText(foodDetails);
            if (foodImageUrls != null && !foodImageUrls.isEmpty()) {
                imageUrl1.setText(foodImageUrls.get(0));
            }
            Toast.makeText(AdminUpdateFoodActivity.this, "Đã reset lại các trường", Toast.LENGTH_SHORT).show();
        });

        // Thiết lập sự kiện cho nút Save
        saveButton.setOnClickListener(v -> updateFood());
    }

    private void updateFood() {
        String newName = itemName.getText().toString().trim();
        String newPriceStr = priceInput.getText().toString().trim();
        String newIngredients = ingredientInput.getText().toString().trim();
        String newDetails = detailsInput.getText().toString().trim();
        String newImageUrl1 = imageUrl1.getText().toString().trim();


        // Basic validation
        if (newName.isEmpty() || newPriceStr.isEmpty() || newIngredients.isEmpty() || newDetails.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        double newPrice;
        try {
            newPrice = Double.parseDouble(newPriceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare the data to update
        Map<String, Object> foodData = new HashMap<>();
        foodData.put("name", newName);
        foodData.put("price", newPrice);
        foodData.put("ingredients", newIngredients);
        foodData.put("details", newDetails);

        ArrayList<String> newImageUrls = new ArrayList<>();
        if (!newImageUrl1.isEmpty()) {
            newImageUrls.add(newImageUrl1);
            foodData.put("imageUrls", newImageUrls); // Update image URLs
        } else {
            foodData.put("imageUrls", foodImageUrls);
        }

        // Update the food item in Firestore
        db.collection("Foods").document(foodId)
                .update(foodData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AdminUpdateFoodActivity.this, "Món ăn đã được cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity after successful update
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminUpdateFoodActivity", "Error updating food: ", e);
                    Toast.makeText(AdminUpdateFoodActivity.this, "Lỗi khi cập nhật món ăn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

