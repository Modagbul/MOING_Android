package com.example.moing.response;

public class MoimMasterResponse {
    private boolean data;
    private String message;
    private int statusCode;

    public boolean isMoimMaster() {
        return data;
    }

    public void setMoimMaster(boolean moimMaster) {
        this.data = moimMaster;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
