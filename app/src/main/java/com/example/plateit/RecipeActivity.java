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
        androidx.cardview.widget.CardView cvSourceCard = findViewById(R.id.cvSourceCard);
        android.widget.ImageView imgSourceThumbnail = findViewById(R.id.imgSourceThumbnail);
        android.view.View btnWatchSource = findViewById(R.id.btnWatchSource);
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
                        finalRecipe.getIngredients(),
                        finalRecipe.getSourceUrl(),
                        finalRecipe.getSourceImage());

                // Pass as JSON
                String jsonModel = new com.google.gson.Gson().toJson(recipeModel);
                intent.putExtra("recipe_json", jsonModel);

                try {
                    startActivity(intent);
                } catch (Exception e) {
                    android.util.Log.e("RecipeActivity", "Error starting CookingMode: " + e.getMessage());
                    android.widget.Toast.makeText(this, "Error starting cooking mode: " + e.getMessage(),
                            android.widget.Toast.LENGTH_LONG).show();
                }
            } else {
                android.widget.Toast.makeText(this, "No steps available!", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnChooseAnother).setOnClickListener(v -> finish());

        // Check for null recipe and log
        if (recipe != null) {
            // Toast for success debugging
            // android.widget.Toast.makeText(this, "Recipe Loaded: " + recipe.getName(),
            // android.widget.Toast.LENGTH_LONG)
            // .show();

            // Header
            if (getSupportActionBar() != null)
                getSupportActionBar().hide();
            tvTitle.setText(recipe.getName());

            tvTime.setText(recipe.getTotalTime() != null ? recipe.getTotalTime() : "N/A");

            // Populate Source Card
            String sourceUrl = recipe.getSourceUrl();
            String sourceImage = recipe.getSourceImage();

            if (sourceUrl != null && !sourceUrl.isEmpty() && sourceUrl.startsWith("http")) {
                cvSourceCard.setVisibility(android.view.View.VISIBLE);

                if (sourceImage != null && !sourceImage.isEmpty()) {
                    com.squareup.picasso.Picasso.get()
                            .load(sourceImage)
                            .placeholder(R.drawable.ic_launcher_background)
                            .error(R.drawable.ic_launcher_background)
                            .centerCrop()
                            .fit()
                            .into(imgSourceThumbnail);
                } else {
                    imgSourceThumbnail.setImageResource(R.drawable.ic_launcher_background);
                }

                // Click listener
                android.view.View.OnClickListener openUrl = v -> {
                    android.content.Intent browserIntent = new android.content.Intent(
                            android.content.Intent.ACTION_VIEW, android.net.Uri.parse(sourceUrl));
                    startActivity(browserIntent);
                };

                cvSourceCard.setOnClickListener(openUrl);
                if (btnWatchSource != null) {
                    btnWatchSource.setOnClickListener(openUrl);
                }
            } else {
                cvSourceCard.setVisibility(android.view.View.GONE);
            }

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
