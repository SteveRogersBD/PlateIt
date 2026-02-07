package com.example.plateit;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import com.example.plateit.api.RetrofitClient;
import com.example.plateit.requests.VideoRequest;
import com.example.plateit.responses.RecipeResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BlogReaderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_reader);

        String url = getIntent().getStringExtra("blog_url");
        if (url == null) {
            finish();
            return;
        }

        WebView webView = findViewById(R.id.wvBlogContent);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient()); // Keep navigation inside
        webView.loadUrl(url);

        findViewById(R.id.fabExtract).setOnClickListener(v -> extractRecipe(url));
    }

    private void extractRecipe(String url) {
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Extracting Recipe...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Using VideoRequest for url structure, even though it's a blog
        VideoRequest request = new VideoRequest(url);
        RetrofitClient.getService().extractRecipe(request)
                .enqueue(new Callback<RecipeResponse>() {
                    @Override
                    public void onResponse(Call<RecipeResponse> call, Response<RecipeResponse> response) {
                        progressDialog.dismiss();
                        if (response.isSuccessful() && response.body() != null) {
                            startRecipePreview(response.body());
                        } else {
                            Toast.makeText(BlogReaderActivity.this, "Extraction failed.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<RecipeResponse> call, Throwable t) {
                        progressDialog.dismiss();
                        Toast.makeText(BlogReaderActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startRecipePreview(RecipeResponse recipe) {
        // Reuse logic? Or better, pass straight to RecipeActivity since we are already
        // in an activity
        // Ideally we show a preview first, but for now jumping to RecipeActivity
        // simplifies flow
        Intent intent = new Intent(this, RecipeActivity.class);
        intent.putExtra("recipe_data", recipe);
        startActivity(intent);
        finish(); // Close reader? Or keep user here? Let's close for now as they "converted" it
    }
}
