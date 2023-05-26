package com.example.moing.response;

public class ChangeJwtResponse {

    private int statusCode;

    private String message;

    private ChangeJwtResponse.Data data;

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public ChangeJwtResponse.Data getData() {
        return data;
    }

    public static class Data {
        private String accessToken;
        private String refreshToken;

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }
    }
}
