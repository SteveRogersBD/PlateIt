package com.example.plateit;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plateit.adapters.PantryAdapter;
import com.example.plateit.api.RetrofitClient;
import com.example.plateit.db.DatabaseClient;
import com.example.plateit.db.PantryItem;
import com.example.plateit.requests.PantryScanRequest;
import com.example.plateit.responses.PantryScanResponse;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.example.plateit.R;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PantryFragment extends Fragment {

    private RecyclerView rvPantryItems;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private PantryAdapter adapter;
    private List<PantryItem> pantryTypeList = new ArrayList<>();
    private com.example.plateit.utils.SessionManager sessionManager;

    private static final int CAMERA_REQUEST_CODE = 202;

    public PantryFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pantry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvPantryItems = view.findViewById(R.id.rvPantryItems);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        ExtendedFloatingActionButton fabAdd = view.findViewById(R.id.fabAdd);
        com.google.android.material.button.MaterialButton btnCookWithPantry = view.findViewById(R.id.btnCookWithPantry);

        rvPantryItems.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PantryAdapter(pantryTypeList, item -> deleteItem(item));
        rvPantryItems.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> showAddOptions());
        btnCookWithPantry.setOnClickListener(v -> cookWithPantry());

        sessionManager = new com.example.plateit.utils.SessionManager(requireContext());
        loadPantryItems();
    }

    private void cookWithPantry() {
        if (pantryTypeList.isEmpty()) {
            Toast.makeText(getContext(), "Pantry is empty! Add items first.", Toast.LENGTH_SHORT).show();
            return;
        }
        showIngredientSelectionDialog();
    }

    private void showIngredientSelectionDialog() {
        // Prepare list of ingredient names
        String[] ingredients = new String[pantryTypeList.size()];
        boolean[] checkedItems = new boolean[pantryTypeList.size()];
        for (int i = 0; i < pantryTypeList.size(); i++) {
            ingredients[i] = pantryTypeList.get(i).name;
            checkedItems[i] = true; // Default all selected
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Select Ingredients")
                .setMultiChoiceItems(ingredients, checkedItems, (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked;
                })
                .setPositiveButton("Find Recipes", (dialog, which) -> {
                    List<String> selectedIngredients = new ArrayList<>();
                    for (int i = 0; i < checkedItems.length; i++) {
                        if (checkedItems[i]) {
                            selectedIngredients.add(ingredients[i]);
                        }
                    }
                    if (!selectedIngredients.isEmpty()) {
                        findRecipesByIngredients(selectedIngredients);
                    } else {
                        Toast.makeText(getContext(), "Select at least one ingredient", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void findRecipesByIngredients(List<String> ingredients) {
        progressBar.setVisibility(View.VISIBLE);
        com.example.plateit.requests.IngredientSearchRequest request = new com.example.plateit.requests.IngredientSearchRequest(
                ingredients, 10);

        RetrofitClient.getAgentService().findRecipesByIngredients(request)
                .enqueue(new Callback<List<com.example.plateit.responses.RecipeSummary>>() {
                    @Override
                    public void onResponse(Call<List<com.example.plateit.responses.RecipeSummary>> call,
                            Response<List<com.example.plateit.responses.RecipeSummary>> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            showRecipeResultsDialog(response.body());
                        } else {
                            Toast.makeText(getContext(), "No matching recipes found.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<com.example.plateit.responses.RecipeSummary>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showRecipeResultsDialog(List<com.example.plateit.responses.RecipeSummary> recipes) {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = new com.google.android.material.bottomsheet.BottomSheetDialog(
                requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_pantry_recipes, null);
        bottomSheetDialog.setContentView(sheetView);

        RecyclerView rvResults = sheetView.findViewById(R.id.rvRecipeResults);
        rvResults.setLayoutManager(new LinearLayoutManager(getContext()));

        // Creating a simple inner adapter or using a new separate adapter class?
        // For brevity, I'll assume we need to create 'PantryRecipeAdapter'.
        // I will use a simple inline binding logic or placeholder for now,
        // but user requested to "show returned recipes".
        // Let's create a quick adapter here or assume one exists.
        // I'll create a simple adapter inside this method or class.

        PantryRecipeAdapter recipeAdapter = new PantryRecipeAdapter(recipes, recipe -> {
            bottomSheetDialog.dismiss();
            fetchFullRecipeDetails(recipe.getId());
        });
        rvResults.setAdapter(recipeAdapter);

        bottomSheetDialog.show();
    }

    private void fetchFullRecipeDetails(int recipeId) {
        progressBar.setVisibility(View.VISIBLE);
        Toast.makeText(getContext(), "Loading recipe details...", Toast.LENGTH_SHORT).show();

        RetrofitClient.getAgentService().getRecipeDetails(recipeId)
                .enqueue(new Callback<com.example.plateit.responses.RecipeResponse>() {
                    @Override
                    public void onResponse(Call<com.example.plateit.responses.RecipeResponse> call,
                            Response<com.example.plateit.responses.RecipeResponse> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            // Navigate to RecipeActivity
                            Intent intent = new Intent(getContext(), RecipeActivity.class);
                            // Pass JSON
                            String json = new com.google.gson.Gson().toJson(response.body());
                            intent.putExtra("recipe_json", json);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getContext(), "Failed to load details.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<com.example.plateit.responses.RecipeResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Internal Adapter Class for Recipe Results
    private static class PantryRecipeAdapter extends RecyclerView.Adapter<PantryRecipeAdapter.ViewHolder> {
        private final List<com.example.plateit.responses.RecipeSummary> recipes;
        private final OnRecipeClickListener listener;

        interface OnRecipeClickListener {
            void onRecipeClick(com.example.plateit.responses.RecipeSummary recipe);
        }

        public PantryRecipeAdapter(List<com.example.plateit.responses.RecipeSummary> recipes,
                OnRecipeClickListener listener) {
            this.recipes = recipes;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry_recipe_result, parent,
                    false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            com.example.plateit.responses.RecipeSummary recipe = recipes.get(position);
            holder.tvTitle.setText(recipe.getTitle());
            holder.tvInfo.setText(
                    "Used: " + recipe.getUsedIngredientCount() + " | Missing: " + recipe.getMissedIngredientCount());

            if (recipe.getImage() != null && !recipe.getImage().isEmpty()) {
                com.squareup.picasso.Picasso.get().load(recipe.getImage()).into(holder.imgThumbnail);
            }

            holder.itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
        }

        @Override
        public int getItemCount() {
            return recipes.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvInfo;
            android.widget.ImageView imgThumbnail;

            ViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvRecipeTitle);
                tvInfo = itemView.findViewById(R.id.tvRecipeInfo);
                imgThumbnail = itemView.findViewById(R.id.imgRecipeThumbnail);
            }
        }
    }

    private void showAddOptions() {
        String[] options = { "Type Manually", "Scan with Camera" };
        new AlertDialog.Builder(requireContext())
                .setTitle("Add Item")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showManualAddDialog();
                    } else {
                        openCamera();
                    }
                })
                .show();
    }

    private void showManualAddDialog() {
        android.view.View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_pantry_item, null);
        android.widget.EditText etName = dialogView.findViewById(R.id.etName);
        android.widget.EditText etAmount = dialogView.findViewById(R.id.etAmount);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Add Pantry Item")
                .setView(dialogView)
                .setPositiveButton("Add", null) // Set null here to override later
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {
            android.widget.Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                String name = etName.getText().toString().trim();
                String amount = etAmount.getText().toString().trim();
                if (!name.isEmpty()) {
                    dialog.dismiss();
                    fetchImageAndSave(name, amount);
                }
            });
        });

        dialog.show();
    }

    private void fetchImageAndSave(String name, String amount) {
        progressBar.setVisibility(View.VISIBLE);
        Toast.makeText(getContext(), "Fetching image...", Toast.LENGTH_SHORT).show();

        RetrofitClient.getAgentService().getIngredientImage(name)
                .enqueue(new Callback<com.example.plateit.responses.IngredientImageResponse>() {
                    @Override
                    public void onResponse(Call<com.example.plateit.responses.IngredientImageResponse> call,
                            Response<com.example.plateit.responses.IngredientImageResponse> response) {
                        progressBar.setVisibility(View.GONE);
                        String imageUrl = null;
                        if (response.isSuccessful() && response.body() != null) {
                            imageUrl = response.body().getImageUrl();
                        }
                        saveItem(name, amount, imageUrl);
                    }

                    @Override
                    public void onFailure(Call<com.example.plateit.responses.IngredientImageResponse> call,
                            Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        saveItem(name, amount, null); // Save without image on failure
                    }
                });
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
        } else {
            Toast.makeText(getContext(), "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == android.app.Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            if (imageBitmap != null) {
                processImage(imageBitmap);
            }
        }
    }

    private void processImage(Bitmap bitmap) {
        progressBar.setVisibility(View.VISIBLE);
        Toast.makeText(getContext(), "Analyzing image...", Toast.LENGTH_SHORT).show();

        // Convert to Base64
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String imageBase64 = Base64.encodeToString(byteArray, Base64.NO_WRAP);

        PantryScanRequest request = new PantryScanRequest(imageBase64);

        RetrofitClient.getAgentService().scanPantry(request).enqueue(new Callback<PantryScanResponse>() {
            @Override
            public void onResponse(Call<PantryScanResponse> call, Response<PantryScanResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<PantryScanResponse.PantryItem> scannedItems = response.body().getItems();
                    if (scannedItems != null && !scannedItems.isEmpty()) {
                        saveBatchItems(scannedItems);
                        Toast.makeText(getContext(), "Added " + scannedItems.size() + " items!", Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        Toast.makeText(getContext(), "No items found in image", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to analyze image", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PantryScanResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveItem(String name, String amount, String imageUrl) {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        com.example.plateit.requests.PantryItemCreateRequest request = new com.example.plateit.requests.PantryItemCreateRequest(
                userId, name, amount, imageUrl);

        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getAgentService().addPantryItem(request).enqueue(new Callback<PantryItem>() {
            @Override
            public void onResponse(Call<PantryItem> call, Response<PantryItem> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    loadPantryItems();
                } else {
                    Toast.makeText(getContext(), "Failed to add item", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PantryItem> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveBatchItems(List<PantryScanResponse.PantryItem> scannedItems) {
        // Sequentially add items (Simple implementation, ideally batch endpoint)
        String userId = sessionManager.getUserId();
        if (userId == null)
            return;

        Toast.makeText(getContext(), "Saving " + scannedItems.size() + " items...", Toast.LENGTH_SHORT).show();

        for (PantryScanResponse.PantryItem sItem : scannedItems) {
            com.example.plateit.requests.PantryItemCreateRequest request = new com.example.plateit.requests.PantryItemCreateRequest(
                    userId, sItem.getName(), sItem.getAmount(), sItem.getImageUrl());
            RetrofitClient.getAgentService().addPantryItem(request).enqueue(new Callback<PantryItem>() {
                @Override
                public void onResponse(Call<PantryItem> call, Response<PantryItem> response) {
                }

                @Override
                public void onFailure(Call<PantryItem> call, Throwable t) {
                }
            });
        }

        // Delay reload slightly or implement recursion
        new android.os.Handler().postDelayed(this::loadPantryItems, 2000);
    }

    private void deleteItem(PantryItem item) {
        RetrofitClient.getAgentService().deletePantryItem(item.id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Remove locally to be snappy or reload
                    loadPantryItems();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPantryItems() {
        String userId = sessionManager.getUserId();
        if (userId == null)
            return;

        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getAgentService().getPantryItems(userId).enqueue(new Callback<List<PantryItem>>() {
            @Override
            public void onResponse(Call<List<PantryItem>> call, Response<List<PantryItem>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    pantryTypeList = response.body();

                    // Parse dates
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
                            java.util.Locale.getDefault());
                    for (PantryItem item : pantryTypeList) {
                        try {
                            if (item.start_date != null) {
                                item.dateAdded = sdf.parse(item.start_date).getTime();
                            }
                        } catch (Exception e) {
                            item.dateAdded = System.currentTimeMillis();
                        }
                    }

                    adapter.updateList(pantryTypeList);
                    tvEmpty.setVisibility(pantryTypeList.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<PantryItem>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to load pantry: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
