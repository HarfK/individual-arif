package com.example.individual_arif;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for the bill history list.
 * Each item shows: Month  |  Final Cost (RM)
 */
public class BillAdapter extends RecyclerView.Adapter<BillAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(BillRecord record);
    }

    private final Context            context;
    private final List<BillRecord>   records;
    private       OnItemClickListener listener;

    public BillAdapter(Context context, List<BillRecord> records) {
        this.context = context;
        this.records = records;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_bill, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BillRecord record = records.get(position);
        holder.tvMonth.setText(record.getMonth());
        holder.tvFinalCost.setText(
                String.format(Locale.getDefault(), "RM %.2f", record.getFinalCost()));
        holder.tvIndex.setText(String.valueOf(position + 1));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(record);
        });
    }

    @Override
    public int getItemCount() { return records.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIndex;
        TextView tvMonth;
        TextView tvFinalCost;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIndex    = itemView.findViewById(R.id.tvIndex);
            tvMonth    = itemView.findViewById(R.id.tvMonth);
            tvFinalCost = itemView.findViewById(R.id.tvFinalCost);
        }
    }
}
