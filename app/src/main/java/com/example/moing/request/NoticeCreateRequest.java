package com.example.moing.request;

public class NoticeCreateRequest {

    private String title;
    private String content;

    public NoticeCreateRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

}
