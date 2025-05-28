package com.example.food.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food.Domain.Food;
import com.example.food.R;

import java.util.List;

public class RecommendedFoodAdapter extends RecyclerView.Adapter<RecommendedFoodAdapter.ViewHolder> {
    private List<Food> foods;
    private OnDeleteClickListener onDeleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(int position, Food food);
    }

    public RecommendedFoodAdapter(List<Food> foods, OnDeleteClickListener onDeleteClickListener) {
        this.foods = foods;
        this.onDeleteClickListener = onDeleteClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.viewholder_recommended_food, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Food food = foods.get(position);
        holder.titleTxt.setText(food.getName());
        holder.descriptionTxt.setText(food.getDetails());
        holder.priceTxt.setText(String.format("%,.0f VNĐ", food.getPrice()));
        if (food.getImageUrls() != null && !food.getImageUrls().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(food.getImageUrls().get(0))
                .into(holder.pic);
        } else {
            holder.pic.setImageResource(R.drawable.intro_pic);
        }
        holder.btnDelete.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(position, food);
            }
        });
    }

    @Override
    public int getItemCount() {
        return foods.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTxt, descriptionTxt, priceTxt;
        ImageView pic;
        Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.titleTxt);
            descriptionTxt = itemView.findViewById(R.id.descriptionTxt);
            priceTxt = itemView.findViewById(R.id.priceTxt);
            pic = itemView.findViewById(R.id.pic);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
} 