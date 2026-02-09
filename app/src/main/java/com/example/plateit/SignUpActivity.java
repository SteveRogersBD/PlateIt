package com.example.plateit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText etFullName;
    private TextInputEditText etUsername;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnSignUp;
    private TextView tvSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        com.example.plateit.utils.SessionManager sessionManager = new com.example.plateit.utils.SessionManager(
                SignUpActivity.this);

        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvSignIn = findViewById(R.id.tvSignIn);

        btnSignUp.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString();
            String username = etUsername.getText().toString();
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();

            if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                android.widget.Toast
                        .makeText(SignUpActivity.this, "Please fill all fields", android.widget.Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            com.example.plateit.requests.SignUpRequest request = new com.example.plateit.requests.SignUpRequest(
                    fullName, username, email, password);

            com.example.plateit.api.RetrofitClient.getService().signup(request)
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

                                android.widget.Toast.makeText(SignUpActivity.this, "Sign Up Successful",
                                        android.widget.Toast.LENGTH_SHORT).show();
                                // Proceed to Preferences Activity
                                Intent intent = new Intent(SignUpActivity.this, PreferencesActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                android.widget.Toast.makeText(SignUpActivity.this,
                                        "Sign Up Failed: " + response.message(), android.widget.Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<com.example.plateit.responses.AuthResponse> call,
                                Throwable t) {
                            android.widget.Toast.makeText(SignUpActivity.this, "Error: " + t.getMessage(),
                                    android.widget.Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        tvSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
