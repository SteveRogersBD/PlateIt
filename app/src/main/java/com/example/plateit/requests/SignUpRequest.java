package com.example.plateit.requests;

public class SignUpRequest {
    private String full_name;
    private String username;
    private String email;
    private String password;

    public SignUpRequest(String full_name, String username, String email, String password) {
        this.full_name = full_name;
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
