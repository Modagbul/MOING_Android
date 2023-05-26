package com.example.moing.response;

public class NoticeVoteFinishResponse {
    private int statusCode;
    private String message;

    public NoticeVoteFinishResponse(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }
}
