package com.rdxindia.avt_timecapture;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
    private static final String CHANNEL_ID = "location_channel";

    private LocationManager locationManager;
    private DB_Helper dbHelper;

    private static final float MOVING_DISTANCE_THRESHOLD = 50.0f; // 50 meters
    private static final long STABLE_TIME_THRESHOLD = 180000; // 3 minutes

    private Location lastStoredLocation;
    private long lastStoredTime = 0;
    private Location latestLocation;
    private double cumulativeDistance = 0.0;

    private Handler handler;
    private Runnable periodicCheck;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DB_Helper(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Load last cumulative distance from database
        loadLastCumulativeDistance();

        createNotificationChannel();
        startForeground(1, createForegroundNotification());

        try {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 5000, 20, this);
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 10000, 50, this);
        } catch (SecurityException e) {
            Log.e(TAG, "Permission error: " + e.getMessage());
        }


        // Periodic check every 10 seconds
        handler = new Handler();
        periodicCheck = new Runnable() {
            @Override
            public void run() {
                checkStorageConditions();
                handler.postDelayed(this, 10000); // Check every 10 sec
            }
        };
        handler.post(periodicCheck);
    }

    private void checkStorageConditions() {
        long currentTime = System.currentTimeMillis();

        if (lastStoredLocation == null && latestLocation != null) {
            storeAndUpdate(latestLocation, currentTime);
            return;
        }

        if (latestLocation != null && (currentTime - lastStoredTime) >= STABLE_TIME_THRESHOLD) {
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

        float distance = location.distanceTo(lastStoredLocation);

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
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        double distance = 0.0;

        if (lastStoredLocation != null) {
            distance = location.distanceTo(lastStoredLocation);
            cumulativeDistance += distance;
        }

        ContentValues values = new ContentValues();
        values.put(DB_Helper.COLUMN_TIMESTAMP, dateTime);
        values.put(DB_Helper.COLUMN_LATITUDE, latitude);
        values.put(DB_Helper.COLUMN_LONGITUDE, longitude);
        values.put(DB_Helper.COLUMN_DISTANCE, distance);
        values.put(DB_Helper.COLUMN_CUMULATIVE_DISTANCE, cumulativeDistance);

        db.insert(DB_Helper.TABLE_NAME, null, values);
        lastStoredLocation = location;
        lastStoredTime = System.currentTimeMillis();

        Log.d(TAG, "Stored: " + dateTime + ", Distance: " + distance + "m, Cumulative: " + cumulativeDistance + "m");
    }

    private void loadLastCumulativeDistance() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + DB_Helper.COLUMN_CUMULATIVE_DISTANCE +
                " FROM " + DB_Helper.TABLE_NAME + " ORDER BY " + DB_Helper.COLUMN_TIMESTAMP + " DESC LIMIT 1", null);

        if (cursor != null && cursor.moveToFirst()) {
            cumulativeDistance = cursor.getDouble(0);
        }
        if (cursor != null) {
            cursor.close();
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
