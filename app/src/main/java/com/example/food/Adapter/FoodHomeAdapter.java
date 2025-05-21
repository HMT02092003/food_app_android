package com.example.food.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food.Model.FoodModel;
import com.example.food.R;

import java.util.List;

public class FoodHomeAdapter extends RecyclerView.Adapter<FoodHomeAdapter.ViewHolder> {

    private Context context;
    private List<FoodModel> foodList;

    public FoodHomeAdapter(Context context, List<FoodModel> foodList) {
        this.context = context;
        this.foodList = foodList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food_home, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodModel food = foodList.get(position);
        holder.foodNameTextView.setText(food.getName());
        holder.foodPriceTextView.setText(String.format("%.0f VNĐ", food.getPrice()));
        holder.foodDescriptionTextView.setText(food.getDetails());

        // Luôn hiển thị ảnh món ăn
        if (holder.foodImageView != null) {
            holder.foodImageView.setVisibility(View.VISIBLE);
            if (food.getImageUrls() != null && !food.getImageUrls().isEmpty()) {
                Glide.with(context)
                    .load(food.getImageUrls().get(0))
                    .placeholder(R.drawable.food_placeholder)
                    .into(holder.foodImageView);
            } else {
                holder.foodImageView.setImageResource(R.drawable.food_placeholder);
            }
        }

        // Không set GONE cho foodImageView nữa
        // Không ẩn các view khác ở đây

        holder.btnDetail.setOnClickListener(v -> {
            // Xử lý khi click vào nút Xem chi tiết
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView foodImageView;
        TextView foodNameTextView;
        TextView foodPriceTextView;
        TextView foodDescriptionTextView;
        View btnDetail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            foodImageView = itemView.findViewById(R.id.foodImageView);
            foodNameTextView = itemView.findViewById(R.id.foodNameTextView);
            foodPriceTextView = itemView.findViewById(R.id.foodPriceTextView);
            foodDescriptionTextView = itemView.findViewById(R.id.foodDescriptionTextView);
            btnDetail = itemView.findViewById(R.id.btnDetail);
        }
    }
}