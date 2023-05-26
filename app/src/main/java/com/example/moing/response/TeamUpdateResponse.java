package com.example.moing.response;

public class TeamUpdateResponse {
    private int statusCode;
    private String message;

    // Getter, Setter 등 필요한 메서드 추가

    // 예시:
    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
