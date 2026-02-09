package com.example.plateit.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // Updated to local IP for physical device testing. Ensure your phone is on the
    // same WiFi.
    private static final String BASE_URL = "http://192.168.1.249:8000/";
    private static Retrofit recipefit = null;

    public static RecipeApiService getService() {
        if (recipefit == null) {
            okhttp3.OkHttpClient okHttpClient = new okhttp3.OkHttpClient.Builder()
                    .connectTimeout(180, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(180, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(180, java.util.concurrent.TimeUnit.SECONDS)
                    .build();

            recipefit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return recipefit.create(RecipeApiService.class);
    }

    public static AgentApiService getAgentService() {
        if (recipefit == null) {
            getService(); // Initialize retrofit
        }
        return recipefit.create(AgentApiService.class);
    }

}
