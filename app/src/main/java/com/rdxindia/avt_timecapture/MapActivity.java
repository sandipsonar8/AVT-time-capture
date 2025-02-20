package com.rdxindia.avt_timecapture;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.HashMap;

public class MapActivity extends AppCompatActivity {

    private MapView mapView;
    private DB_Helper dbHelper;
    private FloatingActionButton btnRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize osmdroid configuration
        Configuration.getInstance().setUserAgentValue(getApplicationContext().getPackageName());

        // Set the layout for the activity
        setContentView(R.layout.activity_map);

        // Initialize the map view
        btnRefresh = findViewById(R.id.btnRefresh);
        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK); // Use OpenStreetMap tiles
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        // Set initial map position and zoom level
        IMapController mapController = mapView.getController();
        mapController.setZoom(15.0); // Zoom level
        GeoPoint startPoint = new GeoPoint(18.467697, 73.782495); // Default location
        mapController.setCenter(startPoint);

        // Initialize database helper
        dbHelper = new DB_Helper(this);

        // Load stored locations from the database
        loadStoredLocations();
        // Handle refresh button click
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshMap();
            }
        });
    }

    private void loadStoredLocations() {
        // Clear existing markers
        mapView.getOverlays().clear();

        // Fetch all stored locations from the database
        ArrayList<HashMap<String, String>> dataList = dbHelper.getData();

        if (dataList.isEmpty()) {
            Toast.makeText(this, "No locations found", Toast.LENGTH_SHORT).show();
        } else {
            for (HashMap<String, String> data : dataList) {
                String id = data.get(DB_Helper.COLUMN_ID); // Assuming you have an ID column
                String timestamp = data.get(DB_Helper.COLUMN_TIMESTAMP); // Assuming timestamp column
                double latitude = Double.parseDouble(data.get(DB_Helper.COLUMN_LATITUDE));
                double longitude = Double.parseDouble(data.get(DB_Helper.COLUMN_LONGITUDE));

                addMarker(latitude, longitude, timestamp, id);
            }
        }

        // Refresh the map
        mapView.invalidate();
    }


    private void addMarker(double lat, double lon, String timestamp, String id) {
        // Create a GeoPoint for the marker
        GeoPoint point = new GeoPoint(lat, lon);

        // Create a marker and set its position
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle("ID: " + id + "\nTime: " + timestamp + "\nLat: " + lat + "\nLon: " + lon);


        // Add the marker to the map
        mapView.getOverlays().add(marker);
    }
    private void refreshMap() {
        Toast.makeText(this, "Refreshing Map...", Toast.LENGTH_SHORT).show();
        loadStoredLocations();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume(); // Required for osmdroid
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause(); // Required for osmdroid
    }
}