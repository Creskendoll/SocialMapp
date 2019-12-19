package com.kenansoylu.socialmap.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class SPService {
    private SharedPreferences sharedpreferences;

    public SPService(Context context) {
        sharedpreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void saveColor(Color color) {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putFloat("R", color.red());
        editor.putFloat("G", color.green());
        editor.putFloat("B", color.blue());
        editor.apply();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Color getColor() {
        float R = sharedpreferences.getFloat("R", 0);
        float G = sharedpreferences.getFloat("G", 0);
        float B = sharedpreferences.getFloat("B", 0);
        return Color.valueOf(R, G, B);
    }
}
