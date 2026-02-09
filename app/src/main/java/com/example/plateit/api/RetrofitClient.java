package com.example.plateit.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // Local Debug URL (Android Emulator uses 10.0.2.2 for localhost)
    private static final String BASE_URL = "http://10.0.2.2:8080/";
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
