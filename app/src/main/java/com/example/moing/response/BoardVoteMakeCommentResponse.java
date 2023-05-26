package com.example.moing.response;

public class BoardVoteMakeCommentResponse {
    private int statusCode;
    private String message;
    private Data data;

    public BoardVoteMakeCommentResponse(int statusCode, String message, Data data) {
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

    public Data getData() {
        return data;
    }

    public static class Data {
        private Long voteCommentId;

        public Data(Long voteCommentId) {
            this.voteCommentId = voteCommentId;
        }

        public Long getVoteCommentId() {
            return voteCommentId;
        }
    }

}
