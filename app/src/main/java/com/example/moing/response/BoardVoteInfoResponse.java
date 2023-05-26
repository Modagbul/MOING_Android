package com.example.moing.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BoardVoteInfoResponse {
    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private voteData data;

    public int getStatusCode() {
        return statusCode;
    }
    public String getMessage() {
        return message;
    }
    public voteData getData() {
        return data;
    }

    public BoardVoteInfoResponse(int statusCode, String message, voteData data) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }

    /** Data 클래스 **/
    public class voteData {
        @SerializedName("title")
        private String title;

        @SerializedName("memo")
        private String memo;

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

        @SerializedName("voteChoices")
        private List<VoteChoice> voteChoices;

        @SerializedName("isMultiple")
        private boolean multiple;

        @SerializedName("isAnonymous")
        private boolean anonymous;

        public String getTitle() {
            return title;
        }

        public String getMemo() {
            return memo;
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

        public boolean isMultiple() {
            return multiple;
        }

        public boolean isAnonymous() {
            return anonymous;
        }

        public List<VoteChoice> getVoteChoices() {
            return voteChoices;
        }

        /** voteData 생성자 **/
        public voteData(String title, String memo, String createdDate, Long userId, String nickName, String userImageUrl,
                        List<String> notReadUsersNickName, List<VoteChoice> voteChoices, boolean multiple, boolean anonymous) {
            this.title = title;
            this.memo = memo;
            this.createdDate = createdDate;
            this.userId = userId;
            this.nickName = nickName;
            this.userImageUrl = userImageUrl;
            this.notReadUsersNickName = notReadUsersNickName;
            this.voteChoices = voteChoices;
            this.multiple = multiple;
            this.anonymous = anonymous;
        }
    }

    /** 투표 선택 클래스 **/
    public static class VoteChoice {
        @SerializedName("content")
        private String content;

        @SerializedName("num")
        private int num;

        @SerializedName("voteUserNickName")
        private List<String> voteUserNickName;

        /** 클릭 여부 확인 **/
        private boolean isChecked;
        public boolean isChecked() {
            return isChecked;
        }
        public void setChecked(boolean checked) {
            isChecked = checked;
        }

        public String getContent() {
            return content;
        }

        public int getNum() {
            return num;
        }

        public List<String> getVoteUserNickName() {
            return voteUserNickName;
        }

        public VoteChoice(String content, int num, List<String> voteUserNickName) {
            this.content = content;
            this.num = num;
            this.voteUserNickName = voteUserNickName;
        }
    }
}
