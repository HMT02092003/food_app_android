package com.example.food.Adapter;

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

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private List<FoodModel> foodList;

    public FoodAdapter(List<FoodModel> foodList) {
        this.foodList = foodList;
    }

    public void updateData(List<FoodModel> newFoodList) {
        this.foodList = newFoodList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food_screen, parent, false); // ĐÃ SỬA TẠI ĐÂY
        return new FoodViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodModel currentFood = foodList.get(position);
        holder.tvName.setText(currentFood.getName());
        holder.tvCategory.setText(currentFood.getCategory());
        holder.tvPrice.setText(String.format("%,.0f VNĐ", currentFood.getPrice()));

        if (currentFood.getImageUrls() != null && !currentFood.getImageUrls().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(currentFood.getImageUrls().get(0)) // Lấy ảnh đầu tiên trong danh sách
                    .into(holder.imgFood);
        }

        // TODO: Xử lý sự kiện click vào item (nếu cần)
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFood;
        TextView tvName;
        TextView tvCategory;
        TextView tvPrice;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.imgFood);
            tvName = itemView.findViewById(R.id.tvName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}