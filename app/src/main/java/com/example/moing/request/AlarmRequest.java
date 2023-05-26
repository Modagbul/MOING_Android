package com.example.moing.request;

import com.google.gson.annotations.SerializedName;

public class AlarmRequest {
    @SerializedName("data")
    private boolean data;

    public AlarmRequest(boolean data) {
        this.data = data;
    }

    public boolean isData() {
        return data;
    }

    public void setData(boolean data) {
        this.data = data;
    }
}
