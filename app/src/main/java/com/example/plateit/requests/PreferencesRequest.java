package com.example.plateit.requests;

import java.util.List;

public class PreferencesRequest {
    private String userId;
    private List<String> preferences;

    public PreferencesRequest(String userId, List<String> preferences) {
        this.userId = userId;
        this.preferences = preferences;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getPreferences() {
        return preferences;
    }

    public void setPreferences(List<String> preferences) {
        this.preferences = preferences;
    }
}
