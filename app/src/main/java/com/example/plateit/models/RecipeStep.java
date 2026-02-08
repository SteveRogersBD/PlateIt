package com.example.plateit.models;

import java.io.Serializable;

public class RecipeStep implements Serializable {
    private String instruction;
    private String imageUrl;
    private String visual_query;

    // Constructors
    public RecipeStep() {
    }

    public RecipeStep(String instruction, String imageUrl, String visual_query) {
        this.instruction = instruction;
        this.imageUrl = imageUrl;
        this.visual_query = visual_query;
    }

    // Getters
    public String getInstruction() {
        return instruction;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getVisualQuery() {
        return visual_query;
    }
}
