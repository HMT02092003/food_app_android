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
        void onDelete(PendingRecipe recipe);
    }

    public PendingRecipesAdapter(List<PendingRecipe> recipeList, OnApproveClickListener listener) {
        this.recipeList = recipeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_pending_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PendingRecipe recipe = recipeList.get(position);
        holder.name.setText(recipe.getName());
        holder.category.setText(recipe.getCategory());
        holder.price.setText(String.format("%.0f VNĐ", recipe.getPrice()));
        holder.status.setText("Trạng thái: Đang chờ duyệt");
        
        // Load ảnh từ URL
        if (recipe.getImageUrls() != null && !recipe.getImageUrls().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(recipe.getImageUrls().get(0))
                .placeholder(R.drawable.food_placeholder)
                .error(R.drawable.food_placeholder)
                .into(holder.recipeImage);
        } else {
            holder.recipeImage.setImageResource(R.drawable.food_placeholder);
        }

        holder.editBtn.setOnClickListener(v -> listener.onApprove(recipe));
        holder.deleteBtn.setOnClickListener(v -> listener.onDelete(recipe));
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, category, price, status;
        ImageView recipeImage;
        Button editBtn, deleteBtn;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textName);
            category = itemView.findViewById(R.id.textCategory);
            price = itemView.findViewById(R.id.textPrice);
            status = itemView.findViewById(R.id.textStatus);
            recipeImage = itemView.findViewById(R.id.recipeImage);
            editBtn = itemView.findViewById(R.id.btnEdit);
            deleteBtn = itemView.findViewById(R.id.btnDelete);
        }
    }
} 