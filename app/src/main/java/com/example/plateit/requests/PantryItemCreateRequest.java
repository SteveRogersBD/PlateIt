package com.example.plateit.requests;

public class PantryItemCreateRequest {
    private String user_id;
    private String name;
    private String amount;
    private String image_url;

    public PantryItemCreateRequest(String user_id, String name, String amount, String image_url) {
        this.user_id = user_id;
        this.name = name;
        this.amount = amount;
        this.image_url = image_url;
    }
}
