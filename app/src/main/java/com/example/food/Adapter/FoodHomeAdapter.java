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
    private OnItemClickListener listener;

    // Interface để xử lý sự kiện click trên item
    public interface OnItemClickListener {
        void onItemClick(FoodModel food);
    }

    // Setter cho listener, để HomeActivity có thể đăng ký
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

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
        holder.foodPriceTextView.setText(String.format("%,.0f VNĐ", food.getPrice()));
        holder.foodDescriptionTextView.setText(food.getDetails());

        // Load hình ảnh bằng Glide
        if (food.getImageUrls() != null && !food.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(food.getImageUrls().get(0))
                    .placeholder(R.drawable.food_placeholder)
                    .error(R.drawable.food_placeholder)
                    .into(holder.foodImageView);
        } else {
            holder.foodImageView.setImageResource(R.drawable.food_placeholder);
        }

        // --- Đặt dữ liệu cho Rating và Review Count ---
        // Đảm bảo FoodModel của bạn có các phương thức getRating() và getReviewCount()
        // Nếu FoodModel không có các trường này, bạn cần thêm chúng vào FoodModel.java
        // và đảm bảo dữ liệu này có trong Firestore.
        holder.ratingTextView.setText(String.valueOf(food.getRating())); // Hiển thị rating (ví dụ: 4.5)
        holder.reviewCountTextView.setText(String.format("(%d đánh giá)", food.getReviewCount())); // Hiển thị số lượng đánh giá (ví dụ: (120 đánh giá))


        // --- Đặt OnClickListener cho toàn bộ itemView ---
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(food);
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
        TextView foodDescriptionTextView;
        TextView ratingTextView;    // Ánh xạ TextView rating
        TextView reviewCountTextView; // Ánh xạ TextView review count

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            foodImageView = itemView.findViewById(R.id.foodImageView);
            foodNameTextView = itemView.findViewById(R.id.foodNameTextView);
            foodPriceTextView = itemView.findViewById(R.id.foodPriceTextView);
            foodDescriptionTextView = itemView.findViewById(R.id.foodDescriptionTextView);
            ratingTextView = itemView.findViewById(R.id.ratingTextView); // Ánh xạ từ item_food_home.xml
            reviewCountTextView = itemView.findViewById(R.id.reviewCountTextView); // Ánh xạ từ item_food_home.xml
        }
    }
}