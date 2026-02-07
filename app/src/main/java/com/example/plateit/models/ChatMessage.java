package com.example.plateit.models;

import com.example.plateit.responses.ChatResponse;

public class ChatMessage {
    private String message;
    private boolean isUser;
    private android.net.Uri imageUri; // Optional user image

    // Dynamic UI Data (from Agent)
    private String uiType = "none";
    private ChatResponse.RecipeListPayload recipeData;
    private ChatResponse.IngredientListPayload ingredientData;
    private ChatResponse.VideoListPayload videoData;

    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
    }

    public ChatMessage(String message, boolean isUser, android.net.Uri imageUri) {
        this(message, isUser);
        this.imageUri = imageUri;
    }

    // Constructor for AI response with dynamic data
    public ChatMessage(String message, String uiType,
            ChatResponse.RecipeListPayload recipeData,
            ChatResponse.IngredientListPayload ingredientData,
            ChatResponse.VideoListPayload videoData) {
        this.message = message;
        this.isUser = false;
        this.uiType = uiType;
        this.recipeData = recipeData;
        this.ingredientData = ingredientData;
        this.videoData = videoData;
    }

    public String getMessage() {
        return message;
    }

    public boolean isUser() {
        return isUser;
    }

    public android.net.Uri getImageUri() {
        return imageUri;
    }

    public String getUiType() {
        return uiType;
    }

    public ChatResponse.RecipeListPayload getRecipeData() {
        return recipeData;
    }

    public ChatResponse.IngredientListPayload getIngredientData() {
        return ingredientData;
    }

    public ChatResponse.VideoListPayload getVideoData() {
        return videoData;
    }
}
