package com.example.moing.response;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    private int statusCode;
    private String message;
    private Data data;

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public Data getData() {
        return data;
    }

    public class Data {
        @SerializedName("accessToken")
        private String accessToken;
        @SerializedName("refreshToken")
        private String refreshToken;
        @SerializedName("process")
        private String process;
        @SerializedName("userId")
        private Long userId;

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public String getProcess() {
            return process;
        }

        public Long getUserId() {
            return userId;
        }
    }
}
