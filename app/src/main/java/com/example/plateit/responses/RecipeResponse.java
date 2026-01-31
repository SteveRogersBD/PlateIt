package com.example.plateit.responses;

import java.io.Serializable;
import java.util.List;

public class RecipeResponse implements Serializable {
    private String name;
    private List<String> steps;
    private List<com.example.plateit.models.Ingredient> ingredients;
    private String total_time;

    // Getters
    public String getName() {
        return name;
    }

    public List<String> getSteps() {
        return steps;
    }

    public List<com.example.plateit.models.Ingredient> getIngredients() {
        return ingredients;
    }

    public String getTotalTime() {
        return total_time;
    }
}