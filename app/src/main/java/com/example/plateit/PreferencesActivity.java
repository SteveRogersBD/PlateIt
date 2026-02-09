package com.example.plateit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.plateit.api.RetrofitClient;
import com.example.plateit.requests.PreferencesRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PreferencesActivity extends AppCompatActivity {

    private ChipGroup chipGroupPreferences;
    private MaterialButton btnContinue, btnSkip;
    private com.example.plateit.utils.SessionManager sessionManager;

    // Predefined meal preference options
    private final String[] preferenceOptions = {
            "Italian", "Mexican", "Chinese", "Japanese", "Indian",
            "Thai", "Mediterranean", "American", "French", "Korean",
            "Vegetarian", "Vegan", "Gluten-Free", "Keto", "Low-Carb",
            "Desserts", "Breakfast", "Quick Meals", "Healthy", "Comfort Food"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        sessionManager = new com.example.plateit.utils.SessionManager(this);

        chipGroupPreferences = findViewById(R.id.chipGroupPreferences);
        btnContinue = findViewById(R.id.btnContinue);
        btnSkip = findViewById(R.id.btnSkip);

        // Populate chips
        for (String preference : preferenceOptions) {
            Chip chip = new Chip(this);
            chip.setText(preference);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.white);
            chip.setChipStrokeColorResource(R.color.chef_orange);
            chip.setChipStrokeWidth(2f);
            chip.setTextColor(getResources().getColor(R.color.tech_black, null));
            chip.setCheckedIconVisible(true);
            chipGroupPreferences.addView(chip);
        }

        btnContinue.setOnClickListener(v -> savePreferences());
        btnSkip.setOnClickListener(v -> skipToMain());
    }

    private void savePreferences() {
        List<String> selectedPreferences = new ArrayList<>();

        for (int i = 0; i < chipGroupPreferences.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupPreferences.getChildAt(i);
            if (chip.isChecked()) {
                selectedPreferences.add(chip.getText().toString());
            }
        }

        if (selectedPreferences.isEmpty()) {
            Toast.makeText(this, "Please select at least one preference", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        android.util.Log.d("PreferencesActivity", "Saving preferences for user: " + userId);
        android.util.Log.d("PreferencesActivity", "Selected preferences: " + selectedPreferences);

        PreferencesRequest request = new PreferencesRequest(userId, selectedPreferences);

        RetrofitClient.getAgentService().updatePreferences(request)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        android.util.Log.d("PreferencesActivity", "Response code: " + response.code());
                        if (response.isSuccessful()) {
                            Toast.makeText(PreferencesActivity.this, "Preferences saved!", Toast.LENGTH_SHORT).show();
                            navigateToMain();
                        } else {
                            try {
                                String errorBody = response.errorBody() != null ? response.errorBody().string()
                                        : "Unknown error";
                                android.util.Log.e("PreferencesActivity", "Error response: " + errorBody);
                                Toast.makeText(PreferencesActivity.this,
                                        "Failed: " + response.code() + " - " + errorBody, Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(PreferencesActivity.this, "Failed to save preferences",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        android.util.Log.e("PreferencesActivity", "Network error: " + t.getMessage(), t);
                        Toast.makeText(PreferencesActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void skipToMain() {
        navigateToMain();
    }

    private void navigateToMain() {
        Intent intent = new Intent(PreferencesActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
