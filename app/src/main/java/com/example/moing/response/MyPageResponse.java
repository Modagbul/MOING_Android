package com.example.moing.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MyPageResponse {
    @SerializedName("data")
    private Data data;

    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("message")
    private String message;

    public Data getData() {
        return data;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public class Data {
        @SerializedName("introduction")
        private String introduction;

        @SerializedName("nickName")
        private String nickName;

        @SerializedName("teamCount")
        private int teamCount;

        @SerializedName("teamList")
        private List<Team> teamList;

        @SerializedName("profileImg")
        private String profileImg;

        public String getProfileImg() {
            return profileImg;
        }

        public String getIntroduction() {
            return introduction;
        }

        public String getNickName() {
            return nickName;
        }

        public int getTeamCount() {
            return teamCount;
        }

        public List<Team> getTeamList() {
            return teamList;
        }
    }

    public class Team {
        @SerializedName("date")
        private String date;

        @SerializedName("profileUrl")
        private String profileUrl;

        @SerializedName("teamName")
        private String teamName;

        public String getDate() {
            return date;
        }

        public String getProfileUrl() {
            return profileUrl;
        }

        public String getTeamName() {
            return teamName;
        }
    }
}

