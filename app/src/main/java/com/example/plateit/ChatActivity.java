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
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
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

        // Reverse layout manager so newest messages are at the bottom but we fill from
        // bottom?
        // Actually, standardized chat view: StackFromEnd = true
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // layoutManager.setReverseLayout(true); // If we want to align to bottom like
        // standard chat
        // However, I set scaleY=-1 in XML, which is a trick for inverted lists.
        // Let's stick to standard StackFromEnd for less confusion with XML scaleY
        // unless I want to keep that trick.
        // The XML had scaleY="-1" on RecyclerView and items. This is a common trick to
        // keep scroll at bottom.
        // Let's use that since I wrote it in XML.
        layoutManager.setReverseLayout(false);
        // data 0 is bottom.

        // Wait, if I use scaleY=-1, then the list is visually inverted.
        // Index 0 appears at the bottom.
        // So I should add new messages to index 0.

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

        // Create User Message
        // Since we are using the scaleY=-1 trick, the 0th item is at the BOTTOM.
        // So we should add to index 0.
        ChatMessage userMsg = new ChatMessage(text, true, pendingImageUri);
        messageList.add(0, userMsg);
        chatAdapter.notifyItemInserted(0);
        rvChatMessages.scrollToPosition(0);

        // Reset inputs
        etChatMessage.setText("");
        pendingImageUri = null;
        btnAttachImage.setColorFilter(getColor(R.color.gray_600)); // Reset color

        // Mock AI Response
        // In real app, this would be an API call
        new android.os.Handler().postDelayed(() -> {
            ChatMessage aiMsg = new ChatMessage("That looks delicious! Here's a recipe for it...", false);
            messageList.add(0, aiMsg);
            chatAdapter.notifyItemInserted(0);
            rvChatMessages.scrollToPosition(0);
        }, 1500);
    }
}
