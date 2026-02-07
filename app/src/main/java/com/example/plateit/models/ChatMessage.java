package com.example.plateit.models;

public class ChatMessage {
    private String message;
    private boolean isUser;
    private android.net.Uri imageUri; // Optional image

    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
    }

    public ChatMessage(String message, boolean isUser, android.net.Uri imageUri) {
        this.message = message;
        this.isUser = isUser;
        this.imageUri = imageUri;
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
}
