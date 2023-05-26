package com.example.moing.response;

public class InvitationCodeResponse {
    private int statusCode;
    private String message;
    private InvitationCodeData data;

    // Getter and Setter methods

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public InvitationCodeData getData() {
        return data;
    }

    public void setData(InvitationCodeData data) {
        this.data = data;
    }

    public static class InvitationCodeData {
        private String invitationCode;

        // Getter and Setter methods

        public String getInvitationCode() {
            return invitationCode;
        }

        public void setInvitationCode(String invitationCode) {
            this.invitationCode = invitationCode;
        }
    }

}


