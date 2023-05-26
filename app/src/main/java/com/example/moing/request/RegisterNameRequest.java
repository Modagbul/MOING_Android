package com.example.moing.request;

public class RegisterNameRequest {

    // 닉네임 중복 검사
    private String result;

    public RegisterNameRequest(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

}
