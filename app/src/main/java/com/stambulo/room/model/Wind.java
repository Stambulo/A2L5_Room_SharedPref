package com.stambulo.room.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

//"wind":{"speed":1,"deg":140}
public class Wind {
    @SerializedName("speed")
    @Expose
    private float speed;

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
