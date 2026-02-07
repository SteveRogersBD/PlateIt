package com.example.plateit.responses;

import com.example.plateit.BlogItem;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BlogRecommendationResponse {
    @SerializedName("blogs")
    private List<BlogItem> blogs;

    public List<BlogItem> getBlogs() {
        return blogs;
    }
}
