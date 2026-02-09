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
import com.example.plateit.api.SerpApiClient;

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

        // Initial State
        btnPaste.setColorFilter(getResources().getColor(R.color.gray_600));

        // --- 1. Smart Link Detection Logic ---
        etPasteUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString().toLowerCase();
                btnPaste.clearColorFilter(); // Clear legacy ColorFilter
                btnPaste.setImageTintList(null); // Clear modern ImageTintList

                if (text.contains("youtube.com") || text.contains("youtu.be")) {
                    btnPaste.setImageResource(R.drawable.youtube);
                } else if (text.contains("instagram.com")) {
                    btnPaste.setImageResource(R.drawable.instagram);
                } else if (text.contains("twitter.com") || text.contains("x.com")) {
                    btnPaste.setImageResource(R.drawable.twitter);
                } else if (text.contains("facebook.com")) {
                    btnPaste.setImageResource(R.drawable.facebook);
                } else if (text.contains("http") || text.contains("www.")) {
                    // Generic Link -> use the 'www' drawable
                    btnPaste.setImageResource(R.drawable.www);
                } else {
                    // Just text -> Revert to send icon with tech_black tint
                    btnPaste.setImageResource(android.R.drawable.ic_menu_send);
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

        // --- 3. RecyclerView Setup ---

        // Videos (Horizontal)
        LinearLayoutManager videoLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,
                false);
        rvRecipes.setLayoutManager(videoLayoutManager);

        // Initialize with empty list
        VideoAdapter videoAdapter = new VideoAdapter(new ArrayList<>(), this::showVideoOptionsDialog);
        rvRecipes.setAdapter(videoAdapter);

        // Blogs (Horizontal)
        RecyclerView rvBlogs = view.findViewById(R.id.rvBlogs);
        LinearLayoutManager blogLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,
                false);
        rvBlogs.setLayoutManager(blogLayoutManager);

        // Initialize Blog Adapter
        BlogAdapter blogAdapter = new BlogAdapter(new ArrayList<>(), this::showBlogOptionsDialog);
        rvBlogs.setAdapter(blogAdapter);

        // Fetch Real Data
        com.example.plateit.utils.SessionManager sessionManager = new com.example.plateit.utils.SessionManager(
                getContext());
        String userId = sessionManager.getUserId();

        if (userId != null) {
            // fetchVideoRecommendations(userId, videoAdapter); // Disabled to save API
            // calls
            // fetchBlogRecommendations(userId, blogAdapter); // Disabled to save API calls
        } else {
            // Fallback or Prompt Login
            Toast.makeText(getContext(), "Please sign in for recommendations", Toast.LENGTH_SHORT).show();
        }

        // Chat FAB
        com.google.android.material.floatingactionbutton.FloatingActionButton fabChat = view
                .findViewById(R.id.fabChat);
        fabChat.setOnClickListener(v ->

        showChatBottomSheet());

        return view;
    }

    private void fetchVideoRecommendations(String userId, VideoAdapter adapter) {
        RetrofitClient.getService().getRecommendations(userId)
                .enqueue(new Callback<com.example.plateit.responses.VideoRecommendationResponse>() {
                    @Override
                    public void onResponse(Call<com.example.plateit.responses.VideoRecommendationResponse> call,
                            Response<com.example.plateit.responses.VideoRecommendationResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<RecipeVideo> videos = response.body().getVideos();
                            if (videos != null && !videos.isEmpty()) {
                                adapter.updateData(videos);
                            }
                        } else {
                            // Silent failure or log
                        }
                    }

                    @Override
                    public void onFailure(Call<com.example.plateit.responses.VideoRecommendationResponse> call,
                            Throwable t) {
                        // Network error
                        Toast.makeText(getContext(), "Network error loading videos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchBlogRecommendations(String userId, BlogAdapter adapter) {
        // Use SerpApi directly from Android
        String apiKey = BuildConfig.SERP_API_KEY;
        if (apiKey == null || apiKey.isEmpty()) {
            Toast.makeText(getContext(), "SerpApi Key Missing", Toast.LENGTH_SHORT).show();
            return;
        }

        // Default query since we don't have easy access to user prefs unless fetched
        // first.
        String query = "best food blogs recipes 2024 -site:youtube.com";

        // Use the newly created SerpApiClient
        SerpApiClient.getService().search("google", query, apiKey, 8)
                .enqueue(new Callback<RecipeBlogs>() {
                    @Override
                    public void onResponse(Call<RecipeBlogs> call, Response<RecipeBlogs> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            RecipeBlogs blogResponse = response.body();
                            List<BlogItem> blogs = new ArrayList<>();

                            int recipeCount = (blogResponse.recipes_results != null)
                                    ? blogResponse.recipes_results.size()
                                    : 0;
                            int organicCount = (blogResponse.organic_results != null)
                                    ? blogResponse.organic_results.size()
                                    : 0;
                            android.util.Log.d("HomeFragment",
                                    "Found " + recipeCount + " recipes_results, " + organicCount + " organic_results");

                            // 1. Check recipes_results (Prioritize these as they have better images)
                            if (blogResponse.recipes_results != null) {
                                for (RecipeBlogs.RecipesResult result : blogResponse.recipes_results) {
                                    if (result.title != null && result.link != null) {
                                        String thumbnail = result.thumbnail;
                                        String source = result.source != null ? result.source : "Recipe";
                                        String snippet = (result.ingredients != null
                                                ? result.ingredients.size() + " ingredients"
                                                : "");
                                        blogs.add(new BlogItem(result.title, result.link, thumbnail, source, snippet));
                                    }
                                }
                            }

                            // 2. Check organic_results (Append as secondary)
                            if (blogResponse.organic_results != null) {
                                for (RecipeBlogs.OrganicResult result : blogResponse.organic_results) {
                                    if (result.title != null && result.link != null) {
                                        // Use the thumbnail field directly from POJO or fallback to pagemap
                                        String thumbnail = result.thumbnail;
                                        // Fallback logic
                                        if (thumbnail == null || thumbnail.isEmpty()) {
                                            // Try pagemap logic if needed, or leave null
                                        }

                                        String source = result.source != null ? result.source : "Web";
                                        blogs.add(new BlogItem(result.title, result.link, thumbnail, source,
                                                result.snippet));
                                    }
                                }
                            }

                            if (!blogs.isEmpty()) {
                                String firstThumb = blogs.get(0).getThumbnail();
                                Toast.makeText(getContext(), "First Thumb: " + firstThumb, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getContext(), "No blogs found!", Toast.LENGTH_SHORT).show();
                            }

                            adapter.updateData(blogs);
                        } else {
                            android.util.Log.e("HomeFragment", "SerpApi Failed: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<RecipeBlogs> call, Throwable t) {
                        android.util.Log.e("HomeFragment", "SerpApi Network Error: " + t.getMessage(), t);
                    }
                });
    }

    private void showBlogOptionsDialog(BlogItem blog) {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = new com.google.android.material.bottomsheet.BottomSheetDialog(
                requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_blog_options, null);
        bottomSheetDialog.setContentView(sheetView);

        ImageView imgHeader = sheetView.findViewById(R.id.imgBlogHeader);
        android.widget.TextView tvTitle = sheetView.findViewById(R.id.tvBlogTitle);
        android.widget.TextView tvSource = sheetView.findViewById(R.id.tvBlogSource);
        android.widget.TextView tvSnippet = sheetView.findViewById(R.id.tvBlogSnippet);

        View btnExtract = sheetView.findViewById(R.id.btnExtractRecipe);
        View btnRead = sheetView.findViewById(R.id.btnReadOnSite);

        tvTitle.setText(blog.getTitle());
        tvSource.setText(blog.getSource());
        tvSnippet.setText(blog.getSnippet());

        if (blog.getThumbnail() != null && !blog.getThumbnail().isEmpty()) {
            com.squareup.picasso.Picasso.get().load(blog.getThumbnail()).into(imgHeader);
        } else {
            imgHeader.setImageResource(R.drawable.ic_launcher_background);
        }

        btnExtract.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            extractRecipe(blog.getLink(), blog.getThumbnail());
        });

        btnRead.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            if (blog.getLink() != null) {
                android.content.Intent intent = new android.content.Intent(getContext(), BlogReaderActivity.class);
                intent.putExtra("blog_url", blog.getLink());
                startActivity(intent);
            }
        });

        bottomSheetDialog.show();
    }

    private void showChatBottomSheet() {
        android.content.Intent intent = new android.content.Intent(getContext(), ChatActivity.class);
        startActivity(intent);
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
                    processBitmapForRecipe(photo);
                }
            });

    private final androidx.activity.result.ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    processUriForRecipe(uri);
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

    // --- Image Processing Helpers ---

    private void processBitmapForRecipe(android.graphics.Bitmap bitmap) {
        try {
            java.io.File file = new java.io.File(getContext().getCacheDir(),
                    "recipe_scan_" + System.currentTimeMillis() + ".jpg");
            file.createNewFile();
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, bos);
            byte[] bitmapdata = bos.toByteArray();

            java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();

            uploadImageForRecipe(file);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error processing camera image", Toast.LENGTH_SHORT).show();
        }
    }

    private void processUriForRecipe(android.net.Uri uri) {
        try {
            java.io.InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            java.io.File file = new java.io.File(getContext().getCacheDir(),
                    "recipe_gallery_" + System.currentTimeMillis() + ".jpg");
            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(file);

            byte[] buffer = new byte[4 * 1024]; // 4kb buffer
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            uploadImageForRecipe(file);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error processing gallery image", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageForRecipe(java.io.File file) {
        showExtractionProgress("Scanning Dish...");

        okhttp3.RequestBody reqFile = okhttp3.RequestBody.create(okhttp3.MediaType.parse("image/jpeg"), file);
        okhttp3.MultipartBody.Part body = okhttp3.MultipartBody.Part.createFormData("file", file.getName(), reqFile);

        RetrofitClient.getAgentService().identifyDishFromImage(body).enqueue(new Callback<RecipeResponse>() {
            @Override
            public void onResponse(Call<RecipeResponse> call, Response<RecipeResponse> response) {
                if (extractionDialog != null)
                    extractionDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    showRecipePreviewDialog(response.body());
                } else {
                    Toast.makeText(getContext(), "Failed to identify dish.", Toast.LENGTH_SHORT).show();
                }
                file.delete();
            }

            @Override
            public void onFailure(Call<RecipeResponse> call, Throwable t) {
                if (extractionDialog != null)
                    extractionDialog.dismiss();
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                file.delete();
            }
        });
    }

    private void showVideoOptionsDialog(RecipeVideo video) {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = new com.google.android.material.bottomsheet.BottomSheetDialog(
                requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_video_options, null);
        bottomSheetDialog.setContentView(sheetView);

        android.widget.ImageView imgThumbnail = sheetView.findViewById(R.id.imgVideoThumbnail);
        android.widget.TextView tvTitle = sheetView.findViewById(R.id.tvVideoTitle);
        android.widget.TextView tvChannel = sheetView.findViewById(R.id.tvVideoChannel);
        View btnExtract = sheetView.findViewById(R.id.btnExtractRecipe);
        View btnWatch = sheetView.findViewById(R.id.btnWatchVideo);

        tvTitle.setText(video.getTitle());
        tvChannel.setText(video.getChannel() + " • " + (video.getViews() != null ? video.getViews() + " views" : ""));

        if (video.getThumbnail() != null && !video.getThumbnail().isEmpty()) {
            com.squareup.picasso.Picasso.get()
                    .load(video.getThumbnail())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(imgThumbnail);
        } else {
            imgThumbnail.setImageResource(R.drawable.ic_launcher_background);
        }

        btnExtract.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            extractRecipe(video.getLink(), video.getThumbnail());
        });

        btnWatch.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            if (video.getLink() != null) {
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse(video.getLink()));
                startActivity(intent);
            }
        });

        bottomSheetDialog.show();
    }

    private android.app.Dialog extractionDialog;

    private void showExtractionProgress(String contextUrl) {
        extractionDialog = new android.app.Dialog(requireContext());
        extractionDialog.setContentView(R.layout.dialog_extraction_progress);
        extractionDialog.getWindow()
                .setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        extractionDialog.setCancelable(false);

        // Populate with thumbnail if available (from video list or blog list context)
        // Since we only have URL here, we might not have the image easily unless
        // passed.
        // For now, we will try to find it or just show generic background.

        // Actually, let's pass the thumbnail URL to extractRecipe if possible, but for
        // now we'll stick to a nice blurred background.

        extractionDialog.show();
    }

    private void extractRecipe(String url) {
        extractRecipe(url, "");
    }

    private void extractRecipe(String url, String thumbnailUrl) {
        // Show Custom Dialog
        extractionDialog = new android.app.Dialog(requireContext());
        extractionDialog.setContentView(R.layout.dialog_extraction_progress);
        extractionDialog.getWindow()
                .setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        extractionDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        extractionDialog.setCancelable(false);

        ImageView imgBg = extractionDialog.findViewById(R.id.imgBackgroundThumbnail);
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            com.squareup.picasso.Picasso.get().load(thumbnailUrl).into(imgBg);
        } else {
            imgBg.setImageResource(R.drawable.ic_launcher_background); // Or a default drawable
        }

        extractionDialog.show();

        VideoRequest request = new VideoRequest(url);
        RetrofitClient.getService().extractRecipe(request)
                .enqueue(new Callback<RecipeResponse>() {
                    @Override
                    public void onResponse(Call<RecipeResponse> call, Response<RecipeResponse> response) {
                        if (extractionDialog != null)
                            extractionDialog.dismiss();
                        if (response.isSuccessful() && response.body() != null) {
                            showRecipePreviewDialog(response.body());
                        } else {
                            Toast.makeText(getContext(), "Extraction failed. Try another link.", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(Call<RecipeResponse> call, Throwable t) {
                        if (extractionDialog != null)
                            extractionDialog.dismiss();
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
            // Pass JSON to avoid Serializable issues
            String json = new com.google.gson.Gson().toJson(recipe);
            intent.putExtra("recipe_json", json);
            startActivity(intent);
        });

        btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }
}
