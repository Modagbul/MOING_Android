package com.example.moing.response;

public class MissionSkipResponse {
    private int statusCode;
    private String message;
    private String data;

    public MissionSkipResponse(int statusCode, String message, String data) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public String getData() {
        return data;
    }
}
