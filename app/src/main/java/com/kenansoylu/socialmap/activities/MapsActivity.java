package com.kenansoylu.socialmap.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kenansoylu.socialmap.R;
import com.kenansoylu.socialmap.data.PinData;
import com.kenansoylu.socialmap.misc.DrawableHelper;
import com.kenansoylu.socialmap.services.DBService;
import com.kenansoylu.socialmap.services.SPService;

import java.util.Map;

import io.opencensus.internal.Utils;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DBService service = new DBService();
    private final int LOCATION_PERMISSION = 10;
    private String userID;
    private SPService spService;

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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        int permCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permCoarse == PackageManager.PERMISSION_GRANTED && permFine == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            AlertDialog.Builder adb = new AlertDialog.Builder(MapsActivity.this);
            adb.setTitle("Location Disabled")
                    .setMessage("Konum Hizmetini Açmak İçin, Uygulamayı Baştan Başlatın Veya İzinler Sekmesine Gidin")
                    .show();
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

//            mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
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

                            boolean owned = userID == pinVals.getOrDefault("owner", "-1");

                            MarkerOptions options = new MarkerOptions()
                                    .title((String) pinVals.getOrDefault("title", "a"))
                                    .position(pos).draggable(owned).icon(getMapPin(pinColor));

                            mMap.addMarker(options);
                        }
                    }
                }
            }
        });

        // OnLongClick Listener
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            public void onMapLongClick(LatLng latLng) {
                final LatLng location = latLng;
                /*
                MarkerOptions newMo = new MarkerOptions()
                .position(latLng)
                .title("Seçtiğiniz Nokta")a
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .draggable(true);
                map.addMarker(newMo);
                */

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

                            PinData newPin = new PinData(pinTitle, location, userID, usrColor);

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

        // Marker Drag Listener
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                Log.e("x", "Started @" + marker.getPosition());
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                Log.e("x", "Currently @" + marker.getPosition());
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                Log.e("x", "Dropped @" + marker.getPosition());
            }
        });
    }
}
