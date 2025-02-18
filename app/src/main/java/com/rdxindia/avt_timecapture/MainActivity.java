package com.rdxindia.avt_timecapture;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private TextView txtDistance;
    private Button btnShowLatestData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtDistance = findViewById(R.id.txtDistance);
        btnShowLatestData = findViewById(R.id.btnShowLatestData);
        Button btnShowMap = findViewById(R.id.btnShowMap);
        btnShowMap.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        });

        // Auto-start LocationService if permissions are granted.
        if (!hasLocationPermission()) {
            requestLocationPermission();
        } else {
            startLocationService();
        }

        // Button to navigate to ShowDataActivity
        btnShowLatestData.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ShowDataActivity.class);
            startActivity(intent);
        });

        // Register receiver for live distance updates
        LocalBroadcastManager.getInstance(this).registerReceiver(distanceReceiver,
                new IntentFilter("LocationDistanceUpdate"));
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void startLocationService() {
        if (hasLocationPermission()) {
            Intent serviceIntent = new Intent(this, LocationService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            Toast.makeText(this, "Location Service Started", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Location permission is required!", Toast.LENGTH_SHORT).show();
            openAppSettings();
        }
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(android.net.Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    // Receiver to update txtDistance with live distance from LocationService
    private final BroadcastReceiver distanceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.hasExtra("distance")) {
                float distance = intent.getFloatExtra("distance", 0);
                txtDistance.setText(String.format(Locale.getDefault(), "Distance: %.2f m", distance));
            }
        }
    };

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(distanceReceiver);
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationService();
            } else {
                Toast.makeText(this, "Location permission is required!", Toast.LENGTH_SHORT).show();
                openAppSettings();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}

//package com.rdxindia.avt_timecapture;
//
//import android.Manifest;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.pm.PackageManager;
//import android.os.Build;
//import android.os.Bundle;
//import android.provider.Settings;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//import androidx.localbroadcastmanager.content.LocalBroadcastManager;
//
//import java.util.Locale;
//
//public class MainActivity extends AppCompatActivity {
//
//    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
//    private TextView txtDistance;
//    private Button btnShowLatestData;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        txtDistance = findViewById(R.id.txtDistance);
//        btnShowLatestData = findViewById(R.id.btnShowLatestData);
//
//        // Auto-start LocationService if permissions are granted.
//        if (!hasLocationPermission()) {
//            requestLocationPermission();
//        } else {
//            startLocationService();
//        }
//
//        // Button to navigate to ShowDataActivity
//        btnShowLatestData.setOnClickListener(v -> {
//            Intent intent = new Intent(MainActivity.this, ShowDataActivity.class);
//            startActivity(intent);
//        });
//
//        // Register receiver for live distance updates
//        LocalBroadcastManager.getInstance(this).registerReceiver(distanceReceiver,
//                new IntentFilter("LocationDistanceUpdate"));
//    }
//
//    private boolean hasLocationPermission() {
//        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED;
//    }
//
//    private void requestLocationPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION},
//                    LOCATION_PERMISSION_REQUEST_CODE);
//        } else {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    LOCATION_PERMISSION_REQUEST_CODE);
//        }
//    }
//
//    private void startLocationService() {
//        if (hasLocationPermission()) {
//            Intent serviceIntent = new Intent(this, LocationService.class);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                startForegroundService(serviceIntent);
//            } else {
//                startService(serviceIntent);
//            }
//            Toast.makeText(this, "Location Service Started", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(this, "Location permission is required!", Toast.LENGTH_SHORT).show();
//            openAppSettings();
//        }
//    }
//
//    private void openAppSettings() {
//        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//        intent.setData(android.net.Uri.parse("package:" + getPackageName()));
//        startActivity(intent);
//    }
//
//    // Receiver to update txtDistance with live distance from LocationService
//    private final BroadcastReceiver distanceReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (intent != null && intent.hasExtra("distance")) {
//                float distance = intent.getFloatExtra("distance", 0);
//                txtDistance.setText(String.format(Locale.getDefault(), "Distance: %.2f m", distance));
//            }
//        }
//    };
//
//    @Override
//    protected void onDestroy() {
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(distanceReceiver);
//        super.onDestroy();
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                startLocationService();
//            } else {
//                Toast.makeText(this, "Location permission is required!", Toast.LENGTH_SHORT).show();
//                openAppSettings();
//            }
//        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//    }
//}
