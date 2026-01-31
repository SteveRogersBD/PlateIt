package com.example.plateit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.plateit.R;
import java.util.ArrayList;
import java.util.List;

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

        android.widget.EditText etPasteUrl = view.findViewById(R.id.etPasteUrl);
        // Note: In the layout xml the save icon is btnPaste, and camera is btnScan.
        // We will use btnPaste as the "Search" button for now based on typical user
        // flow, or we can add a specific search button.
        // Let's treat the 'btnPaste' (save icon) as the submit button for now since
        // it's next to the input.
        View btnSearch = view.findViewById(R.id.btnPaste);

        btnSearch.setOnClickListener(v -> {
            String url = etPasteUrl.getText().toString().trim();
            if (url.isEmpty()) {
                android.widget.Toast
                        .makeText(getContext(), "Please paste a video URL", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            // 1. Show Loading Dialog
            android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(getContext());
            progressDialog.setMessage("Extracting Recipe...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            // 2. Call API
            com.example.plateit.requests.VideoRequest request = new com.example.plateit.requests.VideoRequest(url);
            com.example.plateit.api.RetrofitClient.getService().extractRecipe(request)
                    .enqueue(new retrofit2.Callback<com.example.plateit.responses.RecipeResponse>() {
                        @Override
                        public void onResponse(retrofit2.Call<com.example.plateit.responses.RecipeResponse> call,
                                retrofit2.Response<com.example.plateit.responses.RecipeResponse> response) {
                            progressDialog.dismiss();
                            if (response.isSuccessful() && response.body() != null) {
                                showRecipePreviewDialog(response.body());
                            } else {
                                android.widget.Toast
                                        .makeText(getContext(), "Failed to extract recipe: " + response.message(),
                                                android.widget.Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<com.example.plateit.responses.RecipeResponse> call,
                                Throwable t) {
                            progressDialog.dismiss();
                            android.widget.Toast.makeText(getContext(), "Error: " + t.getMessage(),
                                    android.widget.Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Mock Data
        List<RecipeVideo> mockData = new ArrayList<>();
        mockData.add(new RecipeVideo("15-Minute Creamy Pasta", "15 min", R.drawable.ic_launcher_background));
        mockData.add(new RecipeVideo("Crispy Air Fryer Chicken", "25 min", R.drawable.ic_launcher_background));
        mockData.add(new RecipeVideo("Ultimate Chocolate Cake", "1 hr", R.drawable.ic_launcher_background));
        mockData.add(new RecipeVideo("Spicy Garlic Noodles", "10 min", R.drawable.ic_launcher_background));
        mockData.add(new RecipeVideo("Healthy Avocado Toast", "5 min", R.drawable.ic_launcher_background));
        mockData.add(new RecipeVideo("One Pan Salmon & Veggies", "20 min", R.drawable.ic_launcher_background));

        VideoAdapter adapter = new VideoAdapter(mockData);
        rvRecipes.setAdapter(adapter);

        return view;
    }

    private void showRecipePreviewDialog(com.example.plateit.responses.RecipeResponse recipe) {
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
