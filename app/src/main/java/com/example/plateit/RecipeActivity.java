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

        com.example.plateit.responses.RecipeResponse recipe = (com.example.plateit.responses.RecipeResponse) getIntent()
                .getSerializableExtra("recipe_data");

        // Buttons
        // Buttons
        findViewById(R.id.btnStartCooking).setOnClickListener(v -> {
            if (recipe != null && recipe.getSteps() != null) {
                android.content.Intent intent = new android.content.Intent(this, CookingModeActivity.class);
                intent.putStringArrayListExtra("steps_list", new java.util.ArrayList<>(recipe.getSteps()));

                // Convert Response to Model for Intent passing
                com.example.plateit.models.Recipe recipeModel = new com.example.plateit.models.Recipe(
                        recipe.getName(),
                        recipe.getSteps(),
                        recipe.getIngredients());
                intent.putExtra("recipe_object", recipeModel);

                startActivity(intent);
            } else {
                android.widget.Toast.makeText(this, "No steps available!", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnChooseAnother).setOnClickListener(v -> finish());

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
