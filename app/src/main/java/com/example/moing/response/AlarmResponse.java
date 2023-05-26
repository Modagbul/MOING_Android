package com.example.moing.response;

import com.google.gson.annotations.SerializedName;

public class AlarmResponse {
    @SerializedName("data")
    private DataWrapper data;

    @SerializedName("message")
    private String message;

    @SerializedName("statusCode")
    private int statusCode;

    public DataWrapper getData() {
        return data;
    }

    public void setData(DataWrapper data) {
        this.data = data;
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

    public class DataWrapper {
        @SerializedName("data")
        private boolean data;


        public boolean isData() {
            return data;
        }

        public void setData(boolean data) {
            this.data = data;
        }
    }
}
