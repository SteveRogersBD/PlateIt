package com.example.plateit.api;

import com.example.plateit.requests.SignInRequest;
import com.example.plateit.requests.SignUpRequest;
import com.example.plateit.requests.VideoRequest;
import com.example.plateit.responses.AuthResponse;
import com.example.plateit.responses.RecipeResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RecipeApiService {
    @POST("/extract_recipe")
    Call<RecipeResponse> extractRecipe(@Body VideoRequest body);

    @POST("/signin")
    Call<AuthResponse> signin(@Body SignInRequest body);

    @POST("/signup")
    Call<AuthResponse> signup(@Body SignUpRequest body);
}