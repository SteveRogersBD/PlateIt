package com.example.plateit.models;

import java.io.Serializable;
import java.util.List;

public class Recipe implements Serializable {
    private String name;
    private List<RecipeStep> steps;
    private List<Ingredient> ingredients;
    private String source;
    private String source_image;

    public Recipe(String name, List<RecipeStep> steps, List<Ingredient> ingredients) {
        this.name = name;
        this.steps = steps;
        this.ingredients = ingredients;
    }

    public Recipe(String name, List<RecipeStep> steps, List<Ingredient> ingredients, String source) {
        this.name = name;
        this.steps = steps;
        this.ingredients = ingredients;
        this.source = source;
    }

    public Recipe(String name, List<RecipeStep> steps, List<Ingredient> ingredients, String source,
            String source_image) {
        this.name = name;
        this.steps = steps;
        this.ingredients = ingredients;
        this.source = source;
        this.source_image = source_image;
    }

    public String getName() {
        return name;
    }

    public List<RecipeStep> getSteps() {
        return steps;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public String getSource() {
        return source;
    }

    public String getSourceImage() {
        return source_image;
    }
}
