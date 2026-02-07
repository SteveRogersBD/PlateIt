package com.example.plateit.responses;

import com.example.plateit.models.Ingredient;
import com.example.plateit.models.Recipe;

import java.util.List;

public class ChatResponse {
    private String chat_bubble;
    private String ui_type;
    private RecipeListPayload recipe_data;
    private IngredientListPayload ingredient_data;
    private VideoListPayload video_data;

    public String getChatBubble() {
        return chat_bubble;
    }

    public String getUiType() {
        return ui_type;
    }

    public RecipeListPayload getRecipeData() {
        return recipe_data;
    }

    public IngredientListPayload getIngredientData() {
        return ingredient_data;
    }

    public VideoListPayload getVideoData() {
        return video_data;
    }

    // --- Inner Payload Classes ---

    public static class RecipeListPayload {
        private List<RecipeCard> items;

        public List<RecipeCard> getItems() {
            return items;
        }
    }

    public static class IngredientListPayload {
        private List<IngredientItem> items;

        public List<IngredientItem> getItems() {
            return items;
        }
    }

    public static class VideoListPayload {
        private List<VideoItem> items;

        public List<VideoItem> getItems() {
            return items;
        }
    }

    // --- Item Classes ---

    public static class RecipeCard {
        private int id;
        private String title;
        private String image_url;
        private Integer ready_in_minutes; // Optional
        private Integer missed_ingredient_count; // Optional
        private String source_url; // Optional

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getImageUrl() {
            return image_url;
        }

        public Integer getReadyInMinutes() {
            return ready_in_minutes;
        }

        public Integer getMissedIngredientCount() {
            return missed_ingredient_count;
        }

        public String getSourceUrl() {
            return source_url;
        }
    }

    public static class IngredientItem {
        private int id;
        private String name;
        private String image;
        private String amount;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getImage() {
            return image;
        }

        public String getAmount() {
            return amount;
        }
    }

    public static class VideoItem {
        private String title;
        private String url;
        private String thumbnail;

        public String getTitle() {
            return title;
        }

        public String getUrl() {
            return url;
        }

        public String getThumbnail() {
            return thumbnail;
        }
    }
}
