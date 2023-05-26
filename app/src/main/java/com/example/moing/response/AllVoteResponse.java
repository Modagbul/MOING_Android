package com.example.moing.response;

import java.util.List;

public class AllVoteResponse {
    private int statusCode;
    private String message;
    private VoteData data;

    public AllVoteResponse(int statusCode, String message, VoteData data) {
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

    public VoteData getData() {
        return data;
    }

    public static class VoteData {
        private Long notReadNum;
        private List<VoteBlock> voteBlocks;

        public VoteData(Long notReadNum, List<VoteBlock> voteBlocks) {
            this.notReadNum = notReadNum;
            this.voteBlocks = voteBlocks;
        }

        public Long getNotReadNum() {
            return notReadNum;
        }

        public List<VoteBlock> getVoteBlocks() {
            return voteBlocks;
        }
    }

    public static class VoteBlock {
        private Long voteId;
        private String title;
        private String memo;
        private Long userId;
        private String nickName;
        private String userImageUrl;
        private int commentNum;
        private String createdDate;
        private boolean read;

        public VoteBlock(Long voteId, String title, String memo, Long userId, String nickName,
                         String userImageUrl, int commentNum, String createdDate, boolean read) {
            this.voteId = voteId;
            this.title = title;
            this.memo = memo;
            this.userId = userId;
            this.nickName = nickName;
            this.userImageUrl = userImageUrl;
            this.commentNum = commentNum;
            this.createdDate = createdDate;
            this.read = read;
        }

        public Long getVoteId() {
            return voteId;
        }

        public String getTitle() {
            return title;
        }

        public String getMemo() {
            return memo;
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

        public int getCommentNum() {
            return commentNum;
        }

        public String getCreatedDate() {
            return createdDate;
        }

        public boolean getRead() {
            return read;
        }
    }
}
