package com.rdxindia.avt_timecapture;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

    private final ArrayList<String[]> dataList;

    public DataAdapter(ArrayList<String[]> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String[] data = dataList.get(position);

        holder.idText.setText(data[0]);
        holder.timestampText.setText(data[1]);
        holder.distanceText.setText(data[2]); // No extra formatting
        holder.cumulativeDistanceText.setText(data[3]);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView idText, timestampText, distanceText, cumulativeDistanceText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            idText = itemView.findViewById(R.id.text_id);
            timestampText = itemView.findViewById(R.id.text_timestamp);
            distanceText = itemView.findViewById(R.id.text_distance);
            cumulativeDistanceText = itemView.findViewById(R.id.text_cumulative_distance);
        }
    }
}
