package com.example.food.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView; // Thay ImageButton thành ImageView
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.food.FoodModel;
import com.example.food.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminAddFoodActivity extends AppCompatActivity {

    private ImageView backBtn; // Đổi kiểu dữ liệu thành ImageView
    private Button resetBtn, saveButton;
    private EditText itemName, priceInput, ingredientInput, detailsInput;
    private EditText imageUrl1; // Chỉ cần một EditText cho URL ảnh theo giao diện XML
    private LinearLayout photoLayout;
    private Spinner categorySpinner;
    private ArrayAdapter<String> categoryAdapter;
    private List<String> categoryList;

    // Firebase
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

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();

        // Khởi tạo các thành phần giao diện
        initViews();
        setupCategorySpinner();
        setupClickListeners();
    }

    private void initViews() {
        backBtn = findViewById(R.id.backBtn); // Vẫn giữ findViewById với ID cũ
        resetBtn = findViewById(R.id.resetBtn);
        saveButton = findViewById(R.id.saveButton);
        itemName = findViewById(R.id.itemName);
        priceInput = findViewById(R.id.priceInput);
        ingredientInput = findViewById(R.id.ingredientInput);
        detailsInput = findViewById(R.id.detailsInput);
        photoLayout = findViewById(R.id.photoLayout);
        categorySpinner = findViewById(R.id.categorySpinner);

        // Khởi tạo trường nhập URL ảnh
        imageUrl1 = findViewById(R.id.imageUrl1);
    }

    private void setupCategorySpinner() {
        categoryList = new ArrayList<>();
        categoryList.add("Món cơm");
        categoryList.add("Món nước (bún, phở, miến, mì)");
        categoryList.add("Món kho, hầm, om");
        categoryList.add("Món xào, chiên");
        categoryList.add("Món gỏi, nộm");
        categoryList.add("Món canh, súp");
        categoryList.add("Đồ nướng và món ăn đường phố");
        categoryList.add("Bánh và món ăn vặt");
        categoryList.add("Chè và món tráng miệng");
        categoryList.add("Đặc sản vùng miền tiêu biểu");

        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryList);
        categorySpinner.setAdapter(categoryAdapter);
    }

    private void setupClickListeners() {
        // Xử lý sự kiện click cho nút back. Giữ nguyên logic finish() để quay lại activity trước đó.
        backBtn.setOnClickListener(view -> finish());
        // Xử lý sự kiện click cho nút reset. Gọi hàm resetForm() để làm sạch các trường nhập.
        resetBtn.setOnClickListener(view -> resetForm());
        // Xử lý sự kiện click cho nút save. Kiểm tra form hợp lệ trước khi gọi hàm saveFood().
        saveButton.setOnClickListener(view -> {
            if (validateForm()) {
                saveFood();
            }
        });
    }

    private boolean validateForm() {
        // Kiểm tra nếu tên món ăn bị bỏ trống.
        if (itemName.getText().toString().trim().isEmpty()) {
            showToast("Vui lòng nhập tên món ăn");
            return false;
        }

        // Kiểm tra nếu giá tiền bị bỏ trống.
        if (priceInput.getText().toString().trim().isEmpty()) {
            showToast("Vui lòng nhập giá tiền");
            return false;
        }

        // Kiểm tra nếu URL ảnh bị bỏ trống.
        if (imageUrl1.getText().toString().trim().isEmpty()) {
            showToast("Vui lòng nhập URL ảnh");
            return false;
        }

        // Nếu tất cả các trường bắt buộc đều đã được nhập, trả về true.
        return true;
    }

    private void resetForm() {
        // Đặt lại giá trị của các trường nhập về trạng thái ban đầu (rỗng hoặc giá trị mặc định).
        itemName.setText("");
        priceInput.setText("");
        ingredientInput.setText("");
        detailsInput.setText("");
        imageUrl1.setText("");
        categorySpinner.setSelection(0); // Đặt lại spinner về lựa chọn đầu tiên.
    }

    private void saveFood() {
        // Hiển thị thông báo cho người dùng biết rằng đang thực hiện thao tác lưu.
        saveButton.setEnabled(false);
        saveButton.setText("Đang tạo...");

        // Tạo một ID duy nhất cho món ăn mới.
        final String foodId = UUID.randomUUID().toString();
        // Lấy danh mục đã chọn từ Spinner.
        String selectedCategory = categorySpinner.getSelectedItem().toString();

        // Lấy URL ảnh từ EditText. Hiện tại chỉ hỗ trợ một URL ảnh.
        List<String> imageUrls = new ArrayList<>();
        String url1 = imageUrl1.getText().toString().trim();
        if (!url1.isEmpty()) {
            imageUrls.add(url1);
        }

        // Tạo một đối tượng FoodModel để lưu trữ thông tin món ăn.
        FoodModel food = new FoodModel();
        food.setId(foodId);
        food.setCategory(selectedCategory);
        food.setName(itemName.getText().toString().trim());
        // Chuyển đổi giá từ String sang Double. Cần xử lý NumberFormatException nếu người dùng nhập không phải số.
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
        food.setImageUrls(imageUrls);

        // Lưu đối tượng FoodModel vào Firestore trong collection "Foods" với ID là foodId.
        db.collection("Foods")
                .document(foodId)
                .set(food)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Cập nhật giao diện sau khi lưu thành công.
                        saveButton.setEnabled(true);
                        saveButton.setText("TẠO MỚI MÓN ĂN");
                        showToast("Đã thêm món ăn thành công");
                        resetForm();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Cập nhật giao diện khi lưu thất bại.
                        saveButton.setEnabled(true);
                        saveButton.setText("TẠO MỚI MÓN ĂN");
                        showToast("Lỗi khi thêm món ăn: " + e.getMessage());
                        // Ghi log chi tiết lỗi để debug.
                        Log.e("AdminAddFoodActivity", "Lỗi khi lưu món ăn", e);
                    }
                });
    }

    // Hàm hiển thị Toast message ngắn.
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}