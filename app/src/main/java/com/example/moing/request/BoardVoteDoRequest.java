package com.example.moing.request;

import java.util.List;

public class BoardVoteDoRequest {
    private List<String> choices;

    public BoardVoteDoRequest(List<String> choices) {
        this.choices = choices;
    }

    public List<String> getChoices() {
        return choices;
    }
}
