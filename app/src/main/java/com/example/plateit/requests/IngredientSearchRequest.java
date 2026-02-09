package com.example.plateit.requests;

import java.util.List;

public class IngredientSearchRequest {
    private List<String> ingredients;
    private int number;

    public IngredientSearchRequest(List<String> ingredients, int number) {
        this.ingredients = ingredients;
        this.number = number;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public int getNumber() {
        return number;
    }
}
