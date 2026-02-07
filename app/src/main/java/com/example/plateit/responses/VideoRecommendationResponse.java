package com.example.plateit.responses;

import com.example.plateit.RecipeVideo;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class VideoRecommendationResponse {
    @SerializedName("videos")
    private List<RecipeVideo> videos;

    public List<RecipeVideo> getVideos() {
        return videos;
    }
}
