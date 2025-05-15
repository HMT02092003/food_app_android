package com.example.food.Model;

public class Dish {
    private String name;
    private String imageUrl;
    private int likes;

    public Dish(String name, String imageUrl, int likes) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.likes = likes;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getLikes() {
        return likes;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }
}
