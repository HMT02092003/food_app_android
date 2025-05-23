package com.example.food.Model;

import java.util.Date;

public class Comment {
    private String id;
    private String userId;
    private String userName;
    private String userPhoto;
    private String foodId;
    private String commentText;
    private float rating;
    private Date timestamp;

    public Comment() {
        // Required empty constructor for Firebase
    }

    public Comment(String id, String userId, String userName, String userPhoto, String foodId, 
                  String commentText, float rating) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userPhoto = userPhoto;
        this.foodId = foodId;
        this.commentText = commentText;
        this.rating = rating;
        this.timestamp = new Date();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
} 