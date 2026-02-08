package com.example.plateit.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pantry_items")
public class PantryItem {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String amount;

    // Local usage
    public long dateAdded;

    @com.google.gson.annotations.SerializedName("image_url")
    public String imageUrl;

    @com.google.gson.annotations.SerializedName("created_at")
    public String start_date; // Maps to created_at from backend

    public PantryItem(String name, String amount, long dateAdded, String imageUrl) {
        this.name = name;
        this.amount = amount;
        this.dateAdded = dateAdded;
        this.imageUrl = imageUrl;
    }
}
