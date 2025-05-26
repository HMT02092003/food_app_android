package com.example.food.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food.Activity.DetailActivity;
import com.example.food.Domain.Foods;
import com.example.food.R;

import java.util.ArrayList;

public class PopularDishesAdapter extends RecyclerView.Adapter<PopularDishesAdapter.Viewholder> {
    ArrayList<Foods> items;
    Context context;

    public PopularDishesAdapter(ArrayList<Foods> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public PopularDishesAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_popular_dishes, parent, false);
        return new Viewholder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull PopularDishesAdapter.Viewholder holder, int position) {
        holder.titleTxt.setText(items.get(position).getTitle());
        holder.priceTxt.setText(items.get(position).getPrice() + " VNĐ");
        holder.starTxt.setText(String.format("%.1f", items.get(position).getStar()));

        // Sử dụng Glide thay vì Picasso
        Glide.with(context)
            .load(items.get(position).getImagePath())
            .placeholder(R.drawable.placeholder_image) // Thêm ảnh placeholder nếu cần
            .error(R.drawable.error_image) // Thêm ảnh error nếu cần
            .into(holder.pic);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("foodId", items.get(position).getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        TextView titleTxt, priceTxt, starTxt;
        ImageView pic;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.titleTxt);
            priceTxt = itemView.findViewById(R.id.priceTxt);
            starTxt = itemView.findViewById(R.id.starTxt);
            pic = itemView.findViewById(R.id.pic);
        }
    }
}
