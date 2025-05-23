package com.example.food.Activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide; // Import Glide để tải ảnh
import com.example.food.R; // Đảm bảo đúng package của bạn

public class DetailActivity extends AppCompatActivity {

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

        // Khởi tạo các View
        initViews();
        // Lấy và hiển thị dữ liệu món ăn
        getAndSetFoodData();
        // Thiết lập các lắng nghe sự kiện
        setupListeners();
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
    }
    private void getAndSetFoodData() {
        // Lấy Bundle chứa các extra từ Intent
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            // Lấy dữ liệu từng trường, cung cấp giá trị mặc định nếu không tồn tại
            String foodId = extras.getString("foodId", "");
            String foodName = extras.getString("foodName", "Tên món ăn không xác định");
            double foodPrice = extras.getDouble("foodPrice", 0.0);
            String foodDescription = extras.getString("foodDescription", "Mô tả món ăn đang được cập nhật...");
            String foodImagePath = extras.getString("foodImagePath", "");
            double foodRating = extras.getDouble("foodRating", 0.0);
            String foodCategory = extras.getString("foodCategory", "Chưa phân loại");
            String foodIngredients = extras.getString("foodIngredients", "Nguyên liệu đang được cập nhật..."); // Lấy nguyên liệu

            // Đặt dữ liệu vào các View
            titleTxt.setText(foodName);
            priceTxt.setText(String.format("%,.0f VNĐ", foodPrice)); // Định dạng giá VNĐ
            descriptionTxt.setText(foodDescription);
            ingridentTxt.setText(foodIngredients); // Đặt nguyên liệu

            // Đặt RatingBar và Rate Text
            ratingBar.setRating((float) foodRating);
            rateTxt.setText(String.format("%.1f Rating", foodRating)); // Hiển thị rating dạng số (ví dụ: "4.5 Rating")

            // Đặt thể loại món ăn
            categoryTxt.setText(foodCategory);

            // Tải ảnh món ăn bằng Glide
            if (!foodImagePath.isEmpty()) {
                Glide.with(this)
                        .load(foodImagePath)
                        .placeholder(R.drawable.food_placeholder) // Ảnh placeholder khi đang tải
                        .error(R.drawable.food_placeholder) // Ảnh lỗi nếu tải thất bại
                        .into(imageView8);
            } else {
                // Nếu không có đường dẫn ảnh, hiển thị ảnh placeholder mặc định
                imageView8.setImageResource(R.drawable.food_placeholder);
            }
        }
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
    }
}