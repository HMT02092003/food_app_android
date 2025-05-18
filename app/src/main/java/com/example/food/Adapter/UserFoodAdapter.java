// UserFoodAdapter.java (trong thư mục com.example.food.Adapter)
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
import com.example.food.FoodModel;
import com.example.food.R;

import java.util.List;

public class UserFoodAdapter extends RecyclerView.Adapter<UserFoodAdapter.FoodViewHolder> {

    private Context context;
    private List<FoodModel> foodList;

    public UserFoodAdapter(Context context, List<FoodModel> foodList) {
        this.context = context;
        this.foodList = foodList;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food_user_screen, parent, false);
        return new FoodViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodModel currentFood = foodList.get(position);
        holder.tvName.setText(currentFood.getName());
        holder.tvCategory.setText(currentFood.getCategory());
        holder.tvPrice.setText(String.format("%,.0f VNĐ", currentFood.getPrice()));

        if (currentFood.getImageUrls() != null && !currentFood.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(currentFood.getImageUrls().get(0))
                    .into(holder.imgFood);
        }
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