package com.example.moing.response;

public class MissionCreateResponse {
    private int statusCode;
    private String message;
    private MissionData data;

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

    public MissionData getData() {
        return data;
    }

    public void setData(MissionData data) {
        this.data = data;
    }

    public static class MissionData {
        private String title;
        private String dueTo;
        private String content;
        private String rule;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDueTo() {
            return dueTo;
        }

        public void setDueTo(String dueTo) {
            this.dueTo = dueTo;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getRule() {
            return rule;
        }

        public void setRule(String rule) {
            this.rule = rule;
        }
    }
}

