package com.example.food.Model;

import java.util.List;

public class FoodModel {
    private String id;
    private String name;
    private double price;
    private String ingredients; // Trường nguyên liệu
    private String details;     // Trường mô tả chi tiết món ăn
    private String recipe;      // *** ĐÃ THÊM: Trường công thức món ăn ***
    private List<String> imageUrls; // Danh sách các URL ảnh
    private String category;
    private float rating;
    private int reviewCount; // Trường để lưu số lượng đánh giá
    private String status; // Trường để lưu trạng thái của công thức
    private String userId; // Trường để lưu ID của người dùng đăng công thức

    public FoodModel() {
        // Constructor rỗng cần thiết cho Firestore để tự động chuyển đổi dữ liệu
    }

    // Constructor đầy đủ
    // Đã bao gồm tất cả các trường hiện có, bao gồm recipe và reviewCount
    public FoodModel(String id, String name, double price, String ingredients, String details,
                     String recipe, List<String> imageUrls, String category, float rating, int reviewCount, String status, String userId) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.ingredients = ingredients;
        this.details = details;
        this.recipe = recipe; // Khởi tạo recipe
        this.imageUrls = imageUrls;
        this.category = category;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.status = status;
        this.userId = userId;
    }

    // --- Getters và Setters ---
    // Firestore sử dụng các phương thức này để ánh xạ dữ liệu từ/đến các tài liệu

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

    // *** ĐÃ THÊM: Getter và Setter cho recipe ***
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

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}