package com.rdxindia.avt_timecapture;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize osmdroid configuration
        Configuration.getInstance().setUserAgentValue(getApplicationContext().getPackageName());

        // Set the layout for the activity
        setContentView(R.layout.activity_map);

        // Initialize the map view
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
    }

    private void loadStoredLocations() {
        // Fetch all stored locations from the database
        ArrayList<HashMap<String, String>> dataList = dbHelper.getData();

        if (dataList.isEmpty()) {
            Toast.makeText(this, "No locations found", Toast.LENGTH_SHORT).show();
        } else {
            for (HashMap<String, String> data : dataList) {
                double latitude = Double.parseDouble(data.get(DB_Helper.COLUMN_LATITUDE));
                double longitude = Double.parseDouble(data.get(DB_Helper.COLUMN_LONGITUDE));
                addMarker(latitude, longitude); // Add a marker for each location
            }
        }
    }

    private void addMarker(double lat, double lon) {
        // Create a GeoPoint for the marker
        GeoPoint point = new GeoPoint(lat, lon);

        // Create a marker and set its position
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        // Add the marker to the map
        mapView.getOverlays().add(marker);
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