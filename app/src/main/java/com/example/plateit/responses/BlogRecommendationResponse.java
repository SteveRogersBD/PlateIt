package com.example.plateit.responses;

import java.util.List;
import com.example.plateit.BlogItem;

public class BlogRecommendationResponse {
    private List<BlogItem> blogs;

    public List<BlogItem> getBlogs() {
        return blogs;
    }

    public void setBlogs(List<BlogItem> blogs) {
        this.blogs = blogs;
    }
}
