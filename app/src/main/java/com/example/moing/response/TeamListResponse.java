package com.example.moing.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TeamListResponse {
    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Data data;

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public Data getData() {
        return data;
    }

    public class Data {
        @SerializedName("userNickName")
        private String userNickName;

        @SerializedName("inProgressNum")
        private int inProgressNum;

        @SerializedName("teamBlocks")
        private List<Team> teamBlocks;

        public int getInProgressNum() {
            return inProgressNum;
        }

        public List<Team> getTeamBlocks() {
            return teamBlocks;
        }
        public String getUserNickName() { return userNickName; }
    }

    public static class Team {
        @SerializedName("teamId")
        private long teamId;

        @SerializedName("name")
        private String name;

        @SerializedName("personnel")
        private int personnel;

        @SerializedName("startDate")
        private String startDate;

        @SerializedName("endDate")
        private String endDate;

        @SerializedName("profileImg")
        private String profileImg;

        @SerializedName("approvalStatus")
        private boolean approvalStatus;

        public Team(long teamId) {
            this.teamId = teamId;
        }

        public long getTeamId() {
            return teamId;
        }

        public void setTeamId(int teamId) {
            this.teamId = teamId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getPersonnel() {
            return personnel;
        }

        public void setPersonnel(int personnel) {
            this.personnel = personnel;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public String getProfileImg() {
            return profileImg;
        }

        public void setProfileImg(String profileImg) {
            this.profileImg = profileImg;
        }

        public boolean isApprovalStatus() {
            return approvalStatus;
        }

        public void setApprovalStatus(boolean approvalStatus) {
            this.approvalStatus = approvalStatus;
        }
    }
}

