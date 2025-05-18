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
        holder.categoryNameTextView.setText(category); // Resolved: Cannot resolve symbol 'categoryNameTextView'

        // Highlight the selected category
        if (position == selectedPosition) {
            holder.categoryNameTextView.setBackgroundResource(R.drawable.category_background_selected); // Resolved: Cannot resolve symbol 'category_background_selected'
            holder.categoryNameTextView.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.white));
        } else {
            holder.categoryNameTextView.setBackgroundResource(R.drawable.category_background_default); // Resolved: Cannot resolve symbol 'category_background_default'
            holder.categoryNameTextView.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.black));
        }

        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition(); // Correct way to get the current position
            if (adapterPosition != RecyclerView.NO_POSITION && selectedPosition != adapterPosition) { // Check if the item is still valid
                int previousPosition = selectedPosition;
                selectedPosition = adapterPosition;
                notifyItemChanged(previousPosition);
                notifyItemChanged(selectedPosition);
                listener.onCategoryClick(categoryList.get(adapterPosition)); // Use the adapter position to get the correct category
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