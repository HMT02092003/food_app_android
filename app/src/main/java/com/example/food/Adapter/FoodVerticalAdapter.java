package com.example.food.Adapter; // Đảm bảo đúng package của bạn

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food.FoodModel;
import com.example.food.R;

import java.util.List;

public class FoodVerticalAdapter extends RecyclerView.Adapter<FoodVerticalAdapter.ViewHolder> {

    private Context context;
    private List<FoodModel> foodList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(FoodModel food);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public FoodVerticalAdapter(Context context, List<FoodModel> foodList) {
        this.context = context;
        this.foodList = foodList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food_vertical, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodModel food = foodList.get(position);
        holder.foodNameTextView.setText(food.getName());
        holder.foodPriceTextView.setText(String.format("%.0f VNĐ", food.getPrice()));
        holder.foodCategoryTextView.setText(food.getCategory());

        // GÁN TEXT CHO MÔ TẢ TẠI ĐÂY
        // Đảm bảo FoodModel của bạn có phương thức getDetails()
        holder.foodDescriptionTextView.setText(food.getDetails());

        if (food.getImageUrls() != null && !food.getImageUrls().isEmpty()) {
            String imageUrl = food.getImageUrls().get(0);
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.food_placeholder)
                    .into(holder.foodImageView);
        } else {
            Glide.with(context)
                    .load(R.drawable.food_placeholder)
                    .into(holder.foodImageView);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(food);
            }
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
        TextView foodCategoryTextView;
        TextView foodDescriptionTextView; // KHAI BÁO TEXTVIEW MỚI

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            foodImageView = itemView.findViewById(R.id.foodImageView);
            foodNameTextView = itemView.findViewById(R.id.foodNameTextView);
            foodPriceTextView = itemView.findViewById(R.id.foodPriceTextView);
            foodCategoryTextView = itemView.findViewById(R.id.foodCategoryTextView);
            foodDescriptionTextView = itemView.findViewById(R.id.foodDescriptionTextView); // ÁNH XẠ TEXTVIEW MỚI
        }
    }
}