package com.example.plateit.requests;

import com.example.plateit.models.Recipe;

public class ChatRequest {
    private String message;
    private String thread_id;
    private Recipe recipe;
    private int current_step;
    private String image_data;

    public ChatRequest(String message, String thread_id, Recipe recipe, int current_step, String image_data) {
        this.message = message;
        this.thread_id = thread_id;
        this.recipe = recipe;
        this.current_step = current_step;
        this.image_data = image_data;
    }
}
