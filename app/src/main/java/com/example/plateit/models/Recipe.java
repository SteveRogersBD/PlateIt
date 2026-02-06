package com.example.plateit.models;

import java.io.Serializable;
import java.util.List;

public class Recipe implements Serializable {
    private String name;
    private List<String> steps;
    private List<Ingredient> ingredients;

    public Recipe(String name, List<String> steps, List<Ingredient> ingredients) {
        this.name = name;
        this.steps = steps;
        this.ingredients = ingredients;
    }

    public String getName() {
        return name;
    }

    public List<String> getSteps() {
        return steps;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }
}
