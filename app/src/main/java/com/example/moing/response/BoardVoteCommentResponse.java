package com.example.moing.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BoardVoteCommentResponse {
    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<VoteData> data;

    public BoardVoteCommentResponse(int statusCode, String message, List<VoteData> data) {
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

    public List<VoteData> getData() {
        return data;
    }

    public static class VoteData {
        @SerializedName("voteCommentId")
        private Long voteCommentId;

        @SerializedName("content")
        private String content;

        @SerializedName("userId")
        private Long userId;

        @SerializedName("nickName")
        private String nickName;

        @SerializedName("userImageUrl")
        private String userImageUrl;

        @SerializedName("createdDate")
        private String createdDate;

        public VoteData(Long voteCommentId, String content, Long userId, String nickName, String userImageUrl, String createdDate) {
            this.voteCommentId = voteCommentId;
            this.content = content;
            this.userId = userId;
            this.nickName = nickName;
            this.userImageUrl = userImageUrl;
            this.createdDate = createdDate;
        }

        public Long getVoteCommentId() {
            return voteCommentId;
        }

        public String getContent() {
            return content;
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

        public String getCreatedDate() {
            return createdDate;
        }
    }
}
