package com.example.moing.request;

public class TeamUpdateRequest {
    private String endDate;
    private String name;
    private String profileImg;
    private long teamId;

    public TeamUpdateRequest(String endDate, String name, String profileImg, long teamId) {
        this.endDate = endDate;
        this.name = name;
        this.profileImg = profileImg;
        this.teamId = teamId;
    }

    // Getter and Setter methods

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }
}
