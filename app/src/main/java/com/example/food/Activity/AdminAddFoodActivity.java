package com.example.food.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.view.inputmethod.InputMethodManager;

public class AdminAddFoodActivity extends AppCompatActivity {

    private ImageView backBtn;
    private Button resetBtn, saveButton;
    private EditText itemName, priceInput, ingredientInput, detailsInput;
    private EditText imageUrl1;
    private EditText recipeInput;
    private LinearLayout photoLayout;
    private Spinner categorySpinner;
    private ArrayAdapter<String> categoryAdapter;
    private List<String> categoryList;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_add_food);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        initViews();
        setupCategorySpinner();
        setupClickListeners();
        setupHideKeyboardOnTouchOutside();
    }

    private void initViews() {
        backBtn = findViewById(R.id.backBtn);
        resetBtn = findViewById(R.id.resetBtn);
        saveButton = findViewById(R.id.saveButton);
        itemName = findViewById(R.id.itemName);
        priceInput = findViewById(R.id.priceInput);
        ingredientInput = findViewById(R.id.ingredientInput);
        detailsInput = findViewById(R.id.detailsInput);
        recipeInput = findViewById(R.id.recipeInput);
        photoLayout = findViewById(R.id.photoLayout);
        categorySpinner = findViewById(R.id.categorySpinner);
        imageUrl1 = findViewById(R.id.imageUrl1);
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
        saveButton.setOnClickListener(view -> {
            if (validateForm()) {
                saveFood();
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
        recipeInput.setText("");
        imageUrl1.setText("");
        categorySpinner.setSelection(0);
    }

    private void saveFood() {
        saveButton.setEnabled(false);
        saveButton.setText("Đang tạo...");

        final String foodId = UUID.randomUUID().toString();
        String selectedCategory = categorySpinner.getSelectedItem().toString();

        List<String> imageUrls = new ArrayList<>();
        String url1 = imageUrl1.getText().toString().trim();
        if (!url1.isEmpty()) {
            imageUrls.add(url1);
        }

        FoodModel food = new FoodModel();
        food.setId(foodId);
        food.setCategory(selectedCategory);
        food.setName(itemName.getText().toString().trim());
        try {
            food.setPrice(Double.parseDouble(priceInput.getText().toString().trim()));
        } catch (NumberFormatException e) {
            showToast("Giá tiền không hợp lệ");
            saveButton.setEnabled(true);
            saveButton.setText("TẠO MỚI MÓN ĂN");
            return;
        }
        food.setIngredients(ingredientInput.getText().toString().trim());
        food.setDetails(detailsInput.getText().toString().trim());
        food.setRecipe(recipeInput.getText().toString().trim());
        food.setImageUrls(imageUrls);

        db.collection("Foods")
                .document(foodId)
                .set(food)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        saveButton.setEnabled(true);
                        saveButton.setText("TẠO MỚI MÓN ĂN");
                        showToast("Đã thêm món ăn thành công");
                        resetForm();

                        // Chuyển về trang AdminFoodActivity
                        Intent intent = new Intent(AdminAddFoodActivity.this, AdminFoodActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Đảm bảo không có nhiều instance của Activity
                        startActivity(intent);
                        finish(); // Kết thúc Activity hiện tại
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        saveButton.setEnabled(true);
                        saveButton.setText("TẠO MỚI MÓN ĂN");
                        showToast("Lỗi khi thêm món ăn: " + e.getMessage());
                        Log.e("AdminAddFoodActivity", "Lỗi khi lưu món ăn", e);
                    }
                });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void setupHideKeyboardOnTouchOutside() {
        View root = findViewById(android.R.id.content);
        if (root != null) {
            root.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    View currentFocus = getCurrentFocus();
                    if (currentFocus instanceof EditText) {
                        currentFocus.clearFocus();
                        hideKeyboard(this);
                    }
                }
                return false;
            });
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) view = new View(activity);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
