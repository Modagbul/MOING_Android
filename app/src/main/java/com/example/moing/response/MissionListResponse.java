package com.example.moing.response;

import java.util.List;

public class MissionListResponse {
    private int statusCode;
    private String message;
    private List<MissionData> data;

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public List<MissionData> getData() {
        return data;
    }

    public static class MissionData {
        private Long missionId;
        private String title;
        private String dueTo;
        private String dueDate;
        private String status;

        public Long getMissionId() {
            return missionId;
        }

        public String getTitle() {
            return title;
        }

        public String getDueTo() {
            return dueTo;
        }

        public String getStatus() {
            return status;
        }
        public String getDueDate() {
            return dueDate;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
