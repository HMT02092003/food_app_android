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

        // Kiểm tra xem danh sách imageUrls có tồn tại và không rỗng hay không
        if (food.getImageUrls() != null && !food.getImageUrls().isEmpty()) {
            // Lấy URL đầu tiên từ danh sách (hoặc bạn có thể chọn một cách khác nếu cần)
            String imageUrl = food.getImageUrls().get(0);
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.food_placeholder)
                    .into(holder.foodImageView);
        } else {
            // Nếu không có URL ảnh, hiển thị placeholder
            Glide.with(context)
                    .load(R.drawable.food_placeholder)
                    .into(holder.foodImageView);
        }

        holder.itemView.setOnClickListener(v -> {
            // Xử lý khi click vào một món ăn (ví dụ: mở trang chi tiết)
            // Bạn có thể truyền ID của món ăn qua Intent
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            foodImageView = itemView.findViewById(R.id.foodImageView);
            foodNameTextView = itemView.findViewById(R.id.foodNameTextView);
            foodPriceTextView = itemView.findViewById(R.id.foodPriceTextView);
        }
    }
}