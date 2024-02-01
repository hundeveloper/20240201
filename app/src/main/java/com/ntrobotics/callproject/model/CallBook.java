package com.ntrobotics.callproject.model;

import com.google.gson.annotations.SerializedName;

public class CallBook {
    @SerializedName("HP_RE")
    private String hpRe;

    public String getHpRe() {
        return hpRe;
    }

    public void setHpRe(String hpRe) {
        this.hpRe = hpRe;
    }
}
