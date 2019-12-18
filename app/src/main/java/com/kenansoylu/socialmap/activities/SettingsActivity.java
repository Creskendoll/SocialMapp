package com.kenansoylu.socialmap.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kenansoylu.socialmap.R;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

public class SettingsActivity extends AppCompatActivity {
    private ColorPickerView colorPickerView;
    private Button updateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        colorPickerView = findViewById(R.id.colorPickerView);
        updateButton = findViewById(R.id.updateBtn);

        // TODO: get default color

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Update
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
