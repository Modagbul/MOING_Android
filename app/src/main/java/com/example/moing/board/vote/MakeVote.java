package com.example.moing.board.vote;

public class MakeVote {
    // 투표 항목
    private String voteContent;
    private boolean isSelected;

    public void setVoteContent(String voteContent) {
        this.voteContent = voteContent;
    }

    public String getVoteContent() {
        return voteContent;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
