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
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class MyPendingRecipesAdapter extends RecyclerView.Adapter<MyPendingRecipesAdapter.ViewHolder> {
    private List<PendingRecipe> recipeList;
    private OnRecipeActionListener listener;
    private int layoutId = R.layout.item_my_pending_recipe;

    public interface OnRecipeActionListener {
        void onEdit(PendingRecipe recipe);
        void onDelete(PendingRecipe recipe);
    }

    public MyPendingRecipesAdapter(List<PendingRecipe> recipeList, OnRecipeActionListener listener) {
        this.recipeList = recipeList;
        this.listener = listener;
    }

    public MyPendingRecipesAdapter(List<PendingRecipe> recipeList, OnRecipeActionListener listener, int layoutId) {
        this.recipeList = recipeList;
        this.listener = listener;
        this.layoutId = layoutId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PendingRecipe recipe = recipeList.get(position);
        if (holder.name != null) holder.name.setText(recipe.getName());
        if (holder.category != null) holder.category.setText(recipe.getCategory());
        if (holder.price != null) {
            NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
            String priceText = nf.format(recipe.getPrice()) + " VNĐ";
            holder.price.setText(priceText);
        }

        // Hiển thị trạng thái nếu có view
        if (holder.status != null) {
            String status = recipe.getStatus();
            String statusText = "";
            switch (status) {
                case "pending":
                    statusText = "Đang chờ duyệt";
                    break;
                case "approved":
                    statusText = "Đã được duyệt";
                    break;
                case "rejected":
                    statusText = "Đã bị từ chối";
                    break;
            }
            holder.status.setText(statusText);
        }

        // Load ảnh từ URL
        if (recipe.getImageUrls() != null && !recipe.getImageUrls().isEmpty() && recipe.getImageUrls().get(0) != null && !recipe.getImageUrls().get(0).isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(recipe.getImageUrls().get(0))
                .placeholder(R.drawable.food_placeholder)
                .error(R.drawable.food_placeholder)
                .into(holder.recipeImage);
        } else {
            holder.recipeImage.setImageResource(R.drawable.food_placeholder);
        }

        holder.iconEdit.setOnClickListener(v -> listener.onEdit(recipe));
        holder.iconDelete.setOnClickListener(v -> listener.onDelete(recipe));
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, category, price, status;
        ImageView recipeImage;
        ImageView iconEdit, iconDelete;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textName);
            category = itemView.findViewById(R.id.textCategory);
            price = itemView.findViewById(R.id.textPrice);
            status = itemView.findViewById(R.id.textStatus);
            recipeImage = itemView.findViewById(R.id.recipeImage);
            iconEdit = itemView.findViewById(R.id.iconEdit);
            iconDelete = itemView.findViewById(R.id.iconDelete);
        }
    }
} 