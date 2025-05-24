package com.example.food.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.R;

import java.util.List;

public class CategoryHomeAdapter extends RecyclerView.Adapter<CategoryHomeAdapter.ViewHolder> {

    private Context context;
    private List<String> categoryList;
    private OnCategoryClickListener onCategoryClickListener;
    private int selectedPosition = 0;

    public interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }

    public CategoryHomeAdapter(Context context, List<String> categoryList, OnCategoryClickListener onCategoryClickListener) {
        this.context = context;
        this.categoryList = categoryList;
        this.onCategoryClickListener = onCategoryClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_home, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String categoryName = categoryList.get(position);
        holder.categoryNameTextView.setText(categoryName);

        // Set selected state for TextView
        holder.categoryNameTextView.setSelected(position == selectedPosition);

        // Set click listener for the entire item
        holder.itemView.setOnClickListener(v -> {
            int clickedPosition = holder.getAdapterPosition();
            if (clickedPosition != RecyclerView.NO_POSITION) {
                if (clickedPosition != selectedPosition) {
                    int previousSelectedPosition = selectedPosition;
                    selectedPosition = clickedPosition;
                    notifyItemChanged(previousSelectedPosition);
                    notifyItemChanged(selectedPosition);
                }
                if (onCategoryClickListener != null) {
                    onCategoryClickListener.onCategoryClick(categoryList.get(clickedPosition));
                }
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
            categoryNameTextView = itemView.findViewById(R.id.categoryNameTextView);
        }
    }

    /**
     * Phương thức này được gọi từ HomeActivity để thiết lập category nào được chọn ban đầu.
     * Hoặc khi bạn muốn chọn một category nào đó từ bên ngoài adapter.
     * @param category Tên category cần được chọn.
     */
    public void setSelectedCategory(String category) {
        int index = categoryList.indexOf(category);
        // Chỉ cập nhật nếu category tồn tại, khác vị trí hiện tại, và không phải là NO_POSITION
        if (index != -1 && index != selectedPosition) {
            int previousSelectedPosition = selectedPosition;
            selectedPosition = index;
            // Thông báo cho RecyclerView cập nhật lại trạng thái màu
            notifyItemChanged(previousSelectedPosition);
            notifyItemChanged(selectedPosition);
        }
    }
}