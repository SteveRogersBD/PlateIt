package com.example.plateit.api;

import com.example.plateit.requests.SignInRequest;
import com.example.plateit.requests.SignUpRequest;
import com.example.plateit.requests.VideoRequest;
import com.example.plateit.responses.AuthResponse;
import com.example.plateit.responses.RecipeResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

import okhttp3.MultipartBody;
import retrofit2.http.Multipart;
import retrofit2.http.Part;

public interface RecipeApiService {
    @POST("/extract_recipe")
    Call<RecipeResponse> extractRecipe(@Body VideoRequest body);

    @Multipart
    @POST("/extract_recipe_image")
    Call<RecipeResponse> extractRecipeImage(@Part MultipartBody.Part image);

    @POST("/signin")
    Call<AuthResponse> signin(@Body SignInRequest body);

    @POST("/signup")
    Call<AuthResponse> signup(@Body SignUpRequest body);

    @POST("/chat")
    Call<com.example.plateit.responses.ChatResponse> chat(@Body com.example.plateit.requests.ChatRequest body);
}