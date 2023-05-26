package com.example.moing.response;

public class BoardFireResponse {
    private int statusCode;
    private String message;
    private Data data;

    public BoardFireResponse(int statusCode, String message, Data data) {
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

    public Data getData() {
        return data;
    }

    public static class Data {
        private Long percent;
        private String fireCopy;

        public Data(Long percent, String fireCopy) {
            this.percent = percent;
            this.fireCopy = fireCopy;
        }

        public Long getPercent() {
            return percent;
        }

        public String getFireCopy() {
            return fireCopy;
        }
    }
}
