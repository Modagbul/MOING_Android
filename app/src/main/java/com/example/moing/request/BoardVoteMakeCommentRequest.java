package com.example.moing.request;

public class BoardVoteMakeCommentRequest {
    private String content;

    public BoardVoteMakeCommentRequest(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
