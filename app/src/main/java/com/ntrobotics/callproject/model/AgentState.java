package com.ntrobotics.callproject.model;

import com.google.gson.annotations.SerializedName;

import org.w3c.dom.Text;

public class AgentState {
    @SerializedName("check") Boolean status;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
