package com.example.plateit;

import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plateit.adapters.ChatAdapter;
import com.example.plateit.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;
import java.io.InputStream;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvChatMessages;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private EditText etChatMessage;
    private ImageButton btnAttachImage;
    private ImageButton btnSendMessage;

    private Uri pendingImageUri = null;

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    pendingImageUri = uri;
                    // Provide feedback that image is selected
                    btnAttachImage.setColorFilter(getColor(R.color.app_primary));
                    Toast.makeText(this, "Image attached!", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Views
        rvChatMessages = findViewById(R.id.rvChatMessages);
        etChatMessage = findViewById(R.id.etChatMessage);
        btnAttachImage = findViewById(R.id.btnAttachImage);
        btnSendMessage = findViewById(R.id.btnSendMessage);

        // Setup RecyclerView
        messageList = new ArrayList<>();
        // Add a welcome message
        messageList.add(new ChatMessage(
                "Hello! I'm your AI Chef. Ask me anything about cooking or show me your ingredients!", false));

        // Use standard layout manager, let the adapter / reversed layout handle
        // inversion visually if needed
        // Assuming XML has scaleY=-1 for inverted effect
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        rvChatMessages.setLayoutManager(layoutManager);
        chatAdapter = new ChatAdapter(messageList);
        rvChatMessages.setAdapter(chatAdapter);

        // Actions
        btnAttachImage.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        btnSendMessage.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String text = etChatMessage.getText().toString().trim();
        if (text.isEmpty() && pendingImageUri == null) {
            return;
        }

        // 1. Create User Message UI
        ChatMessage userMsg = new ChatMessage(text, true, pendingImageUri);
        messageList.add(0, userMsg);
        chatAdapter.notifyItemInserted(0);
        rvChatMessages.scrollToPosition(0);

        // 2. Prepare Data
        String imageBase64 = null;
        if (pendingImageUri != null) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(pendingImageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                // Compress and Encode
                java.io.ByteArrayOutputStream byteArrayOutputStream = new java.io.ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                imageBase64 = android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
            }
        }

        // 3. Reset Inputs
        etChatMessage.setText("");
        pendingImageUri = null;
        btnAttachImage.setColorFilter(getColor(R.color.gray_600));

        // 4. API Call
        // Create Request (Empty recipe for general chat, step 0)
        com.example.plateit.requests.ChatRequest req = new com.example.plateit.requests.ChatRequest(
                text,
                "general_chat_thread",
                null, // No specific recipe context
                0,
                imageBase64);

        com.example.plateit.api.RetrofitClient.getAgentService().chat(req)
                .enqueue(new retrofit2.Callback<com.example.plateit.responses.ChatResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.plateit.responses.ChatResponse> call,
                            retrofit2.Response<com.example.plateit.responses.ChatResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            com.example.plateit.responses.ChatResponse resp = response.body();
                            String reply = resp.getChatBubble();

                            String uiType = resp.getUiType();

                            // Add AI Message with rich data
                            ChatMessage aiMsg = new ChatMessage(
                                    reply,
                                    uiType != null ? uiType : "none",
                                    resp.getRecipeData(),
                                    resp.getIngredientData(),
                                    resp.getVideoData());

                            messageList.add(0, aiMsg);
                            chatAdapter.notifyItemInserted(0);
                            rvChatMessages.scrollToPosition(0);

                        } else {
                            ChatMessage errorMsg = new ChatMessage(
                                    "Sorry, I'm having trouble connecting to the kitchen.", false);
                            messageList.add(0, errorMsg);
                            chatAdapter.notifyItemInserted(0);
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.plateit.responses.ChatResponse> call,
                            Throwable t) {
                        ChatMessage errorMsg = new ChatMessage("Network error: " + t.getMessage(), false);
                        messageList.add(0, errorMsg);
                        chatAdapter.notifyItemInserted(0);
                    }
                });
    }
}
