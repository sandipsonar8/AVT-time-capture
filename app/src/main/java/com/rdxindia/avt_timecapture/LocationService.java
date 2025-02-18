package com.rdxindia.avt_timecapture;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LocationService extends Service implements LocationListener {

    private static final String TAG = "LocationService";
    private LocationManager locationManager;
    private DB_Helper dbHelper;

    // Configurable thresholds
//    private static final float MOVING_DISTANCE_THRESHOLD = 1.0f; // 1 meter
//    private static final long STABLE_TIME_THRESHOLD = 10000;     // 10 seconds


    private static final float MOVING_DISTANCE_THRESHOLD = 50.0f; // 50 meters
    private static final long STABLE_TIME_THRESHOLD = 60000;     // 60 seconds

    private Location lastStoredLocation;
    private long lastStoredTime = 0;
    private Location latestLocation;

    private Handler handler;
    private Runnable periodicCheck;
    private static final String CHANNEL_ID = "location_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DB_Helper(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        createNotificationChannel();
        startForeground(1, createForegroundNotification());

        try {
            // Request location updates from both GPS and Network providers
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 1000, 0, this);
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 1000, 0, this);
        } catch (SecurityException e) {
            Log.e(TAG, "Permission error: " + e.getMessage());
        }

        // Setup periodic check every second
        handler = new Handler();
        periodicCheck = new Runnable() {
            @Override
            public void run() {
                checkStorageConditions();
                handler.postDelayed(this, 1000); // Check every second
            }
        };
        handler.post(periodicCheck);
    }

    private void checkStorageConditions() {
        long currentTime = System.currentTimeMillis();

        // Initial storage condition
        if (lastStoredLocation == null && latestLocation != null) {
            storeAndUpdate(latestLocation, currentTime);
            return;
        }

        // Time-based storage condition
        if (latestLocation != null &&
                (currentTime - lastStoredTime) >= STABLE_TIME_THRESHOLD) {

            storeAndUpdate(latestLocation, currentTime);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        latestLocation = location;

        if (lastStoredLocation == null) {
            storeAndUpdate(location, System.currentTimeMillis());
            return;
        }

        // Calculate distance from last stored location
        float distance = location.distanceTo(lastStoredLocation);

        // Distance-based storage condition
        if (distance >= MOVING_DISTANCE_THRESHOLD) {
            storeAndUpdate(location, System.currentTimeMillis());
        }
    }

    private void storeAndUpdate(Location location, long timestamp) {
        storeLocation(location);
        lastStoredLocation = location;
        lastStoredTime = timestamp;
    }

    private void storeLocation(Location location) {
        String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        if (location != null) {
            dbHelper.putData(dateTime,
                    String.valueOf(location.getLatitude()),
                    String.valueOf(location.getLongitude())
            );
            Log.d(TAG, "Stored: " + dateTime + ", Lat: " + location.getLatitude() +
                    ", Lon: " + location.getLongitude());
        } else {
            dbHelper.putData(dateTime, "N/A", "N/A"); // Handle null location
            Log.d(TAG, "Stored: " + dateTime + ", No location");
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Service",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createForegroundNotification() {
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                new Intent(this, MainActivity.class),
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Service")
                .setContentText("Tracking location in background")
                .setSmallIcon(R.drawable.ic_location)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
        if (handler != null) {
            handler.removeCallbacks(periodicCheck);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}