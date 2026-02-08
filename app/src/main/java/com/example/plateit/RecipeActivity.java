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

        com.example.plateit.responses.RecipeResponse recipe = null;
        try {
            String json = getIntent().getStringExtra("recipe_json");
            if (json != null) {
                recipe = new com.google.gson.Gson().fromJson(json, com.example.plateit.responses.RecipeResponse.class);
            }
        } catch (Exception e) {
            android.util.Log.e("PlateIt", "RecipeActivity: JSON Parse Error", e);
            android.widget.Toast.makeText(this, "Error parsing recipe JSON", android.widget.Toast.LENGTH_LONG).show();
        }

        // Buttons
        final com.example.plateit.responses.RecipeResponse finalRecipe = recipe;
        findViewById(R.id.btnStartCooking).setOnClickListener(v -> {
            if (finalRecipe != null && finalRecipe.getSteps() != null) {
                android.content.Intent intent = new android.content.Intent(this, CookingModeActivity.class);
                // intent.putStringArrayListExtra("steps_list", new
                // java.util.ArrayList<>(recipe.getSteps())); // Removed - using object passing

                // Convert Response to Model for Intent passing
                com.example.plateit.models.Recipe recipeModel = new com.example.plateit.models.Recipe(
                        finalRecipe.getName(),
                        finalRecipe.getSteps(), // Now List<RecipeStep>
                        finalRecipe.getIngredients());

                // Pass as JSON
                String jsonModel = new com.google.gson.Gson().toJson(recipeModel);
                intent.putExtra("recipe_json", jsonModel);

                startActivity(intent);
            } else {
                android.widget.Toast.makeText(this, "No steps available!", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnChooseAnother).setOnClickListener(v -> finish());

        // Check for null recipe and log
        if (recipe != null) {
            // Toast for success debugging
            android.widget.Toast.makeText(this, "Recipe Loaded: " + recipe.getName(), android.widget.Toast.LENGTH_LONG)
                    .show();

            // Header
            if (getSupportActionBar() != null)
                getSupportActionBar().hide();
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
        } else {
            android.util.Log.e("PlateIt", "RecipeActivity: Recipe is NULL!");
            android.widget.Toast.makeText(this, "Error: Could not load recipe data", android.widget.Toast.LENGTH_LONG)
                    .show();
            finish();
        }
    }
}
