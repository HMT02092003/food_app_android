package com.example.food.Model;

import java.util.List;

public class FoodModel {
    private String id;
    private String name;
    private double price;
    private String ingredients;
    private String details;
    private List<String> imageUrls;
    private String category; // Thêm trường category

    public FoodModel() {
        // Empty constructor needed for Firestore
    }

    public FoodModel(String id, String name, double price, String ingredients, String details, List<String> imageUrls, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.ingredients = ingredients;
        this.details = details;
        this.imageUrls = imageUrls;
        this.category = category; // Khởi tạo category trong constructor
    }

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

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    // Getter cho category
    public String getCategory() {
        return category;
    }

    // Setter cho category
    public void setCategory(String category) {
        this.category = category;
    }
}