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
import com.example.food.Domain.Foods; // Đảm bảo import đúng lớp Foods của bạn
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
        // Đảm bảo bạn đang sử dụng đúng layout cho mỗi item trong RecyclerView
        // Dựa vào log stack trace trước đó, bạn đang sử dụng R.layout.viewholder_list_food
        View inflate = LayoutInflater.from(context).inflate(R.layout.viewholder_list_food, parent, false);
        return new viewholder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodListAdapter.viewholder holder, int position) {
        Foods food = items.get(position);

        // Đặt văn bản cho từng trường, ánh xạ từ đối tượng Foods
        holder.titleTxt.setText(food.getTitle());

        // Định dạng giá tiền VNĐ
        holder.priceTxt.setText(String.format("%,.0f VNĐ", food.getPrice()));

        // Xử lý hiển thị đánh giá (rating)
        double rating = food.getStar();
        // Đảm bảo giá trị rating được hiển thị đúng, ví dụ: 0.0 nếu chưa có.
        // Bạn đã xử lý đặt 0.0 trong ListFoodsActivity, nên ở đây chỉ cần định dạng.
        holder.rateTxt.setText(String.format("%.1f", rating));

        // Tải ảnh bằng Glide
        String imagePath = food.getImagePath(); // Lấy đường dẫn ảnh
        if (imagePath != null && !imagePath.isEmpty()) { // Kiểm tra null và rỗng
            Glide.with(context)
                    .load(imagePath) // Tải ảnh trực tiếp từ URL
                    .transform(new CenterCrop(), new RoundedCorners(30))
                    .placeholder(R.drawable.food_placeholder) // Ảnh hiển thị khi đang tải
                    .error(R.drawable.food_placeholder) // Ảnh hiển thị khi có lỗi tải
                    .into(holder.pic);
        } else {
            // Nếu ImagePath là null hoặc rỗng, tải ảnh placeholder
            Glide.with(context)
                    .load(R.drawable.food_placeholder) // Tải ảnh placeholder mặc định
                    .transform(new CenterCrop(), new RoundedCorners(30))
                    .into(holder.pic);
        }

        // Thêm trình lắng nghe sự kiện click để mở activity chi tiết
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            // Truyền các dữ liệu cần thiết của món ăn sang DetailActivity
            intent.putExtra("foodId", food.getId()); // ID đã là String sau khi sửa Foods.java
            intent.putExtra("foodTitle", food.getTitle());
            intent.putExtra("foodPrice", food.getPrice());
            intent.putExtra("foodStar", food.getStar());
            intent.putExtra("foodImagePath", food.getImagePath());
            intent.putExtra("foodDescription", food.getDescription());
            intent.putExtra("foodCategoryId", food.getCategoryId());

            // QUAN TRỌNG: Lớp Foods của bạn hiện KHÔNG CÓ thuộc tính 'recipe'.
            // Nếu bạn muốn truyền 'recipe', bạn PHẢI thêm
            // `private String recipe;` và các phương thức `getRecipe()` / `setRecipe()`
            // vào lớp `Foods.java` trước.
            // Nếu không cần, hãy xóa hoặc giữ nguyên dòng bình luận này.
            // intent.putExtra("foodRecipe", food.getRecipe());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class viewholder extends RecyclerView.ViewHolder {
        TextView titleTxt, priceTxt, rateTxt;
        ImageView pic;

        public viewholder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các thành phần UI trong layout của item
            titleTxt = itemView.findViewById(R.id.titleTxt);
            priceTxt = itemView.findViewById(R.id.priceTxt);
            rateTxt = itemView.findViewById(R.id.rateTxt); // Đảm bảo ID này tồn tại trong viewholder_list_food.xml
            pic = itemView.findViewById(R.id.pic);
        }
    }
}