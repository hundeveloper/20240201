package com.ntrobotics.callproject.model;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class CallModel {
    HashMap<String,Object> test;

    @SerializedName("STATUS")
    String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public HashMap<String, Object> getTest() {
        return test;
    }

    public void setTest(HashMap<String, Object> test) {
        this.test = test;
    }
}
