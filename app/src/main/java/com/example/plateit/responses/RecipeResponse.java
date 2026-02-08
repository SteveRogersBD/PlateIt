package com.example.plateit.responses;

import java.io.Serializable;
import java.util.List;

public class RecipeResponse implements Serializable {
    private String name;
    private List<com.example.plateit.models.RecipeStep> steps;
    private List<com.example.plateit.models.Ingredient> ingredients;
    private String total_time;
    private String source;
    private String source_image;

    // Getters
    public String getName() {
        return name;
    }

    public List<com.example.plateit.models.RecipeStep> getSteps() {
        return steps;
    }

    public List<com.example.plateit.models.Ingredient> getIngredients() {
        return ingredients;
    }

    public String getTotalTime() {
        return total_time;
    }

    public String getSourceUrl() {
        return source;
    }

    public String getSourceImage() {
        return source_image;
    }
}