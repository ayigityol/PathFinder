package com.ayigityol.pathfinder.POJO.places;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Results {

    @SerializedName("geometry")
    @Expose
    private Geometry geometry;

    @SerializedName("name")
    @Expose
    private String name;

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Results() {
    }

    public Results(Geometry geometry, String name) {
        this.geometry = geometry;
        this.name = name;
    }
}
