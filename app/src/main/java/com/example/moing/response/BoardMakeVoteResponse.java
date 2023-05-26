package com.example.moing.response;

public class BoardMakeVoteResponse {
    private int statusCode;
    private String message;
    private Data data;

    public BoardMakeVoteResponse(int statusCode, String message, Data data) {
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
        private Long voteId;

        public Long getVoteId() {
            return voteId;
        }

        public Data(Long voteId) {
            this.voteId = voteId;
        }
    }
}
