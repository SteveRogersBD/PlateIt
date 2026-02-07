package com.example.plateit.api;

import com.example.plateit.RecipeBlogs;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SerpApiService {
    @GET("search.json")
    Call<RecipeBlogs> search(
            @Query("engine") String engine,
            @Query("q") String query,
            @Query("api_key") String apiKey,
            @Query("num") int num);
}
