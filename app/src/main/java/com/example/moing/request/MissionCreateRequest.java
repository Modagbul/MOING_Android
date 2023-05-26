package com.example.moing.request;

public class MissionCreateRequest {
    private String title;
    private String dueTo;
    private String content;
    private String rule;

    public MissionCreateRequest(String title, String dueTo, String content, String rule) {
        this.title = title;
        this.dueTo = dueTo;
        this.content = content;
        this.rule = rule;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDueTo() {
        return dueTo;
    }

    public void setDueTo(String dueTo) {
        this.dueTo = dueTo;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }
}

