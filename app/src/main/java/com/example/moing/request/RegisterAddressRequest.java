package com.example.moing.request;

public class RegisterAddressRequest {
    private String accessToken;
    private String address;
    private String nickName;
    private String fcmToken;

    public RegisterAddressRequest(String accessToken, String address, String nickName, String fcmToken) {
        this.accessToken = accessToken;
        this.address = address;
        this.nickName = nickName;
        this.fcmToken = fcmToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
