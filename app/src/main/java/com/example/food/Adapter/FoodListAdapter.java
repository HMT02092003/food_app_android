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
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.food.Activity.DetailActivity;
import com.example.food.Domain.Foods;
import com.example.food.R;

import java.util.ArrayList;

public class FoodListAdapter extends RecyclerView.Adapter<FoodListAdapter.viewholder> {
    ArrayList<Foods> items;
    Context context;

    public FoodListAdapter(ArrayList<Foods> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public FoodListAdapter.viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(context).inflate(R.layout.viewholder_list_food, parent, false);
        return new viewholder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodListAdapter.viewholder holder, int position) {
        Foods food = items.get(position);
        
        // Set text for each field
        holder.titleTxt.setText(food.getTitle());
        holder.timeTxt.setText(food.getTimeValue() + " min");
        holder.priceTxt.setText(String.format("%,.0f VNĐ", food.getPrice()));
        
        // Handle rating display
        double rating = food.getStar();
        holder.rateTxt.setText(String.format("%.1f", rating));

        // Load image using Glide
        Glide.with(context)
                .load(food.getImagePath())
                .transform(new CenterCrop(), new RoundedCorners(30))
                .placeholder(R.drawable.food_placeholder)
                .error(R.drawable.food_placeholder)
                .into(holder.pic);

        // Add click listener to open detail activity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("foodId", food.getId());
            intent.putExtra("foodTitle", food.getTitle());
            intent.putExtra("foodPrice", food.getPrice());
            intent.putExtra("foodTimeValue", food.getTimeValue());
            intent.putExtra("foodStar", food.getStar());
            intent.putExtra("foodImagePath", food.getImagePath());
            intent.putExtra("foodDescription", food.getDescription());
            intent.putExtra("foodCategoryId", food.getCategoryId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class viewholder extends RecyclerView.ViewHolder {
        TextView titleTxt, priceTxt, rateTxt, timeTxt;
        ImageView pic;

        public viewholder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.titleTxt);
            priceTxt = itemView.findViewById(R.id.priceTxt);
            rateTxt = itemView.findViewById(R.id.rateTxt);
            timeTxt = itemView.findViewById(R.id.commentTxt);
            pic = itemView.findViewById(R.id.img);
        }
    }
}
