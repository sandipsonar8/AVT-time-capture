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
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LocationService extends Service implements LocationListener {

    private static final String TAG = "LocationService";
    private LocationManager locationManager;
    private DB_Helper dbHelper;
    private Location lastLocation;
    private float distanceThreshold = 1;
    private long timeThreshold = 10000; // 10 seconds
    private float totalDistance = 0;
    private long lastStoredTime = 0;
    private static final String CHANNEL_ID = "location_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DB_Helper(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Create Notification Channel
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

        // Start as a foreground service
        startForeground(1, createForegroundNotification());

        // Request location updates
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, this);
        } catch (SecurityException e) {
            Log.e(TAG, "Permission error: " + e.getMessage());
        }
    }

    private Notification createForegroundNotification() {
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                new Intent(this, MainActivity.class),
                PendingIntent.FLAG_IMMUTABLE // Fixed the PendingIntent issue
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
    public void onLocationChanged(Location location) {
        if (lastLocation == null) {
            lastLocation = location;
            lastStoredTime = System.currentTimeMillis();
            return;
        }

        float distance = location.distanceTo(lastLocation);
        totalDistance += distance;
        long currentTime = System.currentTimeMillis();

        // Store data based on distance or time
        if (totalDistance >= distanceThreshold || (currentTime - lastStoredTime) >= timeThreshold) {
            String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            dbHelper.putData(dateTime, String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
            // Reset counters
            totalDistance = 0;
            lastStoredTime = currentTime;
            lastLocation = location;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}