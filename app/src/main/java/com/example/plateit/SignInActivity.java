package com.example.plateit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.plateit.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class SignInActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnSignIn;
    private TextView tvSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Check for existing session
        SessionManager sessionManager = new SessionManager(
                SignInActivity.this);
        if (sessionManager.isLoggedIn()) {
            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvSignUp = findViewById(R.id.tvSignUp);

        btnSignIn.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                android.widget.Toast.makeText(SignInActivity.this, "Please enter email and password",
                        android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            com.example.plateit.requests.SignInRequest request = new com.example.plateit.requests.SignInRequest(email,
                    password);

            com.example.plateit.api.RetrofitClient.getService().signin(request)
                    .enqueue(new retrofit2.Callback<com.example.plateit.responses.AuthResponse>() {
                        @Override
                        public void onResponse(retrofit2.Call<com.example.plateit.responses.AuthResponse> call,
                                retrofit2.Response<com.example.plateit.responses.AuthResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                // Save session
                                sessionManager.createLoginSession(
                                        response.body().getUserId(),
                                        response.body().getEmail(),
                                        response.body().getUsername());

                                android.widget.Toast.makeText(SignInActivity.this, "Sign In Successful",
                                        android.widget.Toast.LENGTH_SHORT).show();
                                // Proceed to Preferences Activity
                                Intent intent = new Intent(SignInActivity.this, PreferencesActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                android.widget.Toast.makeText(SignInActivity.this,
                                        "Sign In Failed: " + response.message(), android.widget.Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<com.example.plateit.responses.AuthResponse> call,
                                Throwable t) {
                            android.widget.Toast.makeText(SignInActivity.this, "Error: " + t.getMessage(),
                                    android.widget.Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
