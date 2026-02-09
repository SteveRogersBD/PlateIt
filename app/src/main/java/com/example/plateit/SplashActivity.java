package com.example.plateit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        com.example.plateit.utils.SessionManager sessionManager = new com.example.plateit.utils.SessionManager(this);

        new Handler().postDelayed(() -> {
            Intent intent;

            // Check if user has completed onboarding
            if (!sessionManager.isOnboardingCompleted()) {
                // First time user - show onboarding
                intent = new Intent(SplashActivity.this, OnboardingActivity.class);
            } else if (sessionManager.isLoggedIn()) {
                // Returning logged-in user - go to main
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                // Returning user but not logged in - go to sign in
                intent = new Intent(SplashActivity.this, SignInActivity.class);
            }

            startActivity(intent);
            finish();
        }, 3000);
    }
}
