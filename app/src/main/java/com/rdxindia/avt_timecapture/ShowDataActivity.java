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
                String distance = cursor.getString(cursor.getColumnIndexOrThrow(DB_Helper.COLUMN_DISTANCE));
                String cumulativeDistance = cursor.getString(cursor.getColumnIndexOrThrow(DB_Helper.COLUMN_CUMULATIVE_DISTANCE));

                // Store values without further modification
                dataList.add(new String[]{id, timestamp, distance, cumulativeDistance});

            } while (cursor.moveToNext());
            cursor.close();
        }
    }




}
