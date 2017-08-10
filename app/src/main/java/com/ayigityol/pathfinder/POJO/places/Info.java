package com.ayigityol.pathfinder.POJO.places;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Info {
    @SerializedName("result")
    @Expose
    private Results result;

    public Results getResult() {
        return result;
    }

    public void setResult(Results result) {
        this.result = result;
    }

    public Info() {
    }

    public Info(Results result) {
        this.result = result;
    }
}
