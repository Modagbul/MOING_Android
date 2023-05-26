package com.example.moing.response;

public class MissionUpdateResponse {
    private int statusCode;
    private String message;
    private Data data;

    public static class Data {
        private String title;
        private String dueTo;
        private String content;
        private String rule;
        private String achieve;

        // Getter and Setter methods

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

        public String getAchieve() {
            return achieve;
        }

        public void setAchieve(String achieve) {
            this.achieve = achieve;
        }
    }

    // Getter and Setter methods

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

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}
