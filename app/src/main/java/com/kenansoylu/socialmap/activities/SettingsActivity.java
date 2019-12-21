package com.kenansoylu.socialmap.activities;

import androidx.annotation.NonNull;
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

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.kenansoylu.socialmap.R;
import com.kenansoylu.socialmap.data.PinData;
import com.kenansoylu.socialmap.services.DBService;
import com.kenansoylu.socialmap.services.SPService;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.util.Map;

public class SettingsActivity extends AppCompatActivity {
    private ColorPickerView colorPickerView;
    private Button updateButton;
    private SPService spService;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final String pinId = getIntent().getStringExtra("pin_id");

        spService = new SPService(getApplicationContext());
        colorPickerView = findViewById(R.id.colorPickerView);
        updateButton = findViewById(R.id.updateBtn);

        colorPickerView.setColorListener(new ColorEnvelopeListener() {
            @Override
            public void onColorSelected(ColorEnvelope colorEnvelope, boolean fromUser) {
            LinearLayout linearLayout = findViewById(R.id.colorView);
            ((TextView)findViewById(R.id.colorHex)).setText("#"+colorEnvelope.getHexCode());
            linearLayout.setBackgroundColor(colorEnvelope.getColor());
            }
        });

        if(pinId != null) {
            final DBService dbService = new DBService();
            dbService.getPin(pinId, new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    Map document = task.getResult().getData();
                    if(document != null) {

                        Double r = (Double) document.getOrDefault("red", 0d);
                        Double g = (Double) document.getOrDefault("green", 0d);
                        Double b = (Double) document.getOrDefault("blue", 0d);

                        Color pinColor = Color.valueOf(
                                r.floatValue(),
                                g.floatValue(),
                                b.floatValue()
                        );

                        final String pinOwner = document.getOrDefault("owner", "-1").toString();
                        final String title = (String) document.getOrDefault("title", "");
                        final LatLng pos = new LatLng(
                                (double) document.getOrDefault("lat", 1.0),
                                (double) document.getOrDefault("lng", 1.0));

                        ((TextView)findViewById(R.id.colorHex)).setText("#"+Integer.toHexString(pinColor.toArgb()));
                        findViewById(R.id.colorView).setBackgroundColor(pinColor.toArgb());

                        final PinData oldPin = new PinData(pinId, title, pos, pinOwner, pinColor);

                        updateButton.setOnClickListener(new View.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onClick(View v) {
                                final PinData newPin = new PinData(pinId, title, pos, pinOwner, Color.valueOf(colorPickerView.getColor()));

                                dbService.updatePin(oldPin, newPin, new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(SettingsActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            });
                            }
                        });
                    }
                }
            });
        }else{
            // TODO: get default color
            Color defaultColor = spService.getColor();
            ((TextView)findViewById(R.id.colorHex)).setText("#"+Integer.toHexString(defaultColor.toArgb()));
            findViewById(R.id.colorView).setBackgroundColor(defaultColor.toArgb());

            updateButton.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View v) {
                    spService.saveColor(Color.valueOf(colorPickerView.getColor()));
                    Toast.makeText(SettingsActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

        }
    }
}
