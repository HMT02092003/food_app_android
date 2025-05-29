package com.example.food.Model;

import java.util.List;

public class PendingRecipe {
    private String id;
    private String name;
    private double price;
    private String ingredients;
    private String details;
    private String recipe;
    private List<String> imageUrls;
    private String category;
    private float rating;
    private int reviewCount;
    private String status;
    private String userId;

    public PendingRecipe() {
        // Constructor rỗng cần thiết cho Firestore
    }

    public PendingRecipe(String id, String name, double price, String ingredients, String details,
                         String recipe, List<String> imageUrls, String category, float rating, int reviewCount, String status, String userId) {
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
        this.status = status;
        this.userId = userId;
    }

    // Getters và Setters
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