package com.example.plateit;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.example.plateit.requests.VideoRequest;
import com.example.plateit.responses.RecipeResponse;
import com.example.plateit.api.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView rvRecipes = view.findViewById(R.id.rvRecipes);
        rvRecipes.setLayoutManager(new LinearLayoutManager(getContext()));

        EditText etPasteUrl = view.findViewById(R.id.etPasteUrl);
        ImageView btnPaste = view.findViewById(R.id.btnPaste);
        ImageView btnScan = view.findViewById(R.id.btnScan);

        // --- 1. Smart Link Detection Logic ---
        etPasteUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString().toLowerCase();
                if (text.contains("youtube.com") || text.contains("youtu.be")) {
                    // It's a YouTube link, maybe show a YouTube icon?
                    // For now, let's just ensure the "Go" button is prominent
                    btnPaste.setColorFilter(getResources().getColor(android.R.color.holo_red_dark));
                } else if (text.contains("http")) {
                    // Generic Link
                    btnPaste.setColorFilter(getResources().getColor(R.color.app_primary));
                } else {
                    // Just text
                    btnPaste.setColorFilter(getResources().getColor(R.color.tech_black));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // --- 2. Action Buttons ---

        btnScan.setOnClickListener(v -> showVisionBottomSheet());

        // btnUpload.setOnClickListener(v -> { // Removed
        // Toast.makeText(getContext(), "Opening Gallery for Video...",
        // Toast.LENGTH_SHORT).show();
        // // TODO: Implement Video Picker Logic here
        // });

        btnPaste.setOnClickListener(v -> {
            String url = etPasteUrl.getText().toString().trim();
            if (url.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a link or question", Toast.LENGTH_SHORT).show();
                return;
            }
            extractRecipe(url);
        });

        // Mock Data
        List<RecipeVideo> mockData = new ArrayList<>();
        mockData.add(new RecipeVideo("15-Minute Creamy Pasta", "15 min", R.drawable.ic_launcher_background));
        mockData.add(new RecipeVideo("Crispy Air Fryer Chicken", "25 min", R.drawable.ic_launcher_background));
        mockData.add(new RecipeVideo("Ultimate Chocolate Cake", "1 hr", R.drawable.ic_launcher_background));
        mockData.add(new RecipeVideo("Spicy Garlic Noodles", "10 min", R.drawable.ic_launcher_background));

        // --- 3. RecyclerView Setup ---

        // Videos (Horizontal)
        LinearLayoutManager videoLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,
                false);
        rvRecipes.setLayoutManager(videoLayoutManager);

        List<RecipeVideo> mockVideos = new ArrayList<>();
        mockVideos.add(new RecipeVideo("15-Minute Creamy Pasta", "15 min", R.drawable.ic_launcher_background));
        mockVideos.add(new RecipeVideo("Crispy Air Fryer Chicken", "25 min", R.drawable.ic_launcher_background));
        mockVideos.add(new RecipeVideo("Ultimate Chocolate Cake", "1 hr", R.drawable.ic_launcher_background));
        mockVideos.add(new RecipeVideo("Spicy Garlic Noodles", "10 min", R.drawable.ic_launcher_background));

        VideoAdapter videoAdapter = new VideoAdapter(mockVideos);
        rvRecipes.setAdapter(videoAdapter);

        // Blogs (Horizontal)
        RecyclerView rvBlogs = view.findViewById(R.id.rvBlogs);
        LinearLayoutManager blogLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,
                false);
        rvBlogs.setLayoutManager(blogLayoutManager);

        List<BlogItem> mockBlogs = new ArrayList<>();
        mockBlogs.add(new BlogItem("The Secret to Perfect Sourdough", "BAKING"));
        mockBlogs.add(new BlogItem("5 Knife Skills You Need", "SKILLS"));
        mockBlogs.add(new BlogItem("Best Umami Bombs", "TIPS"));
        mockBlogs.add(new BlogItem("History of Ramen", "CULTURE"));

        BlogAdapter blogAdapter = new BlogAdapter(mockBlogs);
        rvBlogs.setAdapter(blogAdapter);

        return view;
    }

    // --- Vision Launchers ---

    private final androidx.activity.result.ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    launchCamera();
                } else {
                    Toast.makeText(getContext(), "Camera permission required.", Toast.LENGTH_SHORT).show();
                }
            });

    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> cameraLauncher = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    android.graphics.Bitmap photo = (android.graphics.Bitmap) result.getData().getExtras().get("data");
                    // TODO: Send 'photo' to Backend Agent
                    Toast.makeText(getContext(), "Photo captured! Sending to Chef...", Toast.LENGTH_SHORT).show();
                }
            });

    private final androidx.activity.result.ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    // TODO: Send 'uri' to Backend Agent
                    Toast.makeText(getContext(), "Image selected! Analyzing...", Toast.LENGTH_SHORT).show();
                }
            });

    private void launchCamera() {
        android.content.Intent takePictureIntent = new android.content.Intent(
                android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            cameraLauncher.launch(takePictureIntent);
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(getContext(), "Camera not found.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showVisionBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_vision, null);
        bottomSheetDialog.setContentView(sheetView);

        View btnCamera = sheetView.findViewById(R.id.btnOptionCamera);
        View btnGallery = sheetView.findViewById(R.id.btnOptionGallery);

        btnCamera.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            if (androidx.core.content.ContextCompat.checkSelfPermission(requireContext(),
                    android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA);
            }
        });

        btnGallery.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            galleryLauncher.launch("image/*");
        });

        bottomSheetDialog.show();
    }

    private void extractRecipe(String url) {
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(getContext());
        progressDialog.setMessage("Extracting Recipe...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        VideoRequest request = new VideoRequest(url);
        RetrofitClient.getService().extractRecipe(request)
                .enqueue(new Callback<RecipeResponse>() {
                    @Override
                    public void onResponse(Call<RecipeResponse> call, Response<RecipeResponse> response) {
                        progressDialog.dismiss();
                        if (response.isSuccessful() && response.body() != null) {
                            showRecipePreviewDialog(response.body());
                        } else {
                            Toast.makeText(getContext(), "Extraction failed. Try another link.", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(Call<RecipeResponse> call, Throwable t) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showRecipePreviewDialog(RecipeResponse recipe) {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = new com.google.android.material.bottomsheet.BottomSheetDialog(
                requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_recipe_preview, null);
        bottomSheetDialog.setContentView(sheetView);

        android.widget.TextView tvTitle = sheetView.findViewById(R.id.tvPreviewTitle);
        android.widget.TextView tvTime = sheetView.findViewById(R.id.tvPreviewTime);
        android.widget.TextView tvIngredientsCount = sheetView.findViewById(R.id.tvPreviewIngredients);
        com.google.android.material.button.MaterialButton btnStartCooking = sheetView
                .findViewById(R.id.btnStartCooking);
        com.google.android.material.button.MaterialButton btnCancel = sheetView.findViewById(R.id.btnCancel);

        tvTitle.setText(recipe.getName());
        tvTime.setText(recipe.getTotalTime() != null ? recipe.getTotalTime() : "N/A");
        tvIngredientsCount
                .setText((recipe.getIngredients() != null ? recipe.getIngredients().size() : 0) + " Ingredients");

        btnStartCooking.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            android.content.Intent intent = new android.content.Intent(getContext(), RecipeActivity.class);
            intent.putExtra("recipe_data", recipe);
            startActivity(intent);
        });

        btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }
}
