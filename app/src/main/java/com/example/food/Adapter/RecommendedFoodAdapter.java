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
import com.example.food.Domain.Food;
import com.example.food.R;

import java.util.List;

public class RecommendedFoodAdapter extends RecyclerView.Adapter<RecommendedFoodAdapter.ViewHolder> {
    private List<Food> foods;
    private Context context;

    public RecommendedFoodAdapter(List<Food> foods, Context context) {
        this.foods = foods;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.viewholder_recommended_food, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Food food = foods.get(position);
        holder.titleTxt.setText(food.getName());
        holder.priceTxt.setText(String.format("%,.0f VNĐ", food.getPrice()));

        if (food.getImageUrls() != null && !food.getImageUrls().isEmpty()) {
            Glide.with(context)
                .load(food.getImageUrls().get(0))
                .into(holder.pic);
        } else {
            holder.pic.setImageResource(R.drawable.food_placeholder);
        }

        holder.favoriteIcon.setVisibility(View.GONE);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("foodId", food.getId());
            intent.putExtra("foodName", food.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return foods.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTxt, priceTxt;
        ImageView pic, favoriteIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.titleTxt);
            priceTxt = itemView.findViewById(R.id.priceTxt);
            pic = itemView.findViewById(R.id.pic);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
        }
    }
} 