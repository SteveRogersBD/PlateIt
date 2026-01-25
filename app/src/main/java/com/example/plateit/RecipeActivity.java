package com.example.plateit;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RecipeActivity extends AppCompatActivity {

    private TextView ingredientsTextView;
    private TextView stepsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        ingredientsTextView = findViewById(R.id.ingredients_text_view);
        stepsTextView = findViewById(R.id.steps_text_view);

        // TODO: Get the extracted recipe data and display it
        String ingredients = getIntent().getStringExtra("ingredients");
        String steps = getIntent().getStringExtra("steps");

        ingredientsTextView.setText(ingredients);
        stepsTextView.setText(steps);
    }
}
