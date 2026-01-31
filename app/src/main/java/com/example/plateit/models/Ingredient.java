package com.example.plateit.models;

import java.io.Serializable;

public class Ingredient implements Serializable {

    private String name;
    private String amount;
    private String imageUrl;

    public Ingredient(String name, String amount, String imageUrl) {
        this.name = name;
        this.amount = amount;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getAmount() {
        return amount;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
