package com.example.moing.response;

public class NoticeCreateResponse {

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
        private int noticeId;

        public long getNoticeId() {
            return noticeId;
        }
    }

}
