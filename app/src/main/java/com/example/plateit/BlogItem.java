package com.example.plateit;

import com.google.gson.annotations.SerializedName;

public class BlogItem {
    @SerializedName("title")
    private String title;

    @SerializedName("link")
    private String link;

    @SerializedName("snippet")
    private String snippet;

    @SerializedName("source")
    private String source;

    @SerializedName("thumbnail")
    private String thumbnail;

    public BlogItem(String title, String link, String snippet, String source, String thumbnail) {
        this.title = title;
        this.link = link;
        this.snippet = snippet;
        this.source = source;
        this.thumbnail = thumbnail;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getSnippet() {
        return snippet;
    }

    public String getSource() {
        return source;
    }

    public String getThumbnail() {
        return thumbnail;
    }
}
