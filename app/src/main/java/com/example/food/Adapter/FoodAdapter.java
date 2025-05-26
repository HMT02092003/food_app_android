package com.example.food.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log; // Thêm import cho Log
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food.Activity.AdminUpdateFoodActivity;
import com.example.food.Activity.DetailActivity; // Thêm import cho DetailActivity
import com.example.food.Model.FoodModel;
import com.example.food.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.List;
import java.util.ArrayList; // Import ArrayList

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private List<FoodModel> foodList;
    private OnFoodDeletedListener onFoodDeletedListener;

    public interface OnFoodDeletedListener {
        void onFoodDeleted();
    }

    public FoodAdapter(List<FoodModel> foodList) {
        this.foodList = foodList;
    }

    public void setOnFoodDeletedListener(OnFoodDeletedListener listener) {
        this.onFoodDeletedListener = listener;
    }

    public void updateData(List<FoodModel> newFoodList) {
        this.foodList = newFoodList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food_screen, parent, false);
        return new FoodViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodModel currentFood = foodList.get(position);
        holder.tvName.setText(currentFood.getName());
        holder.tvCategory.setText(currentFood.getCategory());
        holder.tvPrice.setText(String.format("%,.0f VNĐ", currentFood.getPrice()));

        if (currentFood.getImageUrls() != null && !currentFood.getImageUrls().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(currentFood.getImageUrls().get(0)) // Lấy ảnh đầu tiên trong danh sách
                    .into(holder.imgFood);
        }

        // Thiết lập sự kiện cho nút xóa
        holder.imgDelete.setOnClickListener(v -> {
            showDeleteConfirmationDialog(holder.itemView.getContext(), currentFood);
        });

        // Thiết lập sự kiện cho nút sửa
        holder.imgEdit.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), AdminUpdateFoodActivity.class);
            // Truyền dữ liệu FoodModel cho Activity AdminUpdateFoodActivity
            Bundle bundle = new Bundle();
            bundle.putString("FOOD_ID", currentFood.getId());
            bundle.putString("FOOD_NAME", currentFood.getName());
            bundle.putDouble("FOOD_PRICE", currentFood.getPrice());
            bundle.putString("FOOD_INGREDIENTS", currentFood.getIngredients());
            bundle.putString("FOOD_DETAILS", currentFood.getDetails());

            // Đảm bảo truyền FOOD_RECIPE
            bundle.putString("FOOD_RECIPE", currentFood.getRecipe()); // THÊM DÒNG NÀY

            bundle.putStringArrayList("FOOD_IMAGE_URLS", (ArrayList<String>) currentFood.getImageUrls());
            bundle.putString("FOOD_CATEGORY", currentFood.getCategory());
            intent.putExtras(bundle);
            holder.itemView.getContext().startActivity(intent);
        });

        // THÊM LISTENER CHO TOÀN BỘ ITEM ĐỂ MỞ DETAILACTIVITY
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), DetailActivity.class);
            intent.putExtra("foodId", currentFood.getId());
            intent.putExtra("foodName", currentFood.getName());
            intent.putExtra("foodPrice", currentFood.getPrice());
            intent.putExtra("foodDescription", currentFood.getDetails()); // 'details' trong FoodModel tương ứng với 'descriptionTxt'

            if (currentFood.getImageUrls() != null && !currentFood.getImageUrls().isEmpty()) {
                intent.putExtra("foodImagePath", currentFood.getImageUrls().get(0));
            } else {
                intent.putExtra("foodImagePath", "");
            }

            intent.putExtra("foodRating", currentFood.getRating());
            intent.putExtra("foodCategory", currentFood.getCategory());
            intent.putExtra("foodIngredients", currentFood.getIngredients());

            // DÒNG QUAN TRỌNG NHẤT: TRUYỀN DỮ LIỆU CÔNG THỨC VỚI KEY "foodRecipe"
            if (currentFood.getRecipe() != null) {
                intent.putExtra("foodRecipe", currentFood.getRecipe());
            } else {
                Log.e("FoodAdapter", "Food recipe is null for food: " + currentFood.getName());
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
        TextView tvCategory;
        TextView tvPrice;
        ImageView imgDelete;
        ImageView imgEdit;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.imgFood);
            tvName = itemView.findViewById(R.id.tvName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            imgDelete = itemView.findViewById(R.id.imgDelete);
            imgEdit = itemView.findViewById(R.id.imgEdit);
            // Không cần findViewById cho recipeContentTxt ở đây vì nó ở DetailActivity
        }
    }

    private void showDeleteConfirmationDialog(Context context, FoodModel food) {
        new AlertDialog.Builder(context)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa món " + food.getName() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteFood(context, food);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteFood(Context context, FoodModel food) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Foods").document(food.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Xóa ảnh (nếu là đường dẫn trực tiếp)
                    // Lưu ý: Việc xóa ảnh từ URL trực tiếp như thế này chỉ có thể thực hiện nếu đó là file trên thiết bị,
                    // không phải URL từ internet (như Firebase Storage).
                    // Nếu ảnh được lưu trên Firebase Storage, bạn cần sử dụng Firebase Storage SDK để xóa.
                    if (food.getImageUrls() != null && !food.getImageUrls().isEmpty()) {
                        for (String imageUrl : food.getImageUrls()) {
                            // Kiểm tra nếu URL là một đường dẫn file cục bộ
                            if (imageUrl != null && !imageUrl.isEmpty() && imageUrl.startsWith("file://")) {
                                File fileToDelete = new File(Uri.parse(imageUrl).getPath());
                                if (fileToDelete.exists()) {
                                    if (fileToDelete.delete()) {
                                        Log.d("FoodAdapter", "Deleted local file: " + fileToDelete.getAbsolutePath());
                                    } else {
                                        Log.e("FoodAdapter", "Failed to delete local file: " + fileToDelete.getAbsolutePath());
                                    }
                                } else {
                                    Log.d("FoodAdapter", "Local file not found: " + fileToDelete.getAbsolutePath());
                                }
                            }
                            // TODO: Nếu ảnh trên Firebase Storage, thêm logic xóa tại đây
                            // Ví dụ: FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl).delete();
                        }
                    }

                    Toast.makeText(context, "Món ăn đã được xóa thành công", Toast.LENGTH_SHORT).show();

                    // Gọi callback để thông báo cho Activity cập nhật lại danh sách
                    if (onFoodDeletedListener != null) {
                        onFoodDeletedListener.onFoodDeleted();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Lỗi khi xóa món ăn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FoodAdapter", "Error deleting food: " + e.getMessage(), e); // Log lỗi chi tiết
                });
    }
}