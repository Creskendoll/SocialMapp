package com.kenansoylu.socialmap;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;

public class PinData {
    private LatLng location;
    private Color color;
    private String owner;

    public PinData(LatLng location, Color color, String owner) {
        this.location = location;
        this.color = color;
        this.owner = owner;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
