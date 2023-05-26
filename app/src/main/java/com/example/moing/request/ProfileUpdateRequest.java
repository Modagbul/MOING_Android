package com.example.moing.request;


public class ProfileUpdateRequest {
    private String introduction;
    private String nickName;
    private String profileImg;

    public ProfileUpdateRequest(String introduction, String nickName, String profileImg) {
        this.introduction = introduction;
        this.nickName = nickName;
        this.profileImg = profileImg;
    }

    // Getter and Setter methods
    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }
}
