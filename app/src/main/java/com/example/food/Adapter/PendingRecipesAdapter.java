package com.example.food.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.food.Model.PendingRecipe;
import com.example.food.R;
import java.util.List;

public class PendingRecipesAdapter extends RecyclerView.Adapter<PendingRecipesAdapter.ViewHolder> {
    private List<PendingRecipe> recipeList;
    private OnApproveClickListener listener;

    public interface OnApproveClickListener {
        void onApprove(PendingRecipe recipe);
    }

    public PendingRecipesAdapter(List<PendingRecipe> recipeList, OnApproveClickListener listener) {
        this.recipeList = recipeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pending_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PendingRecipe recipe = recipeList.get(position);
        holder.name.setText(recipe.getName());
        holder.description.setText(recipe.getDetails());
        holder.category.setText(recipe.getCategory());
        holder.price.setText(String.format("%.0f VNĐ", recipe.getPrice()));
        
        // Load ảnh từ URL
        if (recipe.getImageUrls() != null && !recipe.getImageUrls().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(recipe.getImageUrls().get(0))
                .into(holder.recipeImage);
        }
        
        holder.approveBtn.setOnClickListener(v -> listener.onApprove(recipe));
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, description, category, price;
        ImageView recipeImage;
        Button approveBtn;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textName);
            description = itemView.findViewById(R.id.textDescription);
            category = itemView.findViewById(R.id.textCategory);
            price = itemView.findViewById(R.id.textPrice);
            recipeImage = itemView.findViewById(R.id.recipeImage);
            approveBtn = itemView.findViewById(R.id.btnApprove);
        }
    }
} 