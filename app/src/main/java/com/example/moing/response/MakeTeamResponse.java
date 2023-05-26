package com.example.moing.response;

public class MakeTeamResponse {
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
        private int teamId;
        private String invitationCode;

        public int getTeamId() {
            return teamId;
        }

        public String getInvitationCode() {
            return invitationCode;
        }
    }
}
