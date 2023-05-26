package com.example.moing.request;

public class MakeTeamRequest {
    private String category;
    private String info;
    private String name;
    private int period;
    private int personnel;
    private String profileImg;
    private String promise;
    private String startDate;

    public MakeTeamRequest(String category, String info, String name, int period,
                           int personnel, String profileImg, String promise, String startDate) {
        this.category = category;
        this.info = info;
        this.name = name;
        this.period = period;
        this.personnel = personnel;
        this.profileImg = profileImg;
        this.promise = promise;
        this.startDate = startDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public int getPersonnel() {
        return personnel;
    }

    public void setPersonnel(int personnel) {
        this.personnel = personnel;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    public String getPromise() {
        return promise;
    }

    public void setPromise(String promise) {
        this.promise = promise;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
}
