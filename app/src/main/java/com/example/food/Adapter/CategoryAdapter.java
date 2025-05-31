package com.example.food.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.R;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private final List<String> categoryList;
    private final OnCategoryClickListener listener;
    private int selectedPosition = 0; // To highlight the selected category

    public interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }

    public CategoryAdapter(List<String> categoryList, OnCategoryClickListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false); // Resolved: Cannot resolve symbol 'item_category'
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String category = categoryList.get(position);
        holder.categoryNameTextView.setText(category);

        // Highlight the selected category
        boolean isSelected = position == selectedPosition;
        if (isSelected) {
            holder.categoryNameTextView.setBackgroundResource(R.drawable.category_background_selected);
            holder.categoryNameTextView.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.white));
            holder.categoryNameTextView.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            holder.categoryNameTextView.setBackgroundResource(R.drawable.category_background_default);
            holder.categoryNameTextView.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.black));
            holder.categoryNameTextView.setTypeface(null, android.graphics.Typeface.NORMAL);
        }

        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && selectedPosition != adapterPosition) {
                int previousPosition = selectedPosition;
                selectedPosition = adapterPosition;
                notifyItemChanged(previousPosition);
                notifyItemChanged(selectedPosition);
                listener.onCategoryClick(categoryList.get(adapterPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryNameTextView = itemView.findViewById(R.id.categoryNameTextView); // Resolved: Cannot resolve symbol 'categoryNameTextView'
        }
    }
}