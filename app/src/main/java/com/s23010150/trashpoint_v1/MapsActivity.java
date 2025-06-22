package com.s23010150.trashpoint_v1;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        LatLng base = new LatLng(6.9271, 79.8612);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(base, 12f));


        LatLng[] trashPoints = {
                new LatLng(6.9310, 79.8600),
                new LatLng(6.9255, 79.8650),
                new LatLng(6.9280, 79.8530),
                new LatLng(6.9220, 79.8700),
                new LatLng(6.9310, 79.8600),
                new LatLng(6.9255, 79.8650),
                new LatLng(6.9280, 79.8530),
                new LatLng(6.9220, 79.8700),
                new LatLng(7.2906, 80.6337),
                new LatLng(6.0560, 80.2181),
                new LatLng(8.5700, 81.2100),
                new LatLng(9.6600, 80.0100),
                new LatLng(7.8750, 80.0167),
                new LatLng(6.8200, 81.1100),
                new LatLng(7.2500, 82.7500),
                new LatLng(6.4500, 80.3500),
                new LatLng(7.7200, 79.9000),
                new LatLng(6.0500, 80.0000)
        };

        for (int i = 0; i < trashPoints.length; i++) {
            mMap.addMarker(new MarkerOptions()
                    .position(trashPoints[i])
                    .title("Trash Point " + (i + 1))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }
    }
}
