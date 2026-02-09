package com.example.plateit.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "PlateItPref";
    private static final String IS_LOGGED_IN = "IsLoggedIn";
    private static final String ONBOARDING_COMPLETED = "OnboardingCompleted";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_USERNAME = "username";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context _context;

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(String userId, String email, String username) {
        editor.putBoolean(IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_USERNAME, username);
        editor.commit();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGGED_IN, false);
    }

    public String getUserId() {
        return pref.getString(KEY_USER_ID, null);
    }

    public void setOnboardingCompleted() {
        editor.putBoolean(ONBOARDING_COMPLETED, true);
        editor.commit();
    }

    public boolean isOnboardingCompleted() {
        return pref.getBoolean(ONBOARDING_COMPLETED, false);
    }
}
