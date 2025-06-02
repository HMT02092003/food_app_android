package com.example.food.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.food.Adapter.MyPendingRecipesAdapter;
import com.example.food.Model.PendingRecipe;
import com.example.food.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class MyPendingRecipesActivity extends AppCompatActivity implements MyPendingRecipesAdapter.OnRecipeActionListener {
    private RecyclerView recyclerView;
    private MyPendingRecipesAdapter adapter;
    private List<PendingRecipe> recipeList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_pending_recipes);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerViewMyPending);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyPendingRecipesAdapter(recipeList, this);
        recyclerView.setAdapter(adapter);

        loadMyPendingRecipes();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadMyPendingRecipes() {
        String currentUserId = auth.getCurrentUser().getUid();
        db.collection("PendingRecipes")
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                recipeList.clear();
                for (var doc : queryDocumentSnapshots) {
                    PendingRecipe recipe = doc.toObject(PendingRecipe.class);
                    recipe.setId(doc.getId());
                    recipeList.add(recipe);
                }
                adapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi khi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public void onEdit(PendingRecipe recipe) {
        // Chỉ cho phép sửa nếu món ăn đang ở trạng thái pending
        if (!"pending".equals(recipe.getStatus())) {
            Toast.makeText(this, "Chỉ có thể sửa món ăn đang chờ duyệt", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, UserShareRecipeActivity.class);
        intent.putExtra("recipeId", recipe.getId());
        intent.putExtra("recipeName", recipe.getName());
        intent.putExtra("recipeCategory", recipe.getCategory());
        intent.putExtra("recipePrice", recipe.getPrice());
        intent.putExtra("recipeIngredients", recipe.getIngredients());
        intent.putExtra("recipeDetails", recipe.getDetails());
        intent.putExtra("recipeImageUrl", recipe.getImageUrls().get(0));
        startActivity(intent);
    }

    @Override
    public void onDelete(PendingRecipe recipe) {
        // Chỉ cho phép xóa nếu món ăn đang ở trạng thái pending
        if (!"pending".equals(recipe.getStatus())) {
            Toast.makeText(this, "Chỉ có thể xóa món ăn đang chờ duyệt", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa món ăn này?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                db.collection("PendingRecipes")
                    .document(recipe.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Đã xóa món ăn", Toast.LENGTH_SHORT).show();
                        loadMyPendingRecipes();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi xóa món ăn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyPendingRecipes();
    }
} 