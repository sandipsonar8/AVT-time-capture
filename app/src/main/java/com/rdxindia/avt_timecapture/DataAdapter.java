package com.rdxindia.avt_timecapture;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

    private ArrayList<String[]> dataList;

    public DataAdapter(ArrayList<String[]> dataList) {
        this.dataList = dataList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvId, tvTimestamp, tvLat, tvLon;

        public ViewHolder(View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvId);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvLat = itemView.findViewById(R.id.tvLat);
            tvLon = itemView.findViewById(R.id.tvLon);
        }
    }

    @Override
    public DataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(DataAdapter.ViewHolder holder, int position) {
        String[] item = dataList.get(position);
        // item[0]=ID, item[1]=timestamp, item[2]=latitude, item[3]=longitude
        holder.tvId.setText("ID: " + item[0]);
        holder.tvTimestamp.setText("Time: " + item[1]);
        holder.tvLat.setText("Lat: " + item[2]);
        holder.tvLon.setText("Lon: " + item[3]);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}
