package com.example.food.Adapter;

import android.content.Intent;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.food.Activity.DetailActivity;
import com.example.food.Model.FoodModel;
import com.example.food.R;
import java.util.List;

public class FavoriteFoodAdapter extends RecyclerView.Adapter<FavoriteFoodAdapter.FoodViewHolder> {
    private List<FoodModel> foodList;

    public FavoriteFoodAdapter(List<FoodModel> foodList) {
        this.foodList = foodList;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodModel currentFood = foodList.get(position);
        holder.tvName.setText(currentFood.getName());
        holder.tvPrice.setText(String.format("%,.0f VNĐ", currentFood.getPrice()));
        if (holder.tvCategory != null) holder.tvCategory.setText(currentFood.getCategory());
        holder.tvRating.setText(String.valueOf(currentFood.getRating()));
        holder.tvReviewCount.setText("(" + currentFood.getReviewCount() + " đánh giá)");
        holder.tvDescription.setText(currentFood.getDetails());
        if (currentFood.getImageUrls() != null && !currentFood.getImageUrls().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(currentFood.getImageUrls().get(0))
                    .into(holder.imgFood);
        }
        holder.itemView.setOnClickListener(v -> {
            if (currentFood.getId() == null || currentFood.getId().isEmpty()) {
                Toast.makeText(holder.itemView.getContext(), "Không tìm thấy ID món ăn!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(holder.itemView.getContext(), DetailActivity.class);
            intent.putExtra("foodId", currentFood.getId());
            intent.putExtra("foodName", currentFood.getName());
            intent.putExtra("foodPrice", currentFood.getPrice());
            intent.putExtra("foodDescription", currentFood.getDetails());
            if (currentFood.getImageUrls() != null && !currentFood.getImageUrls().isEmpty()) {
                intent.putExtra("foodImagePath", currentFood.getImageUrls().get(0));
            } else {
                intent.putExtra("foodImagePath", "");
            }
            intent.putExtra("foodRating", currentFood.getRating());
            intent.putExtra("foodCategory", currentFood.getCategory());
            intent.putExtra("foodIngredients", currentFood.getIngredients());
            if (currentFood.getRecipe() != null) {
                intent.putExtra("foodRecipe", currentFood.getRecipe());
            } else {
                intent.putExtra("foodRecipe", "Công thức đang được cập nhật...");
            }
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFood;
        TextView tvName;
        TextView tvPrice;
        TextView tvCategory;
        TextView tvRating;
        TextView tvReviewCount;
        TextView tvDescription;
        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.foodImageView);
            tvName = itemView.findViewById(R.id.foodNameTextView);
            tvPrice = itemView.findViewById(R.id.priceTextView);
            tvCategory = null;
            try { tvCategory = itemView.findViewById(R.id.foodCategoryTextView); } catch (Exception ignored) {}
            tvRating = itemView.findViewById(R.id.ratingTextView);
            tvReviewCount = itemView.findViewById(R.id.reviewCountTextView);
            tvDescription = itemView.findViewById(R.id.descriptionTextView);
        }
    }
} 