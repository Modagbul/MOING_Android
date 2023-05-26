package com.example.moing.response;

import com.google.gson.annotations.SerializedName;

public class AlarmSettingResponse {
    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Data data;

    // Getters for statusCode, message, and data

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

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public class Data {
        @SerializedName("isTotal")
        private boolean isTotal;

        @SerializedName("isRemind")
        private boolean isRemind;

        @SerializedName("isNewUpload")
        private boolean isNewUpload;

        @SerializedName("isFire")
        private boolean isFire;

        // Getters for isTotal, isRemind, isNewUpload, and isFire

        public boolean isTotal() {
            return isTotal;
        }

        public void setTotal(boolean total) {
            isTotal = total;
        }

        public boolean isRemind() {
            return isRemind;
        }

        public void setRemind(boolean remind) {
            isRemind = remind;
        }

        public boolean isNewUpload() {
            return isNewUpload;
        }

        public void setNewUpload(boolean newUpload) {
            isNewUpload = newUpload;
        }

        public boolean isFire() {
            return isFire;
        }

        public void setFire(boolean fire) {
            isFire = fire;
        }
    }
}
