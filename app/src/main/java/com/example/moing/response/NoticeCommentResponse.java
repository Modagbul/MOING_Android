package com.example.moing.response;

public class NoticeCommentResponse {
    private int statusCode;
    private String message;
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

    public static class Data {
        private int noticeCommentId;

        public long getNoticeCommentId() {
            return noticeCommentId;
        }
    }
}
