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
import com.example.food.Model.FoodModel; // Đảm bảo import FoodModel đúng cách
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
        holder.foodPriceTextView.setText(String.format("%,.0f VNĐ", food.getPrice())); // Định dạng giá VNĐ
        holder.foodCategoryTextView.setText(food.getCategory());

        // *** ĐÃ SỬA: Dùng food.getDetails() thay vì food.getDescription() ***
        holder.foodDescriptionTextView.setText(food.getDetails());

        // *** ĐÃ SỬA: Dùng food.getImageUrls() thay vì food.getImagePath() ***
        // Lấy URL của ảnh đầu tiên từ danh sách imageUrls
        if (food.getImageUrls() != null && !food.getImageUrls().isEmpty()) {
            String imageUrl = food.getImageUrls().get(0);
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.food_placeholder) // Ảnh hiển thị tạm thời khi đang tải
                    .error(R.drawable.food_placeholder)     // Ảnh hiển thị khi có lỗi tải
                    .into(holder.foodImageView);
        } else {
            // Nếu không có URL ảnh, hiển thị ảnh placeholder mặc định
            Glide.with(context)
                    .load(R.drawable.food_placeholder)
                    .into(holder.foodImageView);
        }

        // ĐẶT ONCLICK LISTENER CHỈ CHO ẢNH (foodImageView)
        holder.foodImageView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(food);
            }
        });

        // Dòng này đã được comment/xóa trong các lần trước, đảm bảo nó không được kích hoạt
        // nếu bạn chỉ muốn click vào ảnh.
        // holder.itemView.setOnClickListener(v -> {
        //     if (onItemClickListener != null) {
        //         onItemClickListener.onItemClick(food);
        //     }
        // });
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
        TextView foodDescriptionTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            foodImageView = itemView.findViewById(R.id.foodImageView);
            foodNameTextView = itemView.findViewById(R.id.foodNameTextView);
            foodPriceTextView = itemView.findViewById(R.id.foodPriceTextView);
            foodCategoryTextView = itemView.findViewById(R.id.foodCategoryTextView);
            foodDescriptionTextView = itemView.findViewById(R.id.foodDescriptionTextView);
        }
    }
}