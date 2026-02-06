package com.example.plateit.responses;

public class ChatResponse {
    private String chat_bubble;
    private String ui_type;
    // We can add other fields later if needed (recipe_data, etc.)

    public String getChatBubble() {
        return chat_bubble;
    }

    public String getUiType() {
        return ui_type;
    }
}
