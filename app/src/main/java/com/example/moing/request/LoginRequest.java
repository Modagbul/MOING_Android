package com.example.moing.request;

public class LoginRequest {
    private String token;

    public LoginRequest(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
