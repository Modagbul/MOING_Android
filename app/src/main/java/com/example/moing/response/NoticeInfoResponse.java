package com.example.moing.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NoticeInfoResponse {
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

    public NoticeInfoResponse(int statusCode, String message, Data data) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }

    public static class Data {
        @SerializedName("title")
        private String title;

        @SerializedName("content")
        private String content;

        @SerializedName("createdDate")
        private String createdDate;

        @SerializedName("userId")
        private Long userId;

        @SerializedName("nickName")
        private String nickName;

        @SerializedName("userImageUrl")
        private String userImageUrl;

        @SerializedName("notReadUsersNickName")
        private List<String> notReadUsersNickName;

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public String getCreatedDate() {
            return createdDate;
        }

        public Long getUserId() {
            return userId;
        }

        public String getNickName() {
            return nickName;
        }

        public String getUserImageUrl() {
            return userImageUrl;
        }

        public List<String> getNotReadUsersNickName() {
            return notReadUsersNickName;
        }

        public Data(String title, String content, String createdDate, Long userId, String nickName, String userImageUrl,
                    List<String> notReadUsersNickName) {
            this.title = title;
            this.content = content;
            this.createdDate = createdDate;
            this.userId = userId;
            this.nickName = nickName;
            this.userImageUrl = userImageUrl;
            this.notReadUsersNickName = notReadUsersNickName;
        }
    }
}
