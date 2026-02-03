package com.example.plateit;

public class BlogItem {
    private String title;
    private String category;
    private String imageUrl; // Placeholder for now

    public BlogItem(String title, String category) {
        this.title = title;
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }
}
