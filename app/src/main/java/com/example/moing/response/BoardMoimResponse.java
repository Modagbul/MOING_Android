package com.example.moing.response;

public class BoardMoimResponse {
    private int statusCode;
    private String message;
    private Data data;

    public BoardMoimResponse(int statusCode, String message, Data data) {
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
        private String name;
        private String profileImg;
        private String remainingPeriod;
        private String nowTime;

        public Data(String name, String profileImg, String remainingPeriod, String nowTime) {
            this.name = name;
            this.profileImg = profileImg;
            this.remainingPeriod = remainingPeriod;
            this.nowTime = nowTime;
        }

        public String getName() {
            return name;
        }

        public String getProfileImg() {
            return profileImg;
        }

        public String getRemainingPeriod() {
            return remainingPeriod;
        }

        public String getNowTime() {
            return nowTime;
        }

        // 필요한 생성자, getter, setter 등을 포함합니다.
    }
}
