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
    Call<com.example.plateit.responses.RecipeResponse> getRecipeDetails(@retrofit2.http.Path("id") int recipeId);
}
