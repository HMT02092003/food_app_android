package com.example.food.Model;

import java.io.Serializable; // Giữ lại nếu bạn cần truyền đối tượng này qua Intent
import java.util.List;

// Serializable giúp truyền đối tượng qua Intent hoặc Bundle
public class FoodModel implements Serializable {
    // ID này thường được lấy từ document ID của Firestore, không phải là trường lưu trữ bên trong tài liệu
    // Nó được sử dụng để định danh tài liệu trong code Java
    private String id;
    private String name;
    private double price;
    private String ingredients; // Trường nguyên liệu
    private String details;     // Trường mô tả chi tiết món ăn
    private String recipe;      // Trường công thức món ăn
    private List<String> imageUrls; // Danh sách các URL ảnh
    private String category;    // Tên của Category (ví dụ: "Món cơm")
    private double rating;      // Đổi từ float sang double, tương ứng với trường "rating" trong Firestore
    private int reviewCount;    // Trường để lưu số lượng đánh giá
    private int CategoryId;     // ID số của Category (ví dụ: 1 cho "Món cơm"), tương ứng với trường "CategoryId" trong Firestore

    // Constructor rỗng (no-argument constructor) là BẮT BUỘC cho Firestore
    // để nó có thể tự động chuyển đổi dữ liệu từ tài liệu Firestore sang đối tượng Java
    public FoodModel() {
    }

    // Constructor đầy đủ (tùy chọn) - hữu ích khi bạn tạo đối tượng trong code
    public FoodModel(String id, String name, double price, String ingredients, String details,
                     String recipe, List<String> imageUrls, String category, double rating,
                     int reviewCount, int categoryId) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.ingredients = ingredients;
        this.details = details;
        this.recipe = recipe;
        this.imageUrls = imageUrls;
        this.category = category;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.CategoryId = categoryId;
    }

    // --- Getters và Setters ---
    // Firestore sử dụng các phương thức này để ánh xạ dữ liệu từ/đến các tài liệu.
    // Tên của getter/setter phải tuân theo quy tắc Java Bean (getFieldName, setFieldName)
    // để Firestore có thể tự động ánh xạ với các trường trong tài liệu của bạn.

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getRecipe() {
        return recipe;
    }

    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    // Tên Getter/Setter cho CategoryId phải khớp với tên trường trong Firestore ("CategoryId")
    public int getCategoryId() {
        return CategoryId;
    }

    public void setCategoryId(int categoryId) {
        this.CategoryId = categoryId;
    }
}