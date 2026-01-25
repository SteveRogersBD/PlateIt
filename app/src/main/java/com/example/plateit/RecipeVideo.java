package com.example.plateit;

public class RecipeVideo {
    private String title;
    private String time;
    private int thumbnailResId;

    public RecipeVideo(String title, String time, int thumbnailResId) {
        this.title = title;
        this.time = time;
        this.thumbnailResId = thumbnailResId;
    }

    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }

    public int getThumbnailResId() {
        return thumbnailResId;
    }
}
