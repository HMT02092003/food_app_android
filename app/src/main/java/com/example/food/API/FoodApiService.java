package com.example.food.API;

import com.example.food.Model.FoodModel;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FoodApiService {
    @GET("foods/featured")
    Call<List<FoodModel>> getFeaturedFoods();
    
    @GET("foods/suggested")
    Call<List<FoodModel>> getSuggestedFoods();
    
    @GET("foods/category")
    Call<List<FoodModel>> getFoodsByCategory(@Query("category") String category);
    
    @GET("foods/search")
    Call<List<FoodModel>> searchFoods(@Query("query") String query);
} 