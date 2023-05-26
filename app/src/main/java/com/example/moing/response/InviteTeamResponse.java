package com.example.moing.response;

import com.google.gson.annotations.SerializedName;

public class InviteTeamResponse {
    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Data data;

    public InviteTeamResponse(int statusCode, String message, Data data) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public Data getData() {
        return data;
    }

    public static class Data {
        @SerializedName("teamId")
        private Long teamId;

        private String profileImg;

        public Long getTeamId() {
            return teamId;
        }

        public String getProfileImg() {
            return profileImg;
        }

        public Data(Long teamId, String profileImg) {
            this.teamId = teamId;
            this.profileImg = profileImg;
        }
    }
}
