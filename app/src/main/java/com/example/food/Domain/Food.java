package com.example.food.Domain;

import java.util.List;

public class Food {
    private String name;
    private String details;
    private double price;
    private List<String> imageUrls;
    private String id;

    // Empty constructor for Firebase
    public Food() {
    }

    // Constructor with all fields
    public Food(String name, String details, double price, List<String> imageUrls) {
        this.name = name;
        this.details = details;
        this.price = price;
        this.imageUrls = imageUrls;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
} 