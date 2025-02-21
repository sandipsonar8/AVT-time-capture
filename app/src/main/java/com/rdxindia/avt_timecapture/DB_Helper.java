package com.rdxindia.avt_timecapture;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class DB_Helper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "location_data.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_NAME = "location_data";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";

    public static final String COLUMN_DISTANCE = "distance";
    public static final String COLUMN_CUMULATIVE_DISTANCE = "cumulative_distance";
    public static final String COLUMN_SPEED = "speed";
    public DB_Helper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        String createTableSQL = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TIMESTAMP + " TEXT, " +
                COLUMN_LATITUDE + " TEXT, " +
                COLUMN_LONGITUDE + " TEXT, " +
                COLUMN_DISTANCE + " REAL DEFAULT 0, " +
                COLUMN_CUMULATIVE_DISTANCE + " REAL DEFAULT 0, " +
                COLUMN_SPEED + " REAL DEFAULT 0);"; // Added speed column
        db.execSQL(createTableSQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_SPEED + " REAL DEFAULT 0;");
        }
    }

    // Store data in the database
    public void putData(String timestamp, String latitude, String longitude, String distance, String cumulativeDistance, String speed) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_TIMESTAMP, timestamp);
        contentValues.put(COLUMN_LATITUDE, latitude);
        contentValues.put(COLUMN_LONGITUDE, longitude);
        contentValues.put(COLUMN_DISTANCE, distance);
        contentValues.put(COLUMN_CUMULATIVE_DISTANCE, cumulativeDistance);
        contentValues.put(COLUMN_SPEED, speed);

        db.insert(TABLE_NAME, null, contentValues);
    }

    // Get the latest location data
    public Cursor getLatestData() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_ID + " DESC LIMIT 1";
        return db.rawQuery(query, null);
    }

    // Get all location data
    public Cursor getAllData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, null, null, null, null, null, COLUMN_TIMESTAMP + " DESC");
    }

    // Method to fetch all data from the database
    public ArrayList<HashMap<String, String>> getData() {
        ArrayList<HashMap<String, String>> dataList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT " + COLUMN_TIMESTAMP + ", " +
                COLUMN_LATITUDE + ", " + COLUMN_LONGITUDE + ", " +
                COLUMN_DISTANCE + ", " + COLUMN_CUMULATIVE_DISTANCE +", " + COLUMN_SPEED +
                " FROM " + TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> data = new HashMap<>();
                data.put(COLUMN_TIMESTAMP, cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));
                data.put(COLUMN_LATITUDE, cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)));
                data.put(COLUMN_LONGITUDE, cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE)));
                data.put(COLUMN_DISTANCE, cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISTANCE)));
                data.put(COLUMN_CUMULATIVE_DISTANCE, cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CUMULATIVE_DISTANCE)));

                dataList.add(data);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return dataList;
    }
}
