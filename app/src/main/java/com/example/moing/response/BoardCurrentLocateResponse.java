package com.example.moing.response;

public class BoardCurrentLocateResponse {
    private int statusCode;
    private String message;
    private Data data;

    public BoardCurrentLocateResponse(int statusCode, String message, Data data) {
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
        private Long personalRate;
        private Long teamRate;

        public Data(Long personalRate, Long teamRate) {
            this.personalRate = personalRate;
            this.teamRate = teamRate;
        }

        public Long getPersonalRate() {
            return personalRate;
        }

        public Long getTeamRate() {
            return teamRate;
        }
    }

}
