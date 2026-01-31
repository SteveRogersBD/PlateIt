package com.example.plateit;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RecipeActivity extends AppCompatActivity {

    private TextView ingredientsTextView;
    private TextView stepsTextView;
    // private TextView ingredientsTextView; // Removed
    // private TextView stepsTextView; // Removed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        android.widget.TextView tvTitle = findViewById(R.id.tvRecipeTitle);
        android.widget.TextView tvTime = findViewById(R.id.tvRecipeTime);
        androidx.recyclerview.widget.RecyclerView rvIngredients = findViewById(R.id.rvIngredients);
        androidx.recyclerview.widget.RecyclerView rvSteps = findViewById(R.id.rvSteps);

        // Buttons
        findViewById(R.id.btnStartCooking).setOnClickListener(v -> {
            android.widget.Toast.makeText(this, "Cooking Mode Started!", android.widget.Toast.LENGTH_SHORT).show();
            // Future: Navigate to immersive step-by-step view
        });

        findViewById(R.id.btnChooseAnother).setOnClickListener(v -> finish());

        com.example.plateit.responses.RecipeResponse recipe = (com.example.plateit.responses.RecipeResponse) getIntent()
                .getSerializableExtra("recipe_data");

        if (recipe != null) {
            // Header
            if (getSupportActionBar() != null)
                getSupportActionBar().hide(); // Hide default toolbar for cleaner look
            tvTitle.setText(recipe.getName());
            tvTime.setText(recipe.getTotalTime() != null ? recipe.getTotalTime() : "N/A");

            // Ingredients (Horizontal)
            rvIngredients.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this,
                    androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
            com.example.plateit.adapters.IngredientsAdapter ingredientsAdapter = new com.example.plateit.adapters.IngredientsAdapter(
                    recipe.getIngredients());
            rvIngredients.setAdapter(ingredientsAdapter);

            // Steps (Vertical)
            rvSteps.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
            com.example.plateit.adapters.StepsAdapter stepsAdapter = new com.example.plateit.adapters.StepsAdapter(
                    recipe.getSteps());
            rvSteps.setAdapter(stepsAdapter);
        }
    }
}
