package com.example.plateit.responses;

import java.util.List;

public class PantryScanResponse {
    private List<PantryItem> items;

    public List<PantryItem> getItems() {
        return items;
    }

    public static class PantryItem {
        private String name;
        private String amount;
        private String image_url;

        public String getName() {
            return name;
        }

        public String getAmount() {
            return amount;
        }

        public String getImageUrl() {
            return image_url;
        }
    }
}
