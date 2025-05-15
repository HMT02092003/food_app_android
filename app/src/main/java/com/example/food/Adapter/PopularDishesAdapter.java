package com.example.food.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.Model.Dish;  // Mình giả định bạn có class Dish
import com.example.food.R;
import com.squareup.picasso.Picasso; // Thư viện load ảnh

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

        // Load ảnh món ăn bằng Picasso hoặc Glide (phải thêm thư viện vào gradle)
        Picasso.get()
                .load(dish.getImageUrl())
                .placeholder(R.drawable.placeholder) // ảnh tạm khi chưa load xong
                .error(R.drawable.placeholder) // ảnh lỗi
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
        }
    }
}
