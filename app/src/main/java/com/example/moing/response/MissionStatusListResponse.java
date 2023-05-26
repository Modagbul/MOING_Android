package com.example.moing.response;

import java.util.List;

public class MissionStatusListResponse {
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
        private String remainDay;
        private int completeUser;
        private int incompleteUser;
        private String myStatus;
        private List<Integer> fireUserMissionList;
        private List<UserMission> completeList;
        private List<UserMission> incompleteList;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getRemainDay() {
            return remainDay;
        }

        public void setRemainDay(String remainDay) {
            this.remainDay = remainDay;
        }

        public int getCompleteUser() {
            return completeUser;
        }

        public void setCompleteUser(int completeUser) {
            this.completeUser = completeUser;
        }

        public int getIncompleteUser() {
            return incompleteUser;
        }

        public void setIncompleteUser(int incompleteUser) {
            this.incompleteUser = incompleteUser;
        }

        public String getMyStatus() {
            return myStatus;
        }

        public void setMyStatus(String myStatus) {
            this.myStatus = myStatus;
        }

        public List<Integer> getFireUserMissionList() {
            return fireUserMissionList;
        }

        public void setFireUserMissionList(List<Integer> fireUserMissionList) {
            this.fireUserMissionList = fireUserMissionList;
        }

        public List<UserMission> getCompleteList() {
            return completeList;
        }

        public void setCompleteList(List<UserMission> completeList) {
            this.completeList = completeList;
        }

        public List<UserMission> getIncompleteList() {
            return incompleteList;
        }

        public void setIncompleteList(List<UserMission> incompleteList) {
            this.incompleteList = incompleteList;
        }
    }

    public static class UserMission {
        private int userMissionId;
        private String nickname;
        private String profileImg;
        private String status;
        private String archive;
        private String submitDate;

        public int getUserMissionId() {
            return userMissionId;
        }

        public void setUserMissionId(int userMissionId) {
            this.userMissionId = userMissionId;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getProfileImg() {
            return profileImg;
        }

        public void setProfileImg(String profileImg) {
            this.profileImg = profileImg;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getArchive() {
            return archive;
        }

        public void setArchive(String archive) {
            this.archive = archive;
        }

        public String getSubmitDate() {
            return submitDate;
        }

        public void setSubmitDate(String submitDate) {
            this.submitDate = submitDate;
        }
    }
}

