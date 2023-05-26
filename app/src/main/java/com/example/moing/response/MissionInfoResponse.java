package com.example.moing.response;

public class MissionInfoResponse {
    private int statusCode;
    private String message;
    private MissionData data;

    public MissionInfoResponse(int statusCode, String message, MissionData data) {
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

    public MissionData getData() {
        return data;
    }

    public static class MissionData {
        private String title;
        private String dueTo;
        private String content;
        private String rule;
        private String status;

        public MissionData(String title, String dueTo, String content, String rule, String status) {
            this.title = title;
            this.dueTo = dueTo;
            this.content = content;
            this.rule = rule;
            this.status = status;
        }

        public String getTitle() {
            return title;
        }

        public String getDueTo() {
            return dueTo;
        }

        public String getContent() {
            return content;
        }

        public String getRule() {
            return rule;
        }

        public String getStatus() {
            return status;
        }
    }
}
