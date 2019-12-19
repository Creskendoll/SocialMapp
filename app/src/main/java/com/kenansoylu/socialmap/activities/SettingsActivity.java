package com.kenansoylu.socialmap.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kenansoylu.socialmap.R;
import com.kenansoylu.socialmap.services.SPService;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

public class SettingsActivity extends AppCompatActivity {
    private ColorPickerView colorPickerView;
    private Button updateButton;
    private SPService spService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        spService = new SPService(getApplicationContext());
        colorPickerView = findViewById(R.id.colorPickerView);
        updateButton = findViewById(R.id.updateBtn);

        // TODO: get default color

        updateButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                spService.saveColor(Color.valueOf(colorPickerView.getColor()));
                Toast.makeText(SettingsActivity.this, "Updated", Toast.LENGTH_SHORT).show();
            }
        });

        colorPickerView.setColorListener(new ColorEnvelopeListener() {
            @Override
            public void onColorSelected(ColorEnvelope colorEnvelope, boolean fromUser) {
                LinearLayout linearLayout = findViewById(R.id.colorView);
                ((TextView)findViewById(R.id.colorHex)).setText("#"+colorEnvelope.getHexCode());
                linearLayout.setBackgroundColor(colorEnvelope.getColor());
            }
        });
    }
}
