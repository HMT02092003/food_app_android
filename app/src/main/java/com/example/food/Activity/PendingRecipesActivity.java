package com.example.food.Activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.food.Adapter.PendingRecipesAdapter;
import com.example.food.Model.PendingRecipe;
import com.example.food.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class PendingRecipesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PendingRecipesAdapter adapter;
    private List<PendingRecipe> recipeList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_pending_recipes);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerViewPending);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PendingRecipesAdapter(recipeList, this::approveRecipe);
        recyclerView.setAdapter(adapter);

        loadPendingRecipes();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadPendingRecipes() {
        db.collection("PendingRecipes").get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                recipeList.clear();
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
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

    private void approveRecipe(PendingRecipe recipe) {
        // Thêm vào Foods
        db.collection("Foods").document(recipe.getId())
            .set(recipe)
            .addOnSuccessListener(aVoid -> {
                // Xóa khỏi PendingRecipes
                db.collection("PendingRecipes").document(recipe.getId())
                    .delete()
                    .addOnSuccessListener(aVoid1 -> {
                        Toast.makeText(this, "Đã duyệt món ăn!", Toast.LENGTH_SHORT).show();
                        loadPendingRecipes();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi xóa món ăn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi khi thêm món ăn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
} 