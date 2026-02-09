package com.example.plateit.responses;

public class RecipeSummary {
    private int id;
    private String title;
    private String image;
    private int usedIngredientCount;
    private int missedIngredientCount;
    private int likes;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getImage() {
        return image;
    }

    public int getUsedIngredientCount() {
        return usedIngredientCount;
    }

    public int getMissedIngredientCount() {
        return missedIngredientCount;
    }

    public int getLikes() {
        return likes;
    }
}
