package com.rdxindia.avt_timecapture;

import android.database.Cursor;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ShowDataActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DataAdapter dataAdapter;
    private ArrayList<String[]> dataList;
    private DB_Helper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_data);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DB_Helper(this);
        dataList = new ArrayList<>();
        loadData();

        dataAdapter = new DataAdapter(dataList);
        recyclerView.setAdapter(dataAdapter);
    }

    private void loadData() {
        Cursor cursor = dbHelper.getAllData();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(DB_Helper.COLUMN_ID));
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(DB_Helper.COLUMN_TIMESTAMP));
                String latitude = cursor.getString(cursor.getColumnIndexOrThrow(DB_Helper.COLUMN_LATITUDE));
                String longitude = cursor.getString(cursor.getColumnIndexOrThrow(DB_Helper.COLUMN_LONGITUDE));
                dataList.add(new String[]{id, timestamp, latitude, longitude});
            } while (cursor.moveToNext());
            cursor.close();
        }
    }
}
