package com.example.food.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.food.Model.FoodModel;
import com.example.food.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserShareRecipeActivity extends AppCompatActivity {

    private ImageView backBtn;
    private Button resetBtn, submitButton;
    private EditText itemName, priceInput, ingredientInput, detailsInput, recipeInput;
    private EditText imageUrl1;
    private Spinner categorySpinner;
    private ArrayAdapter<String> categoryAdapter;
    private List<String> categoryList;
    private String recipeId;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_share_recipe);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews();
        setupCategorySpinner();
        setupClickListeners();

        // Check if we're editing an existing recipe
        recipeId = getIntent().getStringExtra("recipeId");
        if (recipeId != null) {
            // Load existing recipe data
            String name = getIntent().getStringExtra("recipeName");
            String category = getIntent().getStringExtra("recipeCategory");
            double price = getIntent().getDoubleExtra("recipePrice", 0);
            String ingredients = getIntent().getStringExtra("recipeIngredients");
            String details = getIntent().getStringExtra("recipeDetails");
            String imageUrl = getIntent().getStringExtra("recipeImageUrl");

            // Set the data to views
            itemName.setText(name);
            int categoryPosition = categoryAdapter.getPosition(category);
            if (categoryPosition >= 0) {
                categorySpinner.setSelection(categoryPosition);
            }
            priceInput.setText(String.valueOf(price));
            ingredientInput.setText(ingredients);
            detailsInput.setText(details);
            imageUrl1.setText(imageUrl);
            submitButton.setText("Cập nhật công thức");
        }
    }

    private void initViews() {
        backBtn = findViewById(R.id.backBtn);
        resetBtn = findViewById(R.id.resetBtn);
        submitButton = findViewById(R.id.submitButton);
        itemName = findViewById(R.id.itemName);
        priceInput = findViewById(R.id.priceInput);
        ingredientInput = findViewById(R.id.ingredientInput);
        detailsInput = findViewById(R.id.detailsInput);
        categorySpinner = findViewById(R.id.categorySpinner);
        imageUrl1 = findViewById(R.id.imageUrl1);
        recipeInput = findViewById(R.id.recipeInput);
    }

    private void setupCategorySpinner() {
        categoryList = new ArrayList<>();
        categoryList.add("Tất cả");
        categoryList.add("Món cơm");
        categoryList.add("Món nước");
        categoryList.add("Món kho,hầm");
        categoryList.add("Món chiên,xào");
        categoryList.add("Salad");
        categoryList.add("Món súp");
        categoryList.add("Đồ ăn đường phố");
        categoryList.add("Đồ ăn vặt");
        categoryList.add("Món tráng miệng");
        categoryList.add("Món vùng miền");

        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryList);
        categorySpinner.setAdapter(categoryAdapter);
    }

    private void setupClickListeners() {
        backBtn.setOnClickListener(view -> finish());
        resetBtn.setOnClickListener(view -> resetForm());
        submitButton.setOnClickListener(view -> {
            if (recipeId != null) {
                updateRecipe(recipeId);
            } else {
                submitRecipe();
            }
        });
    }

    private boolean validateForm() {
        if (itemName.getText().toString().trim().isEmpty()) {
            showToast("Vui lòng nhập tên món ăn");
            return false;
        }
        if (priceInput.getText().toString().trim().isEmpty()) {
            showToast("Vui lòng nhập giá tiền");
            return false;
        }
        if (imageUrl1.getText().toString().trim().isEmpty()) {
            showToast("Vui lòng nhập URL ảnh");
            return false;
        }
        return true;
    }

    private void resetForm() {
        itemName.setText("");
        priceInput.setText("");
        ingredientInput.setText("");
        detailsInput.setText("");
        imageUrl1.setText("");
        categorySpinner.setSelection(0);
    }

    private void submitRecipe() {
        submitButton.setEnabled(false);
        submitButton.setText("Đang gửi...");

        final String recipeId = UUID.randomUUID().toString();
        String selectedCategory = categorySpinner.getSelectedItem().toString();

        List<String> imageUrls = new ArrayList<>();
        String url1 = imageUrl1.getText().toString().trim();
        if (!url1.isEmpty()) {
            imageUrls.add(url1);
        }

        FoodModel recipe = new FoodModel();
        recipe.setId(recipeId);
        recipe.setCategory(selectedCategory);
        recipe.setName(itemName.getText().toString().trim());
        try {
            recipe.setPrice(Double.parseDouble(priceInput.getText().toString().trim()));
        } catch (NumberFormatException e) {
            showToast("Giá tiền không hợp lệ");
            submitButton.setEnabled(true);
            submitButton.setText("Gửi công thức");
            return;
        }
        recipe.setIngredients(ingredientInput.getText().toString().trim());
        recipe.setDetails(detailsInput.getText().toString().trim());
        recipe.setImageUrls(imageUrls);
        recipe.setStatus("pending"); // Trạng thái chờ duyệt
        recipe.setUserId(auth.getCurrentUser().getUid()); // ID của người dùng đăng

        db.collection("PendingRecipes")
                .document(recipeId)
                .set(recipe)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        submitButton.setEnabled(true);
                        submitButton.setText("Gửi công thức");
                        showToast("Đã gửi công thức thành công. Vui lòng chờ admin duyệt.");
                        resetForm();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        submitButton.setEnabled(true);
                        submitButton.setText("Gửi công thức");
                        showToast("Lỗi khi gửi công thức: " + e.getMessage());
                        Log.e("UserShareRecipeActivity", "Lỗi khi gửi công thức", e);
                    }
                });
    }

    private void updateRecipe(String recipeId) {
        submitButton.setEnabled(false);
        submitButton.setText("Đang cập nhật...");

        String selectedCategory = categorySpinner.getSelectedItem().toString();

        List<String> imageUrls = new ArrayList<>();
        String url1 = imageUrl1.getText().toString().trim();
        if (!url1.isEmpty()) {
            imageUrls.add(url1);
        }

        FoodModel recipe = new FoodModel();
        recipe.setId(recipeId);
        recipe.setCategory(selectedCategory);
        recipe.setName(itemName.getText().toString().trim());
        try {
            recipe.setPrice(Double.parseDouble(priceInput.getText().toString().trim()));
        } catch (NumberFormatException e) {
            showToast("Giá tiền không hợp lệ");
            submitButton.setEnabled(true);
            submitButton.setText("Cập nhật công thức");
            return;
        }
        recipe.setIngredients(ingredientInput.getText().toString().trim());
        recipe.setDetails(detailsInput.getText().toString().trim());
        recipe.setRecipe(recipeInput.getText().toString().trim());
        recipe.setImageUrls(imageUrls);
        recipe.setStatus("pending");
        recipe.setUserId(auth.getCurrentUser().getUid());

        db.collection("PendingRecipes")
            .document(recipeId)
            .set(recipe)
            .addOnSuccessListener(aVoid -> {
                submitButton.setEnabled(true);
                submitButton.setText("Cập nhật công thức");
                showToast("Đã cập nhật công thức thành công. Vui lòng chờ admin duyệt.");
                finish();
            })
            .addOnFailureListener(e -> {
                submitButton.setEnabled(true);
                submitButton.setText("Cập nhật công thức");
                showToast("Lỗi khi cập nhật công thức: " + e.getMessage());
                Log.e("UserShareRecipeActivity", "Lỗi khi cập nhật công thức", e);
            });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
} 