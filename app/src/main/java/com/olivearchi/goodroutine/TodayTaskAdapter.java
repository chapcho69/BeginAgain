package com.olivearchi.goodroutine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class TodayTaskAdapter extends RecyclerView.Adapter<TodayTaskAdapter.ViewHolder> {

    private List<TodayTaskItem> tasks;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(TodayTaskItem item);
    }

    public TodayTaskAdapter(List<TodayTaskItem> tasks, OnItemClickListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_today_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TodayTaskItem item = tasks.get(position);
        holder.titleText.setText(item.getTitle());
        holder.descText.setText(item.getDescription());
        holder.timeText.setText(String.format(Locale.getDefault(), "소요시간: %d분 (+10분)", item.getEstimatedMinutes()));
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, descText, timeText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.text_task_title);
            descText = itemView.findViewById(R.id.text_task_desc);
            timeText = itemView.findViewById(R.id.text_task_time);
        }
    }
}
