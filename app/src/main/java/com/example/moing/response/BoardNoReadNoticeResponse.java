package com.example.moing.response;

import java.util.List;

public class BoardNoReadNoticeResponse {
    private String message;
    private int statusCode;
    private List<NoticeData> data;

    public BoardNoReadNoticeResponse(String message, int statusCode, List<NoticeData> data) {
        this.message = message;
        this.statusCode = statusCode;
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public List<NoticeData> getData() {
        return data;
    }

    public static class NoticeData {
        private String title;
        private String content;
        private Long noticeId;

        public NoticeData(String title, String content, Long noticeId) {
            this.title = title;
            this.content = content;
            this.noticeId = noticeId;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public Long getNoticeId() {
            return noticeId;
        }
    }
}
