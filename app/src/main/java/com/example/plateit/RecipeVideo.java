package com.example.plateit;

import com.google.gson.annotations.SerializedName;

public class RecipeVideo {
    @SerializedName("title")
    private String title;

    @SerializedName("link")
    private String link;

    @SerializedName("thumbnail")
    private String thumbnail;

    @SerializedName("channel")
    private String channel;

    @SerializedName("views")
    private String views;

    @SerializedName("length")
    private String length;

    // Constructor
    public RecipeVideo(String title, String link, String thumbnail, String channel, String views, String length) {
        this.title = title;
        this.link = link;
        this.thumbnail = thumbnail;
        this.channel = channel;
        this.views = views;
        this.length = length;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getChannel() {
        return channel;
    }

    public String getViews() {
        return views;
    }

    public String getLength() {
        return length;
    }
}
