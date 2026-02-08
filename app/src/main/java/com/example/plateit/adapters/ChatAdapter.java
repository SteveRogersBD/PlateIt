package com.example.plateit.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.plateit.R;
import com.example.plateit.models.ChatMessage;
import com.example.plateit.VideoAdapter;
import com.example.plateit.RecipeVideo;

import java.util.List;
import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_USER = 0;
    private static final int TYPE_AI = 1;

    private List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser() ? TYPE_USER : TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_ai, parent, false);
            return new AIViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).bind(message);
        } else {
            ((AIViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        ImageView imgAttached;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            imgAttached = itemView.findViewById(R.id.imgAttached);
        }

        void bind(ChatMessage message) {
            tvMessage.setText(message.getMessage());
            if (message.getImageUri() != null) {
                imgAttached.setVisibility(View.VISIBLE);
                imgAttached.setImageURI(message.getImageUri());
            } else {
                imgAttached.setVisibility(View.GONE);
            }
        }
    }

    static class AIViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        RecyclerView rvRecipeList, rvIngredientList, rvVideoList;

        public AIViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            rvRecipeList = itemView.findViewById(R.id.rvRecipeList);
            rvIngredientList = itemView.findViewById(R.id.rvIngredientList);
            rvVideoList = itemView.findViewById(R.id.rvVideoList);
        }

        void bind(ChatMessage message) {
            tvMessage.setText(message.getMessage());

            // Reset Visibility
            rvRecipeList.setVisibility(View.GONE);
            rvIngredientList.setVisibility(View.GONE);
            rvVideoList.setVisibility(View.GONE);

            String type = message.getUiType();

            if ("recipe_list".equals(type) && message.getRecipeData() != null) {
                rvRecipeList.setVisibility(View.VISIBLE);
                rvRecipeList.setLayoutManager(
                        new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
                rvRecipeList.setAdapter(new RecipeCardAdapter(message.getRecipeData().getItems(), recipe -> {
                    showRecipePreviewSheet(itemView.getContext(), recipe);
                }));

            } else if ("ingredient_list".equals(type) && message.getIngredientData() != null) {
                rvIngredientList.setVisibility(View.VISIBLE);
                rvIngredientList.setLayoutManager(new GridLayoutManager(itemView.getContext(), 2));

                List<com.example.plateit.models.Ingredient> converted = new ArrayList<>();
                for (com.example.plateit.responses.ChatResponse.IngredientItem item : message.getIngredientData()
                        .getItems()) {
                    converted.add(new com.example.plateit.models.Ingredient(
                            item.getName(), item.getAmount(), item.getImage()));
                }
                rvIngredientList.setAdapter(new IngredientsAdapter(converted));

            } else if ("video_list".equals(type) && message.getVideoData() != null) {
                rvVideoList.setVisibility(View.VISIBLE);
                rvVideoList.setLayoutManager(
                        new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));

                List<RecipeVideo> converted = new ArrayList<>();
                for (com.example.plateit.responses.ChatResponse.VideoItem item : message.getVideoData().getItems()) {
                    converted.add(new RecipeVideo(
                            item.getTitle(), item.getUrl(), item.getThumbnail(), "", "", ""));
                }
                rvVideoList.setAdapter(new VideoAdapter(converted, video -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(video.getLink()));
                    itemView.getContext().startActivity(intent);
                }));
            }
        }

        private void showRecipePreviewSheet(android.content.Context context,
                com.example.plateit.responses.ChatResponse.RecipeCard recipe) {
            com.google.android.material.bottomsheet.BottomSheetDialog sheet = new com.google.android.material.bottomsheet.BottomSheetDialog(
                    context);
            View view = android.view.LayoutInflater.from(context)
                    .inflate(com.example.plateit.R.layout.bottom_sheet_recipe_preview, null);
            sheet.setContentView(view);

            // Populate views
            android.widget.ImageView imgPreview = view.findViewById(com.example.plateit.R.id.imgRecipePreview);
            android.widget.TextView tvTitle = view.findViewById(com.example.plateit.R.id.tvRecipeTitle);
            android.widget.TextView tvTime = view.findViewById(com.example.plateit.R.id.tvReadyTime);
            com.google.android.material.button.MaterialButton btnViewFull = view
                    .findViewById(com.example.plateit.R.id.btnViewFullRecipe);
            com.google.android.material.button.MaterialButton btnStartCooking = view
                    .findViewById(com.example.plateit.R.id.btnStartCooking);

            tvTitle.setText(recipe.getTitle());
            if (recipe.getReadyInMinutes() != null) {
                tvTime.setText("Ready in " + recipe.getReadyInMinutes() + " mins");
            } else {
                tvTime.setVisibility(View.GONE);
            }

            if (recipe.getImageUrl() != null) {
                com.squareup.picasso.Picasso.get().load(recipe.getImageUrl()).into(imgPreview);
            }

            // View Full Recipe button
            btnViewFull.setOnClickListener(v -> {
                if (recipe.getSourceUrl() != null && !recipe.getSourceUrl().isEmpty()) {
                    android.content.Intent browserIntent = new android.content.Intent(
                            android.content.Intent.ACTION_VIEW, android.net.Uri.parse(recipe.getSourceUrl()));
                    context.startActivity(browserIntent);
                    sheet.dismiss();
                } else {
                    android.widget.Toast
                            .makeText(context, "Source URL not available", android.widget.Toast.LENGTH_SHORT).show();
                }
            });

            // Start Cooking button
            btnStartCooking.setOnClickListener(v -> {
                sheet.dismiss();
                fetchAndStartRecipe(context, recipe.getId());
            });

            sheet.show();
        }

        private void fetchAndStartRecipe(android.content.Context context, int recipeId) {
            android.widget.Toast.makeText(context, "Loading recipe...", android.widget.Toast.LENGTH_SHORT).show();

            com.example.plateit.api.RetrofitClient.getAgentService().getRecipeDetails(recipeId)
                    .enqueue(new retrofit2.Callback<com.example.plateit.responses.RecipeResponse>() {
                        @Override
                        public void onResponse(retrofit2.Call<com.example.plateit.responses.RecipeResponse> call,
                                retrofit2.Response<com.example.plateit.responses.RecipeResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                com.example.plateit.responses.RecipeResponse recipeResp = response.body();

                                // Convert to Recipe model
                                com.example.plateit.models.Recipe recipe = new com.example.plateit.models.Recipe(
                                        recipeResp.getName(),
                                        recipeResp.getSteps(),
                                        recipeResp.getIngredients(),
                                        recipeResp.getSourceUrl(),
                                        recipeResp.getSourceImage());

                                // Start CookingModeActivity
                                android.content.Intent intent = new android.content.Intent(context,
                                        com.example.plateit.CookingModeActivity.class);
                                intent.putExtra("recipe_object", recipe);
                                context.startActivity(intent);
                            } else {
                                android.widget.Toast
                                        .makeText(context, "Failed to load recipe", android.widget.Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<com.example.plateit.responses.RecipeResponse> call,
                                Throwable t) {
                            android.widget.Toast
                                    .makeText(context, "Error: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
        }
    }
}
