package com.example.moing.response;

import java.util.List;

public class AllNoticeResponse {
    private int statusCode;
    private String message;
    private NoticeData data;

    public AllNoticeResponse(int statusCode, String message, NoticeData data) {
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

    public NoticeData getData() {
        return data;
    }

    public static class NoticeData {
        private Long notReadNum;
        private List<NoticeBlock> noticeBlocks;

        public NoticeData(Long notReadNum, List<NoticeBlock> noticeBlocks) {
            this.notReadNum = notReadNum;
            this.noticeBlocks = noticeBlocks;
        }

        public Long getNotReadNum() {
            return notReadNum;
        }

        public List<NoticeBlock> getNoticeBlocks() {
            return noticeBlocks;
        }
    }

    public static class NoticeBlock {
        private Long noticeId;
        private String title;
        private String content;
        private Long userId;
        private String nickName;
        private String userImageUrl;
        private int commentNum;
        private String createdDate;
        private boolean read;

        public NoticeBlock(Long noticeId, String title, String content, Long userId, String nickName,
                           String userImageUrl, int commentNum, String createdDate, boolean read) {
            this.noticeId = noticeId;
            this.title = title;
            this.content = content;
            this.userId = userId;
            this.nickName = nickName;
            this.userImageUrl = userImageUrl;
            this.commentNum = commentNum;
            this.createdDate = createdDate;
            this.read = read;
        }

        public Long getNoticeId() {
            return noticeId;
        }

        public String getTitle() {
            return title;
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
