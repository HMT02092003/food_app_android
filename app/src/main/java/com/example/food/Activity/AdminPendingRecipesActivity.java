package com.example.food.Activity;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.food.Adapter.PendingRecipesAdapter;
import com.example.food.Model.PendingRecipe;
import com.example.food.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class AdminPendingRecipesActivity extends AppCompatActivity implements PendingRecipesAdapter.OnApproveClickListener {
    private RecyclerView recyclerView;
    private PendingRecipesAdapter adapter;
    private List<PendingRecipe> recipeList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_pending_recipes);

        recyclerView = findViewById(R.id.recyclerViewPending);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PendingRecipesAdapter(recipeList, this);
        recyclerView.setAdapter(adapter);

        loadPendingRecipes();
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
            });
    }

    @Override
    public void onApprove(PendingRecipe recipe) {
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
                    });
            });
    }
} 