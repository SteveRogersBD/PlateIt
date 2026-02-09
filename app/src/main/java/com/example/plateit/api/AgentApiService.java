package com.example.plateit.api;

import com.example.plateit.requests.ChatRequest;
import com.example.plateit.responses.ChatResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AgentApiService {
        @POST("chat")
        Call<ChatResponse> chat(@Body ChatRequest request);

        @retrofit2.http.GET("recipes/{id}/full")
        retrofit2.Call<com.example.plateit.responses.RecipeResponse> getRecipeDetails(
                        @retrofit2.http.Path("id") int recipeId);

        @retrofit2.http.Multipart
        @retrofit2.http.POST("pantry/scan_image")
        retrofit2.Call<com.example.plateit.responses.PantryScanResponse> scanPantryImage(
                        @retrofit2.http.Part okhttp3.MultipartBody.Part image);

        @retrofit2.http.Multipart
        @retrofit2.http.POST("recipes/identify_dish")
        retrofit2.Call<com.example.plateit.responses.RecipeResponse> identifyDishFromImage(
                        @retrofit2.http.Part okhttp3.MultipartBody.Part image);

        @retrofit2.http.GET("get_ingredient_image")
        retrofit2.Call<com.example.plateit.responses.IngredientImageResponse> getIngredientImage(
                        @retrofit2.http.Query("query") String query);

        @retrofit2.http.GET("pantry/{user_id}")
        retrofit2.Call<java.util.List<com.example.plateit.db.PantryItem>> getPantryItems(
                        @retrofit2.http.Path("user_id") String userId);

        @retrofit2.http.POST("pantry/add")
        retrofit2.Call<com.example.plateit.db.PantryItem> addPantryItem(
                        @retrofit2.http.Body com.example.plateit.requests.PantryItemCreateRequest request);

        @retrofit2.http.DELETE("pantry/{item_id}")
        retrofit2.Call<Void> deletePantryItem(@retrofit2.http.Path("item_id") int itemId);

        @retrofit2.http.POST("recipes/findByIngredients")
        retrofit2.Call<java.util.List<com.example.plateit.responses.RecipeSummary>> findRecipesByIngredients(
                        @retrofit2.http.Body com.example.plateit.requests.IngredientSearchRequest request);

        @retrofit2.http.GET("recommendations/blogs/{user_id}")
        retrofit2.Call<com.example.plateit.responses.BlogRecommendationResponse> getBlogRecommendations(
                        @retrofit2.http.Path("user_id") String userId);

        @retrofit2.http.POST("users/preferences")
        retrofit2.Call<Void> updatePreferences(
                        @retrofit2.http.Body com.example.plateit.requests.PreferencesRequest request);
}
