package com.kenansoylu.socialmap.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kenansoylu.socialmap.R;
import com.kenansoylu.socialmap.data.PinData;
import com.kenansoylu.socialmap.data.UserData;
import com.kenansoylu.socialmap.misc.DrawableHelper;
import com.kenansoylu.socialmap.services.DBService;
import com.kenansoylu.socialmap.services.SPService;

import org.w3c.dom.Text;

import java.util.Map;

import io.opencensus.internal.Utils;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DBService service = new DBService();
    private final int LOCATION_PERMISSION = 10;
    private String userID;
    private SPService spService;
    private PinData selectedPin = null;
    private EditText pinTitleTxt;


    private boolean pinDrawerOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        spService = new SPService(getApplicationContext());

        userID = getIntent().getStringExtra("user_id");

        findViewById(R.id.colorBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsIntent = new Intent(MapsActivity.this, SettingsActivity.class);
                MapsActivity.this.startActivity(settingsIntent);
            }
        });

        pinTitleTxt = findViewById(R.id.pinTitle);
        pinTitleTxt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Button updateBtn = findViewById(R.id.updateBtn);
                updateBtn.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
            }
        });

        findViewById(R.id.updateBtn).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
            if (selectedPin != null) {
                PinData newPin = new PinData(selectedPin.getId(), pinTitleTxt.getText().toString(), selectedPin.getLocation(), selectedPin.getOwner(), selectedPin.getColor());
                new DBService().updatePin(selectedPin, newPin, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MapsActivity.this, "Pin Updated", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (pinDrawerOpen) {
            hidePinData();
        } else {
            finish();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Color getContrastColor(Color color) {
        return color.luminance() >= 0.5 ? Color.valueOf(0, 0, 0) : Color.valueOf(1, 1, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        int permCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permCoarse == PackageManager.PERMISSION_GRANTED && permFine == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            // https://stackoverflow.com/questions/18425141/android-google-maps-api-v2-zoom-to-current-location
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();

            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null)
            {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                        .zoom(10)                   // Sets the zoom
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }else{
            Toast.makeText(MapsActivity.this, "Location not enabled!", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private BitmapDescriptor getMapPin(Color fillColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Drawable vectorDrawable = DrawableHelper
                    .withContext(this)
                    .withColor(fillColor)
                    .withDrawable(R.drawable.ic_map_marker)
                    .tint()
                    .get();
            int h = vectorDrawable.getIntrinsicHeight();
            int w = vectorDrawable.getIntrinsicWidth();
            vectorDrawable.setBounds(0, 0, w, h);
            Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bm);
            vectorDrawable.draw(canvas);
            return BitmapDescriptorFactory.fromBitmap(bm);
        }

        return null;
    }

    private void hidePinData() {
        final EditText pinTitle = findViewById(R.id.pinTitle);
        final TextView pinOwner = findViewById(R.id.ownerText);
        final TextView pinColor = findViewById(R.id.pinColor);

        pinTitle.setVisibility(View.GONE);
        pinOwner.setVisibility(View.GONE);
        pinColor.setVisibility(View.GONE);
        findViewById(R.id.deleteBtn).setVisibility(View.GONE);

        selectedPin = null;
        pinDrawerOpen = false;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (Build.VERSION.SDK_INT >= 23) {
            int permCoarse  = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            int permFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (permCoarse == PackageManager.PERMISSION_GRANTED && permFine == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            } else {
                ActivityCompat.requestPermissions(
                    MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION);
            }
        } else {
            mMap.setMyLocationEnabled(true);
        }

        service.subscribeToPins(new EventListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e == null && queryDocumentSnapshots != null) {
                    mMap.clear();
                    for (QueryDocumentSnapshot pinSnapshot : queryDocumentSnapshots) {
                        Map<String, Object> pinVals = pinSnapshot.getData();
                        LatLng pos = new LatLng(
                                (double) pinVals.getOrDefault("lat", 1.0),
                                (double) pinVals.getOrDefault("lng", 1.0));

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            Double r = (Double) pinVals.getOrDefault("red", 0d);
                            Double g = (Double) pinVals.getOrDefault("green", 0d);
                            Double b = (Double) pinVals.getOrDefault("blue", 0d);

                            Color pinColor = Color.valueOf(
                                    r.floatValue(),
                                    g.floatValue(),
                                    b.floatValue()
                            );

                            String pinOwner = pinVals.getOrDefault("owner", "-1").toString();
                            boolean owned = userID.equals(pinOwner);
                            String title = (String) pinVals.getOrDefault("title", "");
                            PinData pinData = new PinData(pinSnapshot.getId(), title, pos, pinOwner, pinColor);

                            MarkerOptions options = new MarkerOptions()
                                    .title(pinData.getTitle())
                                    .position(pos).draggable(owned).icon(getMapPin(pinColor));

                            mMap.addMarker(options).setTag(pinData);
                        }
                    }
                }
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(pinDrawerOpen)
                    hidePinData();
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            public void onMapLongClick(LatLng latLng) {
                final LatLng location = latLng;

                final EditText et = new EditText(MapsActivity.this);
                AlertDialog.Builder adb = new AlertDialog.Builder(MapsActivity.this);
                adb.setTitle("Pin Title")
                    .setView(et)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            String pinTitle = et.getText().toString();

                            if (pinTitle.isEmpty()) {
                                return;
                            }
                            Color usrColor = spService.getColor();

                            PinData newPin = new PinData(null, pinTitle, location, userID, usrColor);

                            service.addPin(newPin, new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(MapsActivity.this, "Pin Added", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).show();
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public boolean onMarkerClick(Marker marker) {
                final PinData pinData = (PinData)marker.getTag();
                selectedPin = pinData;
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                new DBService().getUser(pinData.getOwner(), new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                        if(task.isSuccessful()) {
                            final DocumentSnapshot document = task.getResult();
                            if(document.exists()) {
                                pinDrawerOpen = true;

                                final boolean owned = userID.equals(pinData.getOwner());

                                final EditText pinTitle = findViewById(R.id.pinTitle);
                                final TextView pinOwner = findViewById(R.id.ownerText);
                                final TextView pinColor = findViewById(R.id.pinColor);
                                final Button deleteBtn = findViewById(R.id.deleteBtn);

                                pinTitle.setVisibility(View.VISIBLE);
                                pinTitle.setEnabled(owned);
                                pinOwner.setVisibility(View.VISIBLE);
                                pinColor.setVisibility(View.VISIBLE);

                                if(owned) {
                                    pinTitle.setBackgroundColor(Color.valueOf(0.68f,0.847f,0.90f).toArgb());
                                    deleteBtn.setVisibility(View.VISIBLE);
                                    deleteBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            AlertDialog.Builder adb = new AlertDialog.Builder(MapsActivity.this);
                                            adb.setTitle("Delete?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @RequiresApi(api = Build.VERSION_CODES.O)
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.dismiss();
                                                    new DBService().deletePin(pinData.getId(), new OnCompleteListener() {
                                                        @Override
                                                        public void onComplete(@NonNull Task task) {
                                                            hidePinData();

                                                            Toast.makeText(MapsActivity.this, "Pin Deleted!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            }).show();
                                        }
                                    });

                                    pinColor.setText("Change Color");
                                }else{
                                    pinTitle.setBackgroundColor(Color.valueOf(1f,1f,1f).toArgb());
                                    deleteBtn.setVisibility(View.GONE);
                                    pinColor.setText("");
                                }

                                pinColor.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if(owned){
                                            hidePinData();
                                            Intent settingsIntent = new Intent(MapsActivity.this, SettingsActivity.class);
                                            settingsIntent.putExtra("pin_id", pinData.getId());
                                            MapsActivity.this.startActivity(settingsIntent);
                                        }
                                    }
                                });

                                pinColor.setBackgroundColor(pinData.getColor().toArgb());
                                pinColor.setTextColor(getContrastColor(pinData.getColor()).toArgb());

                                pinTitle.setText(pinData.getTitle());

                                UserData userData = new UserData((String)document.get("id"), (String)document.get("name"));
                                pinOwner.setText("Owner: " + userData.getName());
                            }else{
                                Log.d("MAPS", "doesnt exist");
                            }
                        }else{
                            Log.d("MAPS", "Unsuccessfull");
                        }
                    }
                });
                return true;
            }
        });

        // Marker Drag Listener
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onMarkerDragEnd(Marker marker) {
                PinData pinData = (PinData) marker.getTag();

                PinData oldPin = new PinData(pinData.getId(), null, null, null, null);
                PinData newPin = new PinData(pinData.getId(), pinData.getTitle(), marker.getPosition(), pinData.getOwner(), pinData.getColor());

                service.updatePin(oldPin, newPin, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MapsActivity.this, "Pin Updated", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION);
            return;
        }

        // https://stackoverflow.com/questions/18425141/android-google-maps-api-v2-zoom-to-current-location
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        if (location != null)
        {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                    .zoom(10)                   // Sets the zoom
//                    .bearing(90)                // Sets the orientation of the camera to east
//                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }
}
