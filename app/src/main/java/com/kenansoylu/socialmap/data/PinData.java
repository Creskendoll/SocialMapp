package com.kenansoylu.socialmap.data;

import android.graphics.Color;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Map;

public class PinData {
    private LatLng location;
    private String title;
    private Color color;
    private String owner;

    public PinData(String title, LatLng location, String owner, Color color) {
        this.title = title;
        this.location = location;
        this.owner = owner;
        this.color = color;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Map<String, Object> serialize() {
        Map<String, Object> m = new HashMap<>();
        m.put("lat", location.latitude);
        m.put("lng", location.longitude);
        m.put("owner", owner);
        m.put("title", title);
        m.put("red", color.red());
        m.put("green", color.green());
        m.put("blue", color.blue());
        return m;
    }
}
