package com.example.moing.request;

import java.util.List;

public class BoardMakeVoteRequest {
    private List<String> choices;
    private String memo;
    private String title;
    private boolean isMultiple;
    private boolean isAnonymous;

    public BoardMakeVoteRequest(List<String> choices, String memo, String title, boolean isMultiple, boolean isAnonymous) {
        this.choices = choices;
        this.memo = memo;
        this.title = title;
        this.isMultiple = isMultiple;
        this.isAnonymous = isAnonymous;
    }

    public List<String> getChoices() {
        return choices;
    }

    public String getMemo() {
        return memo;
    }

    public String getTitle() {
        return title;
    }

    public boolean isMultiple() {
        return isMultiple;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }
}
