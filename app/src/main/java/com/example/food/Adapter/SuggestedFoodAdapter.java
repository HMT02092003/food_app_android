package com.example.food.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food.Activity.DetailActivity;
import com.example.food.Model.FoodModel;
import com.example.food.R;

import java.util.List;

public class SuggestedFoodAdapter extends RecyclerView.Adapter<SuggestedFoodAdapter.SuggestedFoodViewHolder> {

    private Context context;
    private List<FoodModel> suggestedFoods;

    public SuggestedFoodAdapter(Context context, List<FoodModel> suggestedFoods) {
        this.context = context;
        this.suggestedFoods = suggestedFoods;
    }

    @NonNull
    @Override
    public SuggestedFoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_suggested_food, parent, false);
        return new SuggestedFoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestedFoodViewHolder holder, int position) {
        FoodModel food = suggestedFoods.get(position);
        
        holder.suggestedFoodName.setText(food.getName());
        holder.suggestedFoodPrice.setText(String.format("%,.0f VNĐ", food.getPrice()));

        if (food.getImageUrls() != null && !food.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(food.getImageUrls().get(0))
                    .placeholder(R.drawable.food_placeholder)
                    .error(R.drawable.food_placeholder)
                    .into(holder.suggestedFoodImage);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("foodId", food.getId());
            intent.putExtra("foodName", food.getName());
            intent.putExtra("foodPrice", food.getPrice());
            intent.putExtra("foodDescription", food.getDetails());
            intent.putExtra("foodIngredients", food.getIngredients());
            intent.putExtra("foodRecipe", food.getRecipe());
            intent.putExtra("foodCategory", food.getCategory());
            intent.putExtra("foodRating", food.getRating());
            if (food.getImageUrls() != null && !food.getImageUrls().isEmpty()) {
                intent.putExtra("foodImagePath", food.getImageUrls().get(0));
            }
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return suggestedFoods.size();
    }

    public static class SuggestedFoodViewHolder extends RecyclerView.ViewHolder {
        ImageView suggestedFoodImage;
        TextView suggestedFoodName;
        TextView suggestedFoodPrice;

        public SuggestedFoodViewHolder(@NonNull View itemView) {
            super(itemView);
            suggestedFoodImage = itemView.findViewById(R.id.suggestedFoodImage);
            suggestedFoodName = itemView.findViewById(R.id.suggestedFoodName);
            suggestedFoodPrice = itemView.findViewById(R.id.suggestedFoodPrice);
        }
    }
} 