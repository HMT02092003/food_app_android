package com.example.food.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Import Glide
import com.example.food.Model.Dish;  // Mình giả định bạn có class Dish
import com.example.food.R;
// import com.squareup.picasso.Picasso; // Xóa hoặc comment dòng này

import java.util.List;

public class PopularDishesAdapter extends RecyclerView.Adapter<PopularDishesAdapter.DishViewHolder> {

    private Context context;
    private List<Dish> dishList;

    public PopularDishesAdapter(Context context, List<Dish> dishList) {
        this.context = context;
        this.dishList = dishList;
    }

    @NonNull
    @Override
    public DishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_popular_dish, parent, false);
        return new DishViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DishViewHolder holder, int position) {
        Dish dish = dishList.get(position);
        holder.tvDishName.setText(dish.getName());
        holder.tvLikes.setText(dish.getLikes() + " lượt yêu thích");

        // Load ảnh món ăn bằng Glide
        // Đảm bảo dish.getImageUrl() trả về một String URL hợp lệ
        Glide.with(holder.itemView.getContext()) // Sử dụng holder.itemView.getContext() hoặc context
                .load(dish.getImageUrl())
                .placeholder(R.drawable.food_placeholder) // Đổi từ placeholder sang food_placeholder nếu đó là tên của bạn
                .error(R.drawable.food_placeholder) // Đổi từ placeholder sang food_placeholder nếu đó là tên của bạn
                .into(holder.imgDish);
    }

    @Override
    public int getItemCount() {
        return dishList.size();
    }

    static class DishViewHolder extends RecyclerView.ViewHolder {
        ImageView imgDish;
        TextView tvDishName, tvLikes;

        public DishViewHolder(@NonNull View itemView) {
            super(itemView);
            imgDish = itemView.findViewById(R.id.imgDish);
            tvDishName = itemView.findViewById(R.id.tvDishName);
            tvLikes = itemView.findViewById(R.id.tvLikes);
            // Đảm bảo item_popular_dish.xml có các ID này
        }
    }
}